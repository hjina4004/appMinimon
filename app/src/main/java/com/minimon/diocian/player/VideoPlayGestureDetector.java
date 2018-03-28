package com.minimon.diocian.player;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

/**
 * Created by GOOD on 2018-02-26.
 */

public class VideoPlayGestureDetector implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, View.OnTouchListener{
    Context mContext;
    VideoPlayScreenActivity videoActivity;

    int mWidth, mHeight = 0;

    private SimpleExoPlayerView playerView;
    private int doub = 0;
    private long currentTime;
    private boolean isScroll = false;
    private boolean isShowController = false;

    public VideoPlayGestureDetectorListener mListener;
//
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_UP){
            Log.d("ActionUp","real");
        }
        return true;
    }

    public interface VideoPlayGestureDetectorListener{

    }

    public void setListener(VideoPlayGestureDetectorListener listener){
        mListener = listener;
    }

    public float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public VideoPlayGestureDetector(Context context, VideoPlayScreenActivity activity, int width, int height){
        mContext = context;
        videoActivity = activity;
        playerView = activity.findViewById(R.id.player_view);

        mWidth = width;
        mHeight = (int) (height - convertDpToPixel(60.0f, mContext));
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d("VideoPlayGestureDetect", "onSingleTapConfirmed");
        isShowController = !isShowController;
        if(isShowController){
            playerView.hideController();
        }else{
            playerView.showController();
        }
        return true;
    }

    public boolean isShowController(){
        return isShowController;
    }

    public void setShowController(boolean controller){
        isShowController = controller;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        currentTime = playerView.getPlayer().getCurrentPosition();
        doub = 0;
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d("OnSingleTapUp", "in Scroll");
        return true;
    }

    public void onUp(){
        if(isScroll ){
            isScroll = !isScroll;
            playerView.getPlayer().setPlayWhenReady(true);
            playerView.findViewById(R.id.exo_rew).setVisibility(View.GONE);
            playerView.findViewById(R.id.exo_pause).setVisibility(View.GONE);

            playerView.findViewById(R.id.exo_ffwd).setVisibility(View.GONE);
            ((VideoPlayScreenActivity) mContext)
                    .runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           final Handler mHander = new Handler(){
                               @Override
                               public void handleMessage(Message msg) {
                                   videoActivity.findViewById(R.id.view_move_time).setVisibility(View.GONE);
                               }
                           };
                           new Handler().postDelayed(new Runnable() {
                               @Override
                               public void run() {
                                   videoActivity.findViewById(R.id.view_move_time).setVisibility(View.VISIBLE);
                                   mHander.sendEmptyMessage(0);
                               }
                           },1000);

                       }
                   }
            );

        }
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d("VideoPlayGestureDetect", "onScroll");
        Log.d("GestureTag", "onScroll distanceX="+distanceX+", distanceY= "+distanceY);
        float min_distance = 30;
        Log.d("absDistance",String.valueOf(Math.abs(distanceX)));
            if(Math.abs(distanceX) > min_distance && Math.abs(distanceY) < min_distance) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    controlPlayTime(distanceX);
                }
            }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    private void controlPlayTime(final float x1){
        long movingTime = 0;
        isShowController = false;
        if(isScroll) {
            videoActivity.changeState(VideoPlayScreenActivity.STATE_SHOW_MOVING_TIME);
        }
        isScroll = true;
        playerView.hideController();
        Log.d("distanceXControl",String.valueOf(x1));

        doub+=1;
        if(doub>60)
            return;
        if(x1 > 1){
            movingTime = doub*1000;
            playerView.getPlayer().setPlayWhenReady(false);
            playerView.getPlayer().seekTo(currentTime-movingTime);
        }else if(x1<-1){
            movingTime = doub*1000;
            playerView.getPlayer().setPlayWhenReady(false);
            playerView.getPlayer().seekTo(currentTime+movingTime);
        }
        Log.d("doub",String.valueOf(doub));

        TextView tv_now_playtime = (TextView) videoActivity.findViewById(R.id.tv_now_playtime);
        TextView tv_now_moving_time = (TextView) videoActivity.findViewById(R.id.tv_now_moving_time);
        int now_playtime = (int)playerView.getPlayer().getCurrentPosition()/1000;
        int now_minute = now_playtime/60;
        int now_sec = now_playtime%60;

        if(doub == 60){
            tv_now_moving_time.setText("1:00");
        }else if (doub < 60){
            tv_now_moving_time.setText("0:"+String.valueOf(doub));
        }
        tv_now_playtime.setText(now_minute + ":"+now_sec);
    }

    private void controlBright(float y1, float y2) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(mContext)) {
                int brightnessValue = Settings.System.getInt(
                        mContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS,
                        0
                );
                if(brightnessValue <= 255 && brightnessValue>= 0) {
                    if (y1 > y2) {
                        if (y1 - y2 > 10)
                            brightnessValue += 8;
                    } else {
                        if (y2 - y1 > 10)
                            brightnessValue -= 8;
                    }
                    if (brightnessValue > 255) brightnessValue = 255;
                    if (brightnessValue < 0) brightnessValue = 0;
                    Settings.System.putInt(mContext.getContentResolver(),
                            Settings.System.SCREEN_BRIGHTNESS,
                            brightnessValue);
                }
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + videoActivity.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        } else {
            int brightnessValue = Settings.System.getInt(
                    mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS,
                    0
            );
            if(brightnessValue <= 255 && brightnessValue>= 0) {
                if (y1 > y2) {
                    if (y1 - y2 > 10)
                        brightnessValue += 8;
                } else {
                    if (y2 - y1 > 10)
                        brightnessValue -= 8;
                }
                if (brightnessValue > 255) brightnessValue = 255;
                if (brightnessValue < 0) brightnessValue = 0;
                Settings.System.putInt(mContext.getContentResolver(),
                        Settings.System.SCREEN_BRIGHTNESS,
                        brightnessValue);
            }
        }
    }

}
