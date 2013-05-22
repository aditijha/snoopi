package org.snoopi.utility;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import org.snoopi.Common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: aditijha
 * Date: 03/05/13
 * Time: 4:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReportGenerator extends Common {

    private StringBuffer createNewTable(String tablename) {
        return new StringBuffer("<table id='"+tablename+"'style='table-layout: fixed;word-wrap: break-word;border-left:1px dotted black;border-right:1px dotted black;padding-top:.5cm;padding-bottom:.5cm;border-bottom:1px dotted black;width: 12in; display:none;margin-left: auto; margin-right: auto; font-size: 15px;text-align:center;'>\n<tbody>\n<tr style='background-color:#E2E2E2;text-weight:bold;'><th style='width: 4.5in;padding-bottom: 0.3em;padding-top: 0.3em;'>URL</th><th style='width: 4.5in;'>Parent URL</th><th style='width: 1.25in;'>Response</th><th style='width: 1.75in;'>Tested at</td></tr>\n");
    }

    public void generateHTMLReport(String output_folder,String run_id,String run_time) throws Exception {
        String date = run_id.substring(0,4)+"/"+run_id.substring(4,6)+"/"+run_id.substring(6,8)+" "+run_id.substring(9,11)+":"+run_id.substring(11,13)+":"+run_id.substring(13);
        System.out.println(date);
        BasicDBObject run_data_query =  new BasicDBObject("run_data.id",run_id);
        DBCursor cursor = collection.find(run_data_query);
        FileWriter fstream=null;
        BufferedWriter out=null;
        int count=0,ok_count=0,redirect_count=0,not_found_count=0,server_error_count=0,other_count=0;
        BasicDBObject url_entry,run_data;
        Iterator run_data_iterator;
        String url,parent_url,tested_at,response;
        String temp;
        StringBuffer ok=null,redirect=null,not_found=null,server_error=null,other=null;
        while (cursor.hasNext()) {
            //System.out.println(cursor.count());
            if(fstream==null) {
                fstream = new FileWriter(output_folder+"/"+run_id.replaceAll(" ","")+"_report.html");
                out = new BufferedWriter(fstream);
                out.write("<html>");
                out.write("<head><script type='text/javascript'>function setButtonsHeights(){var height = 'innerHeight' in window ? window.innerHeight : document.documentElement.offsetHeight; var t=height*.8; document.getElementById('top').style.top=t+10; document.getElementById('collapse').style.top=t;} function toggle(element_name) { var e=document.getElementById(element_name); e.style.display = (e.style.display == 'none') ? 'block' : 'none'; } function collapseAll(){var elem=document.getElementById('correct_link_table');if(elem != null) elem.style.display='none';elem=document.getElementById('redirect_link_table');if(elem != null) elem.style.display='none';elem=document.getElementById('not_found_table');if(elem != null) elem.style.display='none';elem=document.getElementById('server_error_table');if(elem != null) elem.style.display='none';elem=document.getElementById('other_link_table');if(elem != null) elem.style.display='none';}</script></head>");
                out.write("<body onload='setButtonsHeights();'><div style='width:12in;text-align:center;line-height:1cm;font-size:.7cm;margin:.5cm;font-weight:bold;'>SNOOPI Link Check Report: "+date+"</div>\n");
                out.write("<div id='collapse' onclick='collapseAll();' style='background-color: #F6F6F6;border-top: 1px solid #A3A3A3;border-bottom: 1px solid #A3A3A3;border-right: 1px solid #A3A3A3;border-radius: 0 10px 10px 0;color:#A3A3A3;padding: 7px;position: fixed;text-align: center;font-size:15px;'>Collapse<br>All</div>\n");
                out.write("<div id='top' style='position:fixed;right:0;border-top: 1px solid #A3A3A3;border-bottom: 1px solid #A3A3A3;border-left: 1px solid #A3A3A3;color:#A3A3A3;padding:5px 10px;border-radius:10px 0 0 10px;text-align:center;background-color:#F6F6F6;font-size:15px;' onclick='window.scrollTo(0,0);'>Top &uarr;</div>");
            }
            url_entry = (BasicDBObject)cursor.next();
            url = (String)url_entry.get("url");
            run_data_iterator = ((BasicDBList) url_entry.get("run_data")).iterator();
            while (run_data_iterator.hasNext()) {
                run_data = (BasicDBObject)run_data_iterator.next();
                if(run_data.get("id").equals(run_id)) {
                    parent_url = (String)run_data.get("parent_url");
                    response = (String)run_data.get("response");
                    tested_at = (String)run_data.get("tested_at");
                    temp = "<tr style='background-color:white;'><td style='padding-bottom: 0.3em;padding-top: 0.3em;border-top: 1px dotted gray;width: 4.5in;'>"+url+"</td><td style='border-top: 1px dotted gray;width: 4.5in;'>"+parent_url+"</td><td style='border-top: 1px dotted gray;width: 1.25in;'>"+response+"</td><td style='border-top: 1px dotted gray;width: 1.75in;'>"+tested_at+"</td></tr>\n";
                    if(response.equals("200")) {
                        if(ok==null)
                            ok=createNewTable("correct_link_table");
                        if(ok_count%2==1)
                            temp=temp.replace("white","#F3FFE7");
                        ok.append(temp);
                        ok_count++;
                    }
                    else if(response.startsWith("3")) {
                        if(redirect==null)
                            redirect = createNewTable("redirect_link_table");
                        if(redirect_count%2==1)
                            temp=temp.replace("white","#F0F0F0");
                        redirect.append(temp);
                        redirect_count++;
                    }
                    else if(response.equals("404")) {
                        if(not_found==null)
                            not_found=createNewTable("not_found_table");
                        if(not_found_count%2==1)
                            temp=temp.replace("white","#FFF0F0");
                        not_found.append(temp);
                        not_found_count++;
                    }
                    else if(response.startsWith("5")) {
                        if(server_error==null)
                            server_error=createNewTable("server_error_table");
                        if(server_error_count%2==1)
                            temp=temp.replace("white","#FFF8EF");
                        server_error.append(temp);
                        server_error_count++;
                    }
                    else {
                        System.out.println(response);
                        if(other==null) {
                            other=createNewTable("other_link_table");
                            System.out.println("Created the Other table");
                        }
                        if(other_count%2==1)
                            temp=temp.replace("white","#F2FBFF");
                        other.append(temp);
                        System.out.println(other_count++);
                    }
                    count++;
                }
            }
        }
        if(fstream==null)
            System.out.println("No records found. Report not generated.");
        else {
            out.write("<div style='width:5in;font-size:17px;font-weight:bold;margin-top:.5cm;text-align:center;margin-left:auto;margin-right:auto;margin-bottom:.5cm;'>Total Links Checked : "+count+"<br><br>Total Run Time : "+run_time+"</div>");
            out.write("<div onclick=\"toggle('not_found_table');\" style='width:12in;height:1cm;margin-left:auto;margin-right:auto;border-top:1px solid gray;border-bottom:1px solid gray;background-color:#FFC7C7;padding-left:1cm;color:gray;line-height:1cm;'>+&nbsp;&nbsp;Broken Links : "+not_found_count+"</a></div>");
            if(not_found!=null) {
                not_found.append("</tbody></table>");
                out.write(not_found.toString());
            }
            out.write("<br><div onclick=\"toggle('server_error_table');\" style='width:12in;height:1cm;margin-left:auto;margin-right:auto;border-top:1px solid gray;border-bottom:1px solid gray;background-color:#FEF0E0;padding-left:1cm;color:gray;line-height:1cm;'>+&nbsp;&nbsp;Server Error Links : "+server_error_count+"</a></div>");
            if(server_error!=null) {
                server_error.append("</tbody></table>");
                out.write(server_error.toString());
            }
            out.write("<br><div onclick=\"toggle('redirect_link_table');\" style='width:12in;height:1cm;margin-left:auto;margin-right:auto;border-top:1px solid gray;border-bottom:1px solid gray;background-color:#E7E7E7;padding-left:1cm;color:gray;line-height:1cm;'>+&nbsp;&nbsp;Redirect Links : "+redirect_count+"</a></div>");
            if(redirect!=null) {
                redirect.append("</tbody></table>");
                out.write(redirect.toString());
            }
            out.write("<br><div onclick=\"toggle('correct_link_table');\" style='width:12in;height:1cm;margin-left:auto;margin-right:auto;border-top:1px solid gray;border-bottom:1px solid gray;background-color:#E3FFC6;padding-left:1cm;color:gray;line-height:1cm;'>+&nbsp;&nbsp;Correct Links: "+ok_count+"</a></div>");
            if(ok!=null) {
                ok.append("</tbody></table>");
                out.write(ok.toString());
            }
            out.write("<br><div onclick=\"toggle('other_link_table');\" style='width:12in;height:1cm;margin-left:auto;margin-right:auto;border-top:1px solid gray;border-bottom:1px solid gray;background-color:#E2F4FD;padding-left:1cm;color:gray;line-height:1cm;'>+&nbsp;&nbsp;Other : "+other_count+"</a></div>");
            if(other!=null){
                other.append("</tbody></table>");
                out.write(other.toString());
            }
            out.write("</body></html>");
            out.close();
            System.out.println(count);
        }
    }

}
