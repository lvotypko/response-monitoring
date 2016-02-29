/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hudson.plugins.statistic.performance.web.response;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Testing  {
    
    private Long responseTime;
    
    private Long obtainContent;
    
    private Long performingJS;
    
    private static List<String> pagesToMonitor = new ArrayList<String>();
    
    private static File reportDir = new File("/home/lucinka/jira/2016/statistic/");
    
    private static String urlToReport;
    
    private static Long intervalToAverage = 1000l*60*60;
    
    private static Long monitorInterval = 1000l*60*5;
    
    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException{
        pagesToMonitor.add("https://jenkins-test-rhel6.mw.lab.eng.bos.redhat.com/hudson/");
        pagesToMonitor.add("https://jenkins-test-rhel6.mw.lab.eng.bos.redhat.com/hudson/computer/vmg32/");
        pagesToMonitor.add("http://jenkins-test-rhel6.mw.lab.eng.bos.redhat.com/hudson/");
        pagesToMonitor.add("http://jenkins-test-rhel6.mw.lab.eng.bos.redhat.com/hudson/computer/vmg32/");
       // pagesToMonitor.add("https://jenkins.mw.lab.eng.bos.redhat.com/hudson/view/Jenkins/view/lvotypko/job/matrix-test-bug/configure");
       // pagesToMonitor.add("https://jenkins.mw.lab.eng.bos.redhat.com/hudson/configure");
        if(args.length!=0){//
            String configFile = args[0];
            File file = new File(configFile);
           // FileInputStream st = new FileInputStream(file);
            Reader reader = new FileReader(file);
            BufferedReader stream = new BufferedReader(reader);
            String config = stream.readLine();
            Map<String,String> settings = new HashMap<String,String>();
            while(config !=null){
                String[] setting = config.split("=");
                settings.put(setting[0], setting[1]);
                config = stream.readLine();
            }
            extractSettings(settings);
        }
        ResponseMonitor monitor = new ResponseMonitor(pagesToMonitor);
        monitor.execute();
//        for(PageResponse page : monitor.getResponses()){
//            File file = new File(reportDir, page.getPageName().replace("/", "_") +".properties");
//            PrintStream st = new PrintStream(file);
//            if(file.exists()){
//                st.append("total="+page.getTotalTime() + ",server="+page.getServerResponseTime() + ",content="+page.getObtainContentTime() + ",others="+page.getPerformingJSTime() +"\n");
//            }
//            else{
//                st.print("total="+page.getTotalTime() + ",server="+page.getServerResponseTime() + ",content="+page.getObtainContentTime() + ",others="+page.getPerformingJSTime() +"\n");
//            }
//            st.flush();
//            st.close();
//        }
        
    }
    
    public static void extractSettings(Map<String,String> settings){
        String pagesSetting = settings.get("pages");
        if(pagesSetting!=null){
            String[] pages = pagesSetting.split(",");
            for(String page : pages){
                pagesToMonitor.add(page);
            }
        }
        String reportFile = settings.get("reportDir");
        if(reportDir!=null){
            reportDir = new File(reportFile);
        }
        String interv = settings.get("interval");
        if(interv!=null){
            Long hours = Long.decode(interv);
            intervalToAverage = hours*1000*60*60;
        }
        String monInterv = settings.get("monitorInterval");
        if(monInterv!=null){
            Long minutes = Long.decode(monInterv);
            monitorInterval = minutes*1000*60;
        }
    }
    
    
    
    public void execute() throws FileNotFoundException {
        
        
        
    }
    
    

}
