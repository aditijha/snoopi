package org.snoopi.utility;

/**
 * Created with IntelliJ IDEA.
 * User: aditijha
 * Date: 09/04/13
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.mongodb.*;
import org.snoopi.Common;

public class UrlExtractor extends Common {

    public int insertDefaultUrls() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("./default_url_list.txt"));
        String line;
        int count=0;
        while((line=br.readLine())!=null) {
            if(!line.startsWith(system_under_test) && !line.startsWith("http") && !line.startsWith("www.")) {
                line=system_under_test+line;
            }
            if(line.endsWith("/"))
                line=line.substring(0,line.length()-1);
            makeDbEntry(line,"NA");
            count++;
        }
        br.close();
        return count;
    }

    private boolean makeDbEntry(String url,String parent_url) {
        BasicDBObject find_url, url_entry,temp;
        BasicDBList all_run_data;
        DBCursor dbCursor;
        find_url = new BasicDBObject("url",url);
        dbCursor = collection.find(find_url);
        if(dbCursor.hasNext()) {
            url_entry = (BasicDBObject) dbCursor.next();
            temp = new BasicDBObject("url",url).append("run_data.id",run_id);
            dbCursor = collection.find(temp);
            if(!dbCursor.hasNext()) {
                all_run_data = (BasicDBList)url_entry.get("run_data");
                all_run_data.add(new BasicDBObject().append("id", run_id).append("tested_at", getCurrentTimeStamp()).append("parent_url", parent_url).append("response", "unchecked").append("test_host_id",getNextHostId()));
                url_entry.put("run_data", all_run_data);
                collection.update(find_url, url_entry);
                return true;
            }
        }
        else {
            url_entry = new BasicDBObject().append("url", url).append("details","unknown");
            all_run_data = new BasicDBList();
            all_run_data.add(new BasicDBObject().append("id", run_id).append("tested_at", getCurrentTimeStamp()).append("response", "unchecked").append("parent_url", parent_url).append("test_host_id",getNextHostId()));
            url_entry.append("run_data", all_run_data);
            collection.insert(url_entry);
            return true;
        }
        return false;
    }

    public int addUrlsUptoDepth(int depth) throws Exception {
        if(depth<0)
            return 0;
        if(depth==0) {
            if(makeDbEntry(system_under_test,"NA")) {
                return 1;
            }
        }
        HashMap<String,String> urls = new HashMap<String, String>();
        ArrayList<String> curr_links = new ArrayList<String>();
        ArrayList<String> next_links;
        curr_links.add(system_under_test);
        String[] temp;
        int count=0;
        for(int curr_depth=1;curr_depth<=depth;curr_depth++) {
            System.out.println("At level: "+curr_depth+". urls so far: "+urls.size()+". Links to traverse in this level: "+curr_links.size());
            next_links = new ArrayList<String>();
            for(String link:curr_links) {
                try {
                    temp = getPageLinks(link);
                    if(temp != null && temp.length>0) {
                        for(String url:temp) {
                            if(url.endsWith("/"))
                                url = url.substring(0,url.length()-1);
                            if(!url.contains("#") && !urls.containsKey(url)) {
                                urls.put(url, link);
                                makeDbEntry(url,link);
                                next_links.add(url);
                                count++;
                                System.out.println(count+". "+url);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Exception while working on: "+link+"\n"+getExceptionString(e));
                }
            }
            System.out.println("new urls added in this level: "+next_links.size());
            curr_links = next_links;
        }
       /* int size=0;
        Iterator itr = urls.keySet().iterator();
        String url;
        FileWriter fstream = new FileWriter("out.txt");
        BufferedWriter out = new BufferedWriter(fstream);
        while(itr.hasNext()) {
            url = (String) itr.next();
            if(makeDbEntry(url,urls.get(url))) {
                out.write(url);
                out.write("\n");
                size++;
            }
        }
        out.close();
        return size;   */
        return count;
    }

    public int getNextHostId() {
        if(this.host_id >= server_hosts_available)
            this.host_id = 1;
        else
            this.host_id++;
        return host_id;
    }

}
