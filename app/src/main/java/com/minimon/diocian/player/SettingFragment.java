package com.minimon.diocian.player;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ICARUSUD on 2018. 3. 4..
 */

public class SettingFragment extends Fragment implements MainActivity.onKeypressListenr{
    private View mView;
    private final String PREF_NAME = "minimon-preference";
    private List<SettingItem> mSettingList = new ArrayList<>();
    private LinearLayout viewSetBandWidth;
    private LinearLayout viewSetData;
    private LinearLayout viewVertion;
    private SettingBandwidthAdapter adapter;
    private TextView mSetBandwidth;
//    private TextView mSetData;
    private Switch mEventSwitch;
    private Switch mServiceSwitch;
    private String appVersion;
    private TextView mAppVersion;
    private Switch mLteSwitch;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mView = view;

        mSetBandwidth = (TextView) mView.findViewById(R.id.tv_setting_player_bandwidth);
        switch (ConfigInfo.getInstance().getBandwidth()){
            case 0:
                mSetBandwidth.setText("480p");
                break;
            case 1:
                mSetBandwidth.setText("720p");
                break;
            case 2:
                mSetBandwidth.setText("1080p");
                break;
            case 3:
                mSetBandwidth.setText("자동");
                break;
        }

//        mSetData = (TextView) mView.findViewById(R.id.tv_setting_player_data);
//        if(ConfigInfo.getInstance().isUseData()){
//            mSetData.setText("LTE/3G");
//        }else{
//            mSetData.setText("WIFI");
//        }
        adapter = new SettingBandwidthAdapter(getActivity(), mSettingList);
        viewSetBandWidth = view.findViewById(R.id.view_setting_set_bandwidth);
        viewSetBandWidth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBandWidthList();
                showBandDialog();
            }
        });

//        viewSetData = view.findViewById(R.id.view_setting_set_data);
//        viewSetData.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                setDataList();
//                showDataDialog();
//            }
//        });


        mEventSwitch = view.findViewById(R.id.switch_setting_event);
        mEventSwitch.setChecked(ConfigInfo.getInstance().isAlertEvent());
        mLteSwitch = view.findViewById(R.id.switch_setting_use_lte);
        mLteSwitch.setChecked(ConfigInfo.getInstance().isUseData());
        mServiceSwitch = view.findViewById(R.id.switch_setting_service);
        mEventSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mEventSwitch.setChecked(b);
                Log.d("SWITCHTAG",String.valueOf(b));
                SharedPreferences pref = getActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("alertEvent",b);
                editor.apply();
                ConfigInfo.getInstance().setAlertEvent(b);
            }
        });
        mLteSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mLteSwitch.setChecked(isChecked);
                ConfigInfo.getInstance().setUseData(isChecked);
                SharedPreferences pref = getActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("useData",ConfigInfo.getInstance().isUseData());
                editor.apply();
            }
        });

        try{
            PackageInfo i = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            appVersion = i.versionName;
        } catch(PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        viewVertion = view.findViewById(R.id.view_setting_app_version);
        mAppVersion = view.findViewById(R.id.tv_setting_app_version);
        mAppVersion.setText("ver "+appVersion);

        super.onViewCreated(view, savedInstanceState);
    }

//    private void showDataDialog(){
//        LayoutInflater inflater = LayoutInflater.from(getActivity());
//        View customTitle = inflater.inflate(R.layout.custom_title_dialog, null);
//        TextView title = customTitle.findViewById(R.id.customtitlebar);
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
////        TextView title = new TextView(getActivity());
//        title.setText("데이터 설정");
////        title.setGravity(Gravity.CENTER);
////        title.setTextSize(16);
////        title.setTextColor(Color.BLACK);
////        title.setPadding(0,30,0,20);
//
////        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
////            @Override
////            public void onClick(DialogInterface dialogInterface, int i) {
////                SharedPreferences pref = getActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
////                SharedPreferences.Editor editor = pref.edit();
////                switch (i){
////                    case 0:
////                        ConfigInfo.getInstance().setUseData(false);
////                        mSetData.setText("WIFI");
//////                        setMobileDataEnabled(getActivity(),false);
////                        break;
////                    case 1:
////                        ConfigInfo.getInstance().setUseData(true);
////                        mSetData.setText("LTE/3G");
////                        break;
////                }
////                editor.putBoolean("useData",ConfigInfo.getInstance().isUseData());
////                editor.apply();
////                dialogInterface.dismiss();
////            }
////        }).setCustomTitle(customTitle);
////        builder.show();
//    }

    private void showBandDialog(){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View customTitle = inflater.inflate(R.layout.custom_title_dialog, null);
        TextView title = customTitle.findViewById(R.id.customtitlebar);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        final AlertDialog alert;
//                = new AlertDialog.Builder(getActivity()).
//        TextView title = new TextView(getActivity());
        title.setText("플레이어 기본화질");
//        title.setGravity(Gravity.CENTER);
//        title.setTextSize(16);
//        title.setTextColor(Color.BLACK);
//        title.setPadding(0,52,0,52);

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences pref = getActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                ConfigInfo.getInstance().setBandwidth(i);
                editor.putInt("BandWidth",ConfigInfo.getInstance().getBandwidth());
                editor.apply();
                switch (i){
                    case 0:
                        mSetBandwidth.setText("480p");
                        break;
                    case 1:
                        mSetBandwidth.setText("720p");
                        break;
                    case 2:
                        mSetBandwidth.setText("1080p");
                        break;
                    case 3:
                        mSetBandwidth.setText("자동");
                        break;
                }
                dialogInterface.dismiss();
            }
        }).setCustomTitle(customTitle);
        builder.show();
    }

    private void setDataList(){
        mSettingList.clear();
        if(ConfigInfo.getInstance().isUseData()){
            mSettingList.add(new SettingItem(false,"WIFI"));
            mSettingList.add(new SettingItem(true,"LTE/3G"));
        }else{
            mSettingList.add(new SettingItem(true,"WIFI"));
            mSettingList.add(new SettingItem(false,"LTE/3G"));
        }
        adapter.notifyDataSetChanged();
    }

    private void setBandWidthList(){
        mSettingList.clear();
        if(ConfigInfo.getInstance().getBandwidth() == 0){
            mSettingList.add(new SettingItem(true,"480p"));
            mSettingList.add(new SettingItem(false,"720p"));
            mSettingList.add(new SettingItem(false,"1080p"));
            mSettingList.add(new SettingItem(false,"자동"));
        }else if(ConfigInfo.getInstance().getBandwidth() == 1){
            mSettingList.add(new SettingItem(false,"480p"));
            mSettingList.add(new SettingItem(true,"720p"));
            mSettingList.add(new SettingItem(false,"1080p"));
            mSettingList.add(new SettingItem(false,"자동"));
        }else if(ConfigInfo.getInstance().getBandwidth() == 2){
            mSettingList.add(new SettingItem(false,"480p"));
            mSettingList.add(new SettingItem(false,"720p"));
            mSettingList.add(new SettingItem(true,"1080p"));
            mSettingList.add(new SettingItem(false,"자동"));
        }else if(ConfigInfo.getInstance().getBandwidth() == 3){
            mSettingList.add(new SettingItem(false,"480p"));
            mSettingList.add(new SettingItem(false,"720p"));
            mSettingList.add(new SettingItem(false,"1080p"));
            mSettingList.add(new SettingItem(true,"자동"));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity activity = (MainActivity) getActivity();
        activity.setOnKeypressListener(this);
    }

    @Override
    public void onBack() {
        MainActivity activity = (MainActivity) getActivity();
        activity.setOnKeypressListener(null);
        activity.onBackPressed();
    }
}
