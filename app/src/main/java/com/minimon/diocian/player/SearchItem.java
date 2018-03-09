package com.minimon.diocian.player;

import io.realm.RealmObject;

/**
 * Created by ICARUSUD on 2018. 3. 9..
 */

public class SearchItem extends RealmObject{
    private String history;
    private String date;

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
