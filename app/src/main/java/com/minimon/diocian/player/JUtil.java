package com.minimon.diocian.player;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import java.util.List;


public class JUtil {
    public interface JUtilListener {
        // These methods are the different events and need to pass relevant arguments with the event
        void callback(int id);
    }

    public void alertNotice(Context context, String str, final JUtilListener listener) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setCancelable(false);
        dialog.setMessage(str);
        dialog.setPositiveButton(R.string.notice_close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (listener != null)
                    listener.callback(id);
            }
        });

        final AlertDialog alert = dialog.create();
        alert.show();
    }

    public void showList(Context context, List<SettingItem> list, final JUtilListener listener){

    }
}
