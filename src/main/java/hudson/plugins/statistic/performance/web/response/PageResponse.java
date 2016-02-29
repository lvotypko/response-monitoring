/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hudson.plugins.statistic.performance.web.response;

import java.io.PrintStream;

/**
 *
 * @author lucinka
 */
public class PageResponse {
    
    private String pageName;
    
    private Long responseTime;
    
    private Long obtainContent;
    
    private Long performingJS;
    
    private Long totalTime;
    
    public PageResponse(Long totalTime, String pageName){
        this.totalTime = totalTime;
        this.pageName = pageName;
    }
    
    public void setServerResponseTime(Long time){
        responseTime = time;
    }
    
    public Long getTotalTime(){
        return totalTime;
    }
    
    public String getPageName(){
        return pageName;
    }
    
    public Long getServerResponseTime(){
        return responseTime;
    }
    
    public void setObtainContentTime(Long time){
        this.obtainContent = time;
    }
    
    public Long getObtainContentTime(){
        return obtainContent;
    }
    
    public void setPerformingJSTime(Long time){
        this.performingJS = time;
    }
    
    public Long getPerformingJSTime(){
        return performingJS;
    }
    
    public String toString(){
        return "" + totalTime + "," + obtainContent + "," + responseTime + "," + performingJS;
    }
    
    public void write(PrintStream st){
        st.append("total=" + totalTime + ",content=" + obtainContent + ",serverResponse=" + responseTime + ",others=" + performingJS);
    }
}
