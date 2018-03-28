package com.minimon.diocian.player;

import android.content.ContentValues;

/**
 * Created by yun-yeohyeon on 2018. 3. 28..
 */

public class EpisodeHistory {
    private String url;
    private ContentValues content;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ContentValues getContent() {
        return content;
    }

    public void setContent(ContentValues content) {
        this.content = content;
    }
}
