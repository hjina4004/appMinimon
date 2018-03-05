package com.minimon.diocian.player;

import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ICARUSUD on 2018. 3. 4..
 */

public class SettingFragment extends Fragment {
    View mView;
    List<SettingItem> mBandWidthList = new ArrayList<>();
    LinearLayout viewSetBandWidth;
    SettingBandwidthAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mView = view;

        adapter = new SettingBandwidthAdapter(getActivity(), mBandWidthList);
        viewSetBandWidth = view.findViewById(R.id.view_setting_set_bandwidth);
        viewSetBandWidth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBandWidthList();
                showBandDialog();
            }
        });
        super.onViewCreated(view, savedInstanceState);
    }

    private void showBandDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        final AlertDialog alert;
//                = new AlertDialog.Builder(getActivity()).
        TextView title = new TextView(getActivity());
        title.setText("플레이어 기본화질");
        title.setGravity(Gravity.CENTER);
        title.setTextSize(16);
        title.setTextColor(Color.BLACK);

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ConfigInfo.getInstance().setBandwidth(i);
                dialogInterface.dismiss();
            }
        }).setCustomTitle(title);
        builder.show();
    }

    private void setBandWidthList(){
        mBandWidthList.clear();
        if(ConfigInfo.getInstance().getBandwidth() == 0){
            mBandWidthList.add(new SettingItem(true,"480p"));
            mBandWidthList.add(new SettingItem(false,"720p"));
            mBandWidthList.add(new SettingItem(false,"1080p"));
        }else if(ConfigInfo.getInstance().getBandwidth() == 1){
            mBandWidthList.add(new SettingItem(false,"480p"));
            mBandWidthList.add(new SettingItem(true,"720p"));
            mBandWidthList.add(new SettingItem(false,"1080p"));
        }else if(ConfigInfo.getInstance().getBandwidth() == 2){
            mBandWidthList.add(new SettingItem(false,"480p"));
            mBandWidthList.add(new SettingItem(false,"720p"));
            mBandWidthList.add(new SettingItem(true,"1080p"));
        }
        adapter.notifyDataSetChanged();
    }
}
