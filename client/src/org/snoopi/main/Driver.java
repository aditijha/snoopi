package org.snoopi.main;

/**
 * Created with IntelliJ IDEA.
 * User: aditijha
 * Date: 09/04/13
 * Time: 5:01 PM
 * To change this template use File | Settings | File Templates.
 */

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import org.snoopi.Common;
import org.snoopi.utility.ReportGenerator;
import org.snoopi.utility.UrlExtractor;

public class Driver extends Common {

    public Driver() throws Exception {
        setup();
        setupDBConnection();
    }

    private void setupDBConnection() throws Exception {
        mongoClient = new MongoClient(config.getProperty("db.hostname"),Integer.parseInt(config.getProperty("db.port")));
        db = mongoClient.getDB(config.getProperty("db.dbname"));
        /*boolean auth = db.authenticate(config.getProperty("db.username"), config.getProperty("db.password").toCharArray());
        if(auth==false) {
            System.out.println("Unable to connect to DB. Quitting now.");
            System.exit(0);
        } */
        collection = db.getCollection(config.getProperty("db.tablename"));
    }

    private void cleanup() throws Exception {
        if(!Boolean.parseBoolean(config.getProperty("result_persist")))
            collection.remove(new BasicDBObject("run_data.id",run_id));
        mongoClient.close();
    }

    public static void main(String args[]) throws Exception {
        try {
            long startTime = System.currentTimeMillis();
            Driver driver = new Driver();
            System.out.println("----------------"+run_id+"----------------");
            UrlExtractor urlExtractor = new UrlExtractor();
            System.out.println(urlExtractor.insertDefaultUrls());
            System.out.println(urlExtractor.addUrlsUptoDepth(1));
            System.out.println(driver.waitForResponseUpdates());
            driver.cleanup();
            long endTime   = System.currentTimeMillis();
            long totalTime = (endTime - startTime)/60000;
            //System.out.println("Time take to complete this run: "+totalTime/60000+ " minutes.");
            new ReportGenerator().generateHTMLReport(".",run_id,totalTime+" minutes"); 
            //new ReportGenerator().generateHTMLReport(".","20130509 130047","2 minutes");
        } catch (Exception e) {
            System.out.println(getExceptionString(e));
        }
    }

    private boolean waitForResponseUpdates() throws Exception{
        System.out.println("Starting the wait at: " + getCurrentTimeStamp());
        BasicDBObject getUncheckedUrlsQuery =  new BasicDBObject("run_data",new BasicDBObject("$elemMatch",new BasicDBObject("id",run_id).append("response", "unchecked")));
        DBCursor cursor;
        long wait_time_left=Long.parseLong(config.getProperty("max_wait_time_in_millis"));
        do {
            cursor = collection.find(getUncheckedUrlsQuery);
            if(cursor.hasNext()) {
                Thread.sleep(5000);
                wait_time_left=wait_time_left-5000;
            }
            else
                return true;
        } while (wait_time_left > 0);
        return false;
    }
}
