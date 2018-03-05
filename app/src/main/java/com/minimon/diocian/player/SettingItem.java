package com.minimon.diocian.player;

/**
 * Created by ICARUSUD on 2018. 3. 4..
 */

public class SettingItem {

    private boolean isSelect;
    private String name;
    public SettingItem(boolean isSel, String itemName){
        isSelect = isSel;
        name = itemName;
    }
    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
