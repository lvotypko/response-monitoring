package hudson.plugins.statistic.performance.web.response;

import edu.umass.cs.benchlab.har.HarEntry;
import edu.umass.cs.benchlab.har.HarLog;
import edu.umass.cs.benchlab.har.HarPage;
import edu.umass.cs.benchlab.har.HarPages;
import edu.umass.cs.benchlab.har.tools.HarFileReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonParseException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;

public class ResponseMonitor {
    
    private Map<String,List<PageResponse>> responses = new HashMap<String,List<PageResponse>>();
    
   
    
    
    public void execute() throws InterruptedException, IOException{
        FirefoxDriver driver = setDriver();
        File directory = new File("/home/lucinka/jira/2016/har");
        int i = 0;
        while(i < 10){
            for(String page : responses.keySet()){
                System.out.println("investigation page " + page);
                cleanDirectory(directory);
                PageResponse response = visitPage(page, driver);
                if(directory.list().length==0){
                    response = visitPage(page, driver);
                }
                if(directory.list().length>1){
                    cleanDirectory(directory);
                    response = visitPage(page, driver);
                }
                try {
                    analyze(directory.listFiles()[0], response, i);
                } catch (Exception ex) {
                    cleanDirectory(directory);
                    response = visitPage(page, driver);
                    try {
                        analyze(directory.listFiles()[0], response, i);
                    } catch (Exception ex1) {
                        System.err.println("can not parse " + page);
                        ex1.printStackTrace();
                    }
                }
                if(page.contains("configure")){
                    System.out.println("end of page " + page);
                    driver.quit();
                    driver = setDriver();
                }
            }
            i++;
            Thread.sleep(60000);
        }
        driver.quit();
        writeData();
        
    }
    
    public void writeData() throws FileNotFoundException{
        File recordDir = new File("/home/lucinka/jira/2016/statistic");
        for(String page : responses.keySet()){
            List<PageResponse> resp = responses.get(page);
            PageResponse r = doAverage(resp, page);
            File record = new File(recordDir,page.replace("/", "_").replace("jenkins.mw.lab.eng.bos.redhat.com", ""));
            write(resp, record);
            File recordTotal = new File(recordDir,"total_" + page.replace("/", "_").replace("jenkins.mw.lab.eng.bos.redhat.com", ""));
            PrintStream st = new PrintStream(recordTotal);
            r.write(st);
            st.flush();
            st.close();
        }
    }
    
    public void write(List<PageResponse> resp, File file) throws FileNotFoundException{
        PrintStream st = new PrintStream(file);
        for(PageResponse r: resp){
            r.write(st);
            st.println("");
        }
        st.flush();
        st.close();
    }
    
    public PageResponse doAverage(List<PageResponse> resp, String page){
        long total = 0l;
        long server = 0l;
        long content = 0l;
        long others = 0l;
        int count = resp.size();
        for(PageResponse r : resp){
            total += r.getTotalTime();
            server += r.getServerResponseTime();
            content += r.getObtainContentTime();
            others += r.getPerformingJSTime();
        }
        PageResponse res = new PageResponse(total/count, "total_" + page);
        res.setObtainContentTime(content/count);
        res.setPerformingJSTime(others/count);
        res.setServerResponseTime(server/count);
        return res;
    }
    
    public PageResponse visitPage(String page, FirefoxDriver driver) throws InterruptedException{
        long time = System.currentTimeMillis();
        System.out.println("displayin page " + page);
        driver.get(page);
        time = System.currentTimeMillis() - time;
        PageResponse res = new PageResponse(time, page);
        Thread.sleep(30000);
        return res;
    }
    
    private void cleanDirectory(File dir){
        if(dir==null || dir.list()==null){
            return;
        }
        System.out.println("cleaning directory " + dir);
        for(File file : dir.listFiles()){
            System.out.println("cleaning file " +file);
            file.delete();
        }
    }
    
    
    public ResponseMonitor(List<String> pages){
        for(String page :pages){
            responses.put(page, new ArrayList<PageResponse>());
        }
    }
    
    public void analyze(File har, PageResponse response, int number) throws JsonParseException, IOException, Exception {
        HarFileReader reader = new HarFileReader();
        HarLog log = reader.readHarFile(har);
        parse(response, log);
        responses.get(response.getPageName()).add(response);
        System.out.println("add response " + response);
        har.delete();
    }
    
    private long count(Long l){
        System.out.println("time" +l);
        if(l==null){
            return 0l;
        }
        return l;
    }
    
    public void parse(PageResponse response, HarLog log) throws Exception{
        HarPages pages = log.getPages();
        if(pages.getPages().isEmpty()){
            throw new Exception("There no page");
        }
        HarPage page = pages.getPages().get(0);
        HarEntry entry = log.getEntries().getEntries().get(0);
        if(!response.getPageName().equals(entry.getRequest().getUrl())){
            throw new Exception("There is no declared page");
        }
        long time = 0l;
        time += count(entry.getTimings().getBlocked());
        time += count(entry.getTimings().getConnect());
        time += count(entry.getTimings().getDns());
        time += count(entry.getTimings().getReceive());
        time += count(entry.getTimings().getSend());
       // time += count(entry.getTimings().getSsl());
        time += count(entry.getTimings().getWait());
        response.setServerResponseTime(time);
        response.setObtainContentTime(page.getPageTimings().getOnContentLoad() - time);
        response.setPerformingJSTime(response.getTotalTime() - response.getObtainContentTime() - response.getServerResponseTime());
    }
    
    public FirefoxDriver setDriver() throws InterruptedException, IOException{
        ProfilesIni prof = new ProfilesIni();
        FirefoxProfile profile = prof.getProfile("selenium");
        
        File dir = new File("/home/lucinka/jira/2016");
        File firebug = new File(dir,"firebug-1.11.4b1.xpi");
        File netExport = new File(dir,"netExport-0.9b4.xpi");
        profile.addExtension(firebug);
        profile.addExtension(netExport);

        // Set default Firefox preferences
        profile.setPreference("app.update.enabled", false);

        String domain = "extensions.firebug.";

        // Set default Firebug preferences
        profile.setPreference(domain + "currentVersion", "1.11.4b1");
        profile.setPreference(domain + "allPagesActivation", "on");
        profile.setPreference(domain + "defaultPanelName", "net");
        profile.setPreference(domain + "net.enableSites", true);

        // Set default NetExport preferences
        profile.setPreference(domain + "netexport.alwaysEnableAutoExport", true);
        profile.setPreference(domain + "netexport.showPreview", false);
        profile.setPreference(domain + "netexport.defaultLogDir", "/home/lucinka/jira/2016/har");

        FirefoxDriver driver = new FirefoxDriver(profile);
        Thread.sleep(5000);
        return driver;
        
    }
    
    
}