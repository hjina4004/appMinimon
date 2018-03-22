package com.minimon.diocian.player;

import java.util.ArrayList;

/**
 * Created by GOOD on 2018-03-14.
 */

public class WebViewInfo {
    public static volatile WebViewInfo webViewInfo;
    public WebViewInfo(){
        if(webViewInfo != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static WebViewInfo getInstance(){
        if(webViewInfo == null){
            synchronized (ConfigInfo.class){
                if(webViewInfo == null) webViewInfo = new WebViewInfo();
            }
        }
        return webViewInfo;
    }

    private String pageName;
    private String c_idx;
    private String c_id;
    private String ep_idx;
    private String category;
    private String search_tag;
    private String htmlCode;
    private String payHow;

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getC_idx() {
        return c_idx;
    }

    public void setC_idx(String c_idx) {
        this.c_idx = c_idx;
    }

    public String getC_id() {
        return c_id;
    }

    public void setC_id(String c_id) {
        this.c_id = c_id;
    }

    public String getEp_idx() {
        return ep_idx;
    }

    public void setEp_idx(String ep_idx) {
        this.ep_idx = ep_idx;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSearch_tag() {
        return search_tag;
    }

    public void setSearch_tag(String search_tag) {
        this.search_tag = search_tag;
    }

    public String getHtmlCode() {
        return htmlCode;
    }

    public void setHtmlCode(String htmlCode) {
        this.htmlCode = htmlCode;
    }

    public String getPayHow() {
        return payHow;
    }

    public void setPayHow(String payHow) {
        this.payHow = payHow;
    }
}
