package com.minimon.diocian.player;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by ICARUSUD on 2018. 3. 9..
 */

public class SearchItemList extends RealmObject {
    private RealmList<SearchItem> items;

    public RealmList<SearchItem> getItems() {
        return items;
    }

    public void setItems(RealmList<SearchItem> items) {
        this.items = items;
    }
}
