package com.minimon.diocian.player;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

/**
 * Created by GOOD on 2018-02-27.
 */

public class FullscreenGestureDialog extends Dialog {
    VideoPlayGestureDetector videoGestureDetector;
    GestureDetectorCompat detectorCompat;
    GestureDetector detector;
    View.OnTouchListener listener;
    public FullscreenGestureDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        detector = new GestureDetector(videoGestureDetector);
        listener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(detector.onTouchEvent(event))
                    return true;
                else
                    return false;
            }
        };
//        detectorCompat = new GestureDetectorCompat(context,videoGestureDetector);
//        ((SimpleExoPlayerView)context).findvie
//        videoGestureDetector.setListener(listener);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if(detector.onTouchEvent(event))
            return true;
        else
            return false;
    }

}
