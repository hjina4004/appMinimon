package com.minimon.diocian.player;

import android.content.ContentValues;

/**
 * Created by GOOD on 2018-03-22.
 */

public class WebViewHistory {
//    private String pageName;
    private String pageUrl;
    private ContentValues content;
    private String pageType;

    public String getPageUrl() {
        return pageUrl;
    }

    public void setPageUrl(String pageUrl) {
        this.pageUrl = pageUrl;
    }

    public ContentValues getContent() {
        return content;
    }

    public void setContent(ContentValues content) {
        this.content = content;
    }

    public String getPageType() {
        return pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }
//    private String pageKey;
//    private String pageValue;
//    private String item;
//    private String how;
//    private String loc;
//    private String search_tag;


}
