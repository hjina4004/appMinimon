package com.minimon.diocian.player;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ICARUSUD on 2018. 3. 4..
 */

public class SettingBandwidthAdapter extends BaseAdapter {
    List<SettingItem> mList;
    Activity mActivity;
    public SettingBandwidthAdapter(Activity activity, List<SettingItem> list){
        mActivity = activity;
        mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_setting,null);
        }
        final SettingItem item = mList.get(i);
        TextView tv_setting = (TextView) view.findViewById(R.id.tv_setting_list_item);
        ImageView img_setting_select = (ImageView) view.findViewById(R.id.img_setting_list_item);
        tv_setting.setText(item.getName());
        if(!item.isSelect()){
            img_setting_select.setImageResource(0);
        }else{
            img_setting_select.setImageResource(R.mipmap.a001_intro_tl);
        }
        return view;
    }
}
