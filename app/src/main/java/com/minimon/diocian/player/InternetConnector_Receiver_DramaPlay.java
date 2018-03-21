package com.minimon.diocian.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by GOOD on 2018-03-21.
 */

public class InternetConnector_Receiver_DramaPlay extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try{
//            boolean isWifiConnected =
//            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//            DramaPlayActivity dac = (DramaPlayActivity) intent;
//            boolean isDramaplayActive = new DramaPlayActivity().getIsActive();

//            if(networkInfo != null && networkInfo.getType() != ConnectivityManager.TYPE_WIFI){
//
//            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
