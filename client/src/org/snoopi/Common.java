package org.snoopi;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.By;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created with IntelliJ IDEA.
 * User: aditijha
 * Date: 09/04/13
 * Time: 2:28 PM
 * To change this template use File | Settings | File Templates.
 */

public class Common {

    public static String system_under_test;
    public static Properties config = new Properties();
    public static HtmlUnitDriver htmlUnitDriver;
    public static String run_id;
    public static int server_hosts_available;
    public long max_wait_time_in_millis;
    public static MongoClient mongoClient;
    public static DB db;
    public static DBCollection collection;
    public static int host_id=0;

    public void setup() throws Exception {
        config.load(new FileInputStream("./config.properties"));
        system_under_test = config.getProperty("system_under_test");
        if(system_under_test.endsWith("/"))
            system_under_test=system_under_test.substring(0,system_under_test.length()-1);
        htmlUnitDriver = new HtmlUnitDriver();
        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd HHmmss");
        run_id = sdf.format(dt);
        server_hosts_available = Integer.parseInt(config.getProperty("snoopi.server.count"));
    }

    public String getCurrentTimeStamp() {
        java.util.Date dt = new java.util.Date();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(dt);
    }

    public String[] getPageLinks(String url) throws Exception {
        htmlUnitDriver.get(url);
        List<WebElement> elements = htmlUnitDriver.findElements(getBy("//a[@href and not(contains(@href,'javascript')) and not(contains(@href,'#'))]"));
        if(elements != null && elements.size()>0) {
            ArrayList<String> links = new ArrayList<String>();
            String href;
            for(WebElement element:elements) {
                href=element.getAttribute("href");
                if(!links.contains(href))
                    links.add(href);
            }
            return links.toArray(new String[links.size()]);
        }
        return null;
    }

    public By getBy(String locator) {
        By by;
        if(locator.startsWith("//"))
            by = By.xpath(locator);
        else if(locator.startsWith("css="))
            by = By.cssSelector(locator.replace("css=","").trim());
        else if(locator.startsWith("link="))
            by = By.linkText(locator.replace("link=",""));
        else
            by = By.id(locator);
        return by;
    }

    public static String getExceptionString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        e.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }
}