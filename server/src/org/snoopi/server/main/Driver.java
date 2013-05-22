package org.snoopi.server.main;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
/**
 * Created with IntelliJ IDEA.
 * User: aditijha
 * Date: 10/04/13
 * Time: 4:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class Driver extends Common {

    private void startEnqueuingUrls() {
        Thread enqueue = new Thread() {
            DBCursor cursor;
            BasicDBObject url_extract_query =  new BasicDBObject("run_data",new BasicDBObject("$elemMatch",new BasicDBObject("response","unchecked").append("test_host_id",host_id)));
            @Override
            public void run() {
                System.out.println("Enqueue Thread Started.");
                int count=0;
                while (true) {
                    cursor  = collection.find(url_extract_query);
                    count = cursor.count();
                    if(count>0) {
                        count=0;
                        while(cursor.hasNext() && count<max_queue_size) {
                            try {
                                url_queue.put(cursor.next().get("url"));
                                count++;
                            } catch (InterruptedException ir) {
                                System.out.println(getExceptionString(ir));
                            }
                        }
                        System.out.println("Added "+count+" urls to the queue.");
                        synchronized (url_queue){url_queue.notifyAll();}
                        while(!url_queue.isEmpty()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ie) {
                                System.out.println(getExceptionString(ie));
                            }
                        }
                    }
                }
            }
        };
        enqueue.start();
    }

    private void startVerifyingUrls() {
        Thread[] threads = new Thread[thread_count];
        for(int i=0;i<thread_count;i++) {
            threads[i] = new Thread((i+1)+"") {
                String url_under_test;
                int response,run_data_size;
                BasicDBObject run_data_query,run_data,url_entry;
                DBCursor run_data_cursor;
                BasicDBList run_data_list;
                @Override
                public void run() {
                    System.out.println("Started dequeue thread number: "+this.getName());
                    while (true) {
                            if(!url_queue.isEmpty()) {
                                try {
                                    synchronized (url_queue){
                                        url_under_test = (String)url_queue.take();
                                    };
                                    System.out.println("Thread "+this.getName()+": "+url_under_test);
                                } catch (InterruptedException ie) {
                                    System.out.println(ie);
                                }
                                try {
                                    response = getStatusCode(url_under_test);
                                } catch (Exception e) {
                                    System.out.println(getExceptionString(e));
                                    continue;
                                }
                                run_data_query = new BasicDBObject("url",url_under_test);
                                run_data_cursor = collection.find(run_data_query);
                                if(run_data_cursor.hasNext()) {
                                    url_entry = (BasicDBObject) run_data_cursor.next();
                                    run_data_list = (BasicDBList) url_entry.get("run_data");
                                    run_data_size = run_data_list.size();
                                    for(int i=0;i<run_data_size;i++) {
                                        run_data = (BasicDBObject)run_data_list.get(i);
                                        if(run_data.get("response").equals("unchecked")) {
                                            run_data.put("response", response + "");
                                            run_data.put("tested_at",getCurrentTimeStamp());
                                        }
                                        run_data_list.put(i,run_data);
                                    }
                                    url_entry.put("run_data",run_data_list);
                                    //System.out.println("url_entry_after: "+url_entry+"\n");
                                    collection.update(run_data_query,url_entry);
                                }
                            }
                            else {
                                try {
                                    System.out.println("Message from Thread "+this.getName()+": Entering wait state as no url enqueued for verification.");
                                    synchronized (url_queue){url_queue.wait();}
                                } catch (InterruptedException ie) {
                                    System.out.println(ie);
                                }
                            }
                        }
                }
            };
            threads[i].start();
        }
    }

    public static void main(String args[]) throws Exception {
        Driver driver = new Driver();
        driver.setup();
        System.out.println("This is Host number: "+driver.host_id+", connecting to mongo db host: "+driver.mongoClient.getAddress());
        driver.startEnqueuingUrls();
        driver.startVerifyingUrls();
    }

    public void cleanup() {
        mongoClient.close();
    }
}
