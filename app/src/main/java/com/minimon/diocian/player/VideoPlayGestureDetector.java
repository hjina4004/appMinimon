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
//    private Path volumePath;
    private RectF volumeRectf;
//    private Path brightPath;
    private RectF brightRectf;

    private boolean isShowBrightSeekBar = false;
    private boolean isShowVolumeSeekBar = false;
    private boolean isActivePlaylist = false;

    private SimpleExoPlayerView playerView;
    private int doub = 0;
    private int moveCount = 0;
    private long currentTime;
    private boolean isScroll = false;

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
        int brightnessValue = Settings.System.getInt(
                mContext.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS,
                0
        );
        videoActivity = activity;
        playerView = activity.findViewById(R.id.player_view);

        mWidth = width;
        mHeight = (int) (height - convertDpToPixel(60.0f, mContext));

        brightRectf = new RectF(0, 0, mWidth/6, mHeight);
        volumeRectf = new RectF(mWidth/6*5, 0, mWidth, mHeight);
//
//        brightPath = new Path();
//        volumePath = new Path();
//        brightRectf = new RectF();
//        volumeRectf = new RectF();
//        brightPath.moveTo(0,0);
//        brightPath.moveTo(0,height);
//        brightPath.moveTo(width/5,height);
//        brightPath.moveTo(width/5,200);
//        brightPath.close();
//
//        volumePath.moveTo(width/4*3,0);
//        volumePath.moveTo(width/4*3,height);
//        volumePath.moveTo(width,height);
//        volumePath.moveTo(width,0);
//        volumePath.close();
//
//        brightPath.computeBounds(brightRectf,true);
//        volumePath.computeBounds(volumeRectf,true);

//        bottomBarHeight = videoActivity.findViewById(R.id.exo_view_play_info).getHeight();
//        playlistHeight = videoActivity.findViewById(R.id.rec_playing_playlist).getHeight();
    }

    private boolean isViewContains(View view, int rx, int ry) {
        if(view.getVisibility() == View.GONE)
            return false;
        int[] l = new int[2];
        view.getLocationOnScreen(l);
        int x = l[0];
        int y = l[1];
        int w = view.getWidth();
        int h = view.getHeight();
        boolean returnValue = true;

        if (rx < x || rx > x + w || ry < y || ry > y + h) {
            returnValue = false;
        }
        Log.d("isViewContains",String.valueOf(returnValue));
        return returnValue;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        int currentState = videoActivity.getCurrentState();
        boolean inBrightCtrl = brightRectf.contains(e.getX(), e.getY());
        boolean inVolumeCtrl = volumeRectf.contains(e.getX(), e.getY());
        if (inBrightCtrl) {
            videoActivity.changeState(VideoPlayScreenActivity.STATE_BRIGHT_CTRL);
            return false;
        } else if (inVolumeCtrl) {
            videoActivity.changeState(VideoPlayScreenActivity.STATE_VOLUME_CTRL);
            return false;
        }
        if (currentState > VideoPlayScreenActivity.STATE_IDLE) {
            if(!isViewContains(playerView.findViewById(R.id.exo_ffwd),(int)e.getX(),(int) e.getY()) &&
                    !isViewContains(playerView.findViewById(R.id.exo_rew),(int)e.getX(),(int)e.getY()) &&
                    !isViewContains(playerView.findViewById(R.id.view_now_bandwidth),(int)e.getX(),(int)e.getY()) &&
                    !isViewContains(playerView.findViewById(R.id.exo_play),(int)e.getX(),(int)e.getY()) &&
                    !isViewContains(playerView.findViewById(R.id.exo_pause),(int)e.getX(),(int)e.getY())){
                videoActivity.changeState(VideoPlayScreenActivity.STATE_IDLE);
            }
            return false;
        } else if (currentState != VideoPlayScreenActivity.STATE_SHOW_MOVING_TIME){
            videoActivity.changeState(VideoPlayScreenActivity.STATE_EXOPLAYER_CTRL);
        }

//        if(videoActivity.findViewById(R.id.view_playing_bright_seekbar).getVisibility() ==View.VISIBLE) {
//            videoActivity.findViewById(R.id.view_playing_bright_seekbar).setVisibility(View.GONE);
//            isShowBrightSeekBar = false;
//            return false;
//        }
//        if(videoActivity.findViewById(R.id.view_playing_volume_seekbar).getVisibility() == View.VISIBLE) {
//            videoActivity.findViewById(R.id.view_playing_volume_seekbar).setVisibility(View.GONE);
//            isShowVolumeSeekBar = false;
//            return false;
//        }
//
//        if(brightRectf.contains(e.getX(),e.getY()) && !isActivePlaylist) {
//            videoActivity.findViewById(R.id.view_playing_bright_seekbar).setVisibility(View.VISIBLE);
//            isShowBrightSeekBar = true;
//        }
//        else if(volumeRectf.contains(e.getX(),e.getY()) && !isActivePlaylist) {
//            videoActivity.findViewById(R.id.view_playing_volume_seekbar).setVisibility(View.VISIBLE);
//            isShowVolumeSeekBar = true;
//        }
        return true;
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
        if(isScroll){
            isScroll = !isScroll;
            playerView.getPlayer().setPlayWhenReady(true);
            playerView.findViewById(R.id.exo_rew).setVisibility(View.GONE);
            playerView.findViewById(R.id.exo_pause).setVisibility(View.GONE);
//            playerView.findViewById(R.id.view_move_time).setVisibility(View.GONE);

            playerView.findViewById(R.id.exo_ffwd).setVisibility(View.GONE);
//            playerView.findViewById(R.id.view_move_time).setVisibility(View.GONE);
        }
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d("GestureTag", "onScroll distanceX="+distanceX+", distanceY= "+distanceY);
        float min_distance = 30;
        int currentState = videoActivity.getCurrentState();
        Log.d("absDistance",String.valueOf(Math.abs(distanceX)));
        if (Math.abs(distanceX) > 1) {    // HORIZONTAL SCROLL
            if(Math.abs(distanceX) > 1) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    controlPlayTime(distanceX);
                } else {
//                // not long enough swipe...
                }
            }
        } else {                                            // VERTICAL SCROLL
            boolean inBrightCtrl = brightRectf.contains(e1.getX(), e1.getY());
            boolean inVolumeCtrl = volumeRectf.contains(e1.getX(), e1.getY());

            if (Math.abs(distanceY) > min_distance) {
                if (distanceY > 0) {                 // Bottom To Top Swipe
                    if (inBrightCtrl) {
                        videoActivity.changeState(VideoPlayScreenActivity.STATE_BRIGHT_CTRL);
                        return false;
                    } else if (inVolumeCtrl) {
                        videoActivity.changeState(VideoPlayScreenActivity.STATE_VOLUME_CTRL);
                        return false;
                    }
                }
            } else {
                // not long enough swipe...
            }
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//        Log.d("FlingVelocity e1:",String.valueOf(e1.getY()));
//        Log.d("FlingVelocity e2:",String.valueOf(e2.getY()));
//        if(e1.getY()>mHeight-50 ) {
//            isActivePlaylist = true;
//            videoActivity.findViewById(R.id.view_playing_volume_seekbar).setVisibility(View.GONE);
//            videoActivity.findViewById(R.id.view_playing_bright_seekbar).setVisibility(View.GONE);
//            isShowVolumeSeekBar = false;
//            isShowBrightSeekBar = false;
//            controlBottomMenu(e1.getY(), e2.getY(), isActivePlaylist);
//        }
//        if(e1.getY() > mHeight-320 && (velocityX<velocityY && velocityX>0)) {
//            isActivePlaylist = false;
//            controlBottomMenu(e1.getY(), e2.getY(), isActivePlaylist);
//            return false;
//        }

        return true;
    }

    private void controlPlayTime(float x1){
        long movingTime = 0;
        if(isScroll)
            videoActivity.changeState(VideoPlayScreenActivity.STATE_SHOW_MOVING_TIME);
        isScroll = true;
        Log.d("distanceXControl",String.valueOf(x1));

//        doub = Math.round(x1);
//        movingTime = doub*1000;
//
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
        String moving ="";
        if(moving.isEmpty()){
            if (x1 > 1){
                moving = "-";
            }else{
                moving = "+";
            }
        }

        if(doub == 60){
            tv_now_moving_time.setText("1:00");
        }else if (doub < 60){
            tv_now_moving_time.setText(moving+"0:"+String.valueOf(doub));
        }
        tv_now_playtime.setText(now_minute + ":"+now_sec);
        Thread thread = new Thread(){
            @Override
            public void run() {
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        };
        tv_now_moving_time.setText("");
        tv_now_playtime.setText("");
//        try {

//
//        }catch (InterruptedException e){
//
//        }
//        tv_now_moving_time.setVisibility(View.GONE);
//        tv_now_playtime.setVisibility(View.GONE);
    }

    private void controlBottomMenu(float y1, float y2, boolean isShow){
        if(y1>y2 && isShow){
            videoActivity.findViewById(R.id.view_playlist).setVisibility(View.VISIBLE);
            videoActivity.findViewById(R.id.exo_view_play_info).setVisibility(View.GONE);
        }
        if(y2>y1 && !isShow){
            Log.d("DetectorFlingTag","fling");
            videoActivity.findViewById(R.id.view_playlist).setVisibility(View.GONE);
            videoActivity.findViewById(R.id.exo_view_play_info).setVisibility(View.VISIBLE);
        }
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
//                    brightSeekBar.setProgress(brightnessValue);
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
//                brightSeekBar.setProgress(brightnessValue);
            }
        }
    }


    private void controlMediaVolume(float y1, float y2){
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(currentVolume <= 15) {
            if(currentVolume == 15)
                return;
            if (y1 > y2) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
            } else {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
            }
        }
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        Log.d("TAGVOLUME", String.valueOf(currentVolume));
//        volumeSeekBar.setProgress(currentVolume);
    }
}
