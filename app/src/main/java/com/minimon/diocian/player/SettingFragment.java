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

public class SettingFragment extends Fragment {
    private View mView;
    private final String PREF_NAME = "minimon-preference";
    private List<SettingItem> mSettingList = new ArrayList<>();
    private LinearLayout viewSetBandWidth;
    private LinearLayout viewSetData;
    private LinearLayout viewVertion;
    private SettingBandwidthAdapter adapter;
    private TextView mSetBandwidth;
    private TextView mSetData;
    private Switch mEventSwitch;
    private Switch mServiceSwitch;
    private String appVersion;
    private TextView mAppVersion;

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

        mSetData = (TextView) mView.findViewById(R.id.tv_setting_player_data);
        if(ConfigInfo.getInstance().isUseData()){
            mSetData.setText("LTE/3G");
        }else{
            mSetData.setText("WIFI");
        }
        adapter = new SettingBandwidthAdapter(getActivity(), mSettingList);
        viewSetBandWidth = view.findViewById(R.id.view_setting_set_bandwidth);
        viewSetBandWidth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setBandWidthList();
                showBandDialog();
            }
        });

        viewSetData = view.findViewById(R.id.view_setting_set_data);
        viewSetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDataList();
                showDataDialog();
            }
        });


        mEventSwitch = view.findViewById(R.id.switch_setting_event);
        mEventSwitch.setChecked(ConfigInfo.getInstance().isAlertEvent());
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

    private void showDataDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        TextView title = new TextView(getActivity());
        title.setText("데이터 설정");
        title.setGravity(Gravity.CENTER);
        title.setTextSize(16);
        title.setTextColor(Color.BLACK);

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences pref = getActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                switch (i){
                    case 0:
                        ConfigInfo.getInstance().setUseData(false);
                        mSetData.setText("WIFI");
//                        setMobileDataEnabled(getActivity(),false);
                        break;
                    case 1:
                        ConfigInfo.getInstance().setUseData(true);
                        mSetData.setText("LTE/3G");
                        break;
                }
                editor.putBoolean("useData",ConfigInfo.getInstance().isUseData());
                editor.apply();
                dialogInterface.dismiss();
            }
        }).setCustomTitle(title);
        builder.show();
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
        title.setPadding(0,52,0,52);

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
                }
                dialogInterface.dismiss();
            }
        }).setCustomTitle(title);
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
        }
        adapter.notifyDataSetChanged();
    }

    public void setMobileDataEnabled(Context context, boolean enabled) {
      //final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        try {
//            final Class conmanClass = Class.forName(conman.getClass().getName());
//            final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
//            iConnectivityManagerField.setAccessible(true);
//            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
//            final Class iConnectivityManagerClass = Class.forName(
//                    iConnectivityManager.getClass().getName());
//            final Method setMobileDataEnabledMethod = iConnectivityManagerClass
//                    .getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
//            setMobileDataEnabledMethod.setAccessible(true);
//
//            setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        }

//        Intent intent = new Intent();
//        intent.setClassName("com.android.settings",
//                "com.android.settings.Settings$DataUsageSummaryActivity");
//        startActivity(intent);

//        ConnectivityManager connectivityManager =
//                (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);
//        NetworkInfo mobileInfo =
//                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    }
}
