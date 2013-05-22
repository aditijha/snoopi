package org.snoopi.server.main;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.methods.HttpGet;
import com.mongodb.*;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 * Created with IntelliJ IDEA.
 * User: aditijha
 * Date: 10/04/13
 * Time: 1:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class Common {
    MongoClient mongoClient;
    DB db;
    DBCollection collection;
    public static Properties config = new Properties();
    int host_id;
    int thread_count;
    BlockingQueue url_queue;
    int max_queue_size;

    public static String getExceptionString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }

    public void setup() throws Exception {
        config.load(new FileInputStream("./config.properties"));
        mongoClient = new MongoClient(config.getProperty("db.hostname"),Integer.parseInt(config.getProperty("db.port")));
        db = mongoClient.getDB(config.getProperty("db.dbname"));
        collection = db.getCollection(config.getProperty("db.tablename"));
        host_id = Integer.parseInt(config.getProperty("host_id"));
        thread_count = Integer.parseInt(config.getProperty("thread.count"));
        max_queue_size = Integer.parseInt(config.getProperty("max_queue_size"));
        url_queue = new LinkedBlockingQueue(max_queue_size);
    }

    public String getCurrentTimeStamp() {
        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(dt);
    }

    public int getStatusCode(String url) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpParams params = client.getParams();
        HttpConnectionParams.setConnectionTimeout(params, 3000);
        HttpConnectionParams.setSoTimeout(params, 3000);
        HttpGet httpget = new HttpGet(url);
        HttpResponse httpResp = client.execute(httpget);
        return httpResp.getStatusLine().getStatusCode();
    }
}
