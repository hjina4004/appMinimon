package com.minimon.diocian.player;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayScreenActivity extends AppCompatActivity implements PlayListItemClickListener, View.OnTouchListener{

    private MinimonEpisode minimonEpisode;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private boolean playWhenReady = false;
    private boolean inErrorState;
    private String videoUrl = "";
    private long mResumePosition = 0;
    private boolean isChangeBandWidth = false;
    private String nowBandWidth = "";

    private Dialog gestureInfoDialog;

    private FrameLayout mFullScreenButton;
    private ImageView mFullScreenIcon;
    private ImageView mScreenLock;
    private RelativeLayout mBottomMenu;
    private ImageView mShowGestureInfo;
    private LinearLayout mNowBandWidth;
    private TextView mBandWidth;
    private TextView mBandWidthAuto;
    private TextView mBandWidth480;
    private TextView mBandWidth720;
    private TextView mBandWidth1080;
    private LinearLayout mBandWidthView;
    private ImageView mPrev;
    private ImageView mPlay;
    private ImageView mNext;
    private TextView mTitle;


    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private ComponentListener componentListener;

    private int width, height= 0;
    private VideoPlayGestureDetector videoPlayGestureDetector;
    private TouchGestureDetector gestureDetector;

    private List<Drama> arrEpisode = new ArrayList<>();// = new List<Drama>();
    private PlaylistDramaAdapter epiAdapter;
    private RecyclerView rec_playing_playlist;
    LinearLayoutManager layoutManager;

//    private VerticalSeekBar brightSeekBar;
//    private VerticalSeekBar volumeSeekBar;
//    private LinearLayout view_playing_bright_seekbar;
//    private LinearLayout view_playing_volume_seekbar;

    private boolean isplaying;

    public int now_bright_status;
    public int now_volume_status;

    private int mCurrentState;
    public static final int STATE_IDLE = 300;             // idle state
    public static final int STATE_EPISODE_LIST = 301;     // episode list view state
    public static final int STATE_EXOPLAYER_CTRL = 302;   // exoPlayer controller iew state
    public static final int STATE_BRIGHT_CTRL = 303;      // bright controller vew state
    public static final int STATE_VOLUME_CTRL = 304;      // volume controller view state
    public static final int STATE_SHOW_MOVING_TIME = 305; // seek drag show

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play_screen);
        videoUrl = EpisodeInfo.getInsatnace().getVideoUrl();
        mResumePosition = EpisodeInfo.getInsatnace().getResumePosition();
        playerView = findViewById(R.id.player_view);
        componentListener = new ComponentListener();

        gestureInfoDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen){
        };

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        rec_playing_playlist = findViewById(R.id.rec_playing_playlist);
        rec_playing_playlist.setNestedScrollingEnabled(false);

        epiAdapter = new PlaylistDramaAdapter(this, arrEpisode, "list_");
        rec_playing_playlist.setAdapter(epiAdapter);
        epiAdapter.setClickListener(this);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rec_playing_playlist.setLayoutManager(layoutManager);
        mCurrentState = STATE_IDLE;
        playerView.setControllerVisibilityListener(new PlaybackControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                Log.d("GestureTag", "onVisibilityChange: " + visibility);
                if (visibility == 0 && getCurrentState() != STATE_EXOPLAYER_CTRL)
                    playerView.hideController();
            }
        });

        initializePlayer();
        initFullscreenButton();
        initData();
        sendPlaylistData();

        initVerticalSeekBar();
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    public void changeState(int state) {
        int currentState = getCurrentState();

        Log.d("ChangeState : ", String.valueOf(state));
        if (state == STATE_IDLE) {
            playerView.hideController();
            findViewById(R.id.view_playing_bright_seekbar).setVisibility(View.GONE);
            findViewById(R.id.view_playing_volume_seekbar).setVisibility(View.GONE);
            findViewById(R.id.view_playlist).setVisibility(View.GONE);
            playerView.findViewById(R.id.view_move_time).setVisibility(View.GONE);
            playerView.findViewById(R.id.view_bandwidth).setVisibility(View.GONE);
        } else if (currentState != STATE_EPISODE_LIST) {
            playerView.hideController();
            findViewById(R.id.view_playing_bright_seekbar).setVisibility(View.GONE);
            findViewById(R.id.view_playing_volume_seekbar).setVisibility(View.GONE);
            findViewById(R.id.view_playlist).setVisibility(View.GONE);
            findViewById(R.id.view_move_time).setVisibility(View.GONE);

            if (state == STATE_EPISODE_LIST) {
                findViewById(R.id.view_playlist).setVisibility(View.VISIBLE);
            } else if (state == STATE_EXOPLAYER_CTRL) {
                mCurrentState = state;
                playerView.showController();
                playerView.findViewById(R.id.exo_rew).setVisibility(View.VISIBLE);
                if(playerView.getPlayer().getPlayWhenReady()) {
                    playerView.findViewById(R.id.exo_play).setVisibility(View.GONE);
                    playerView.findViewById(R.id.exo_pause).setVisibility(View.VISIBLE);
                }
                else {
                    playerView.findViewById(R.id.exo_play).setVisibility(View.VISIBLE);
                    playerView.findViewById(R.id.exo_pause).setVisibility(View.GONE);
                }
                playerView.findViewById(R.id.exo_ffwd).setVisibility(View.VISIBLE);
                playerView.findViewById(R.id.view_move_time).setVisibility(View.GONE);
            } else if (state == STATE_BRIGHT_CTRL) {
                findViewById(R.id.view_playing_bright_seekbar).setVisibility(View.VISIBLE);
            } else if (state == STATE_VOLUME_CTRL) {
                findViewById(R.id.view_playing_volume_seekbar).setVisibility(View.VISIBLE);
            } else if (state == STATE_SHOW_MOVING_TIME){
//                playerView.showController();
//                playerView.findViewById(R.id.exo_rew).setVisibility(View.GONE);
//                playerView.findViewById(R.id.exo_play).setVisibility(View.GONE);
//                playerView.findViewById(R.id.exo_pause).setVisibility(View.GONE);
//                playerView.findViewById(R.id.exo_ffwd).setVisibility(View.GONE);
                findViewById(R.id.view_move_time).setVisibility(View.VISIBLE);
            }
        }
        mCurrentState = state;
    }

    private void initVerticalSeekBar() {
        try {
            if (Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == 1) {
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
            }

            now_bright_status = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch(Exception e){
            Log.e("Exception e "+e.getMessage(), null);
        }

        final AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        now_volume_status = audio.getStreamVolume(AudioManager.STREAM_MUSIC);


        final VerticalSeekBar vSeekBar = (VerticalSeekBar) findViewById(R.id.BrightSeekBar);
        final VerticalSeekBar volumeSeekBar = (VerticalSeekBar) findViewById(R.id.VolumeSeekBar);

        vSeekBar.setMax(255);
        vSeekBar.setProgress(now_bright_status);
        vSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 10) {
                    progress = 10;
                    vSeekBar.setProgress(progress);
                } else if (progress > 250) {
                    progress = 250;
                    vSeekBar.setProgress(progress);
                }

                Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        volumeSeekBar.setMax(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeSeekBar.setProgress(now_volume_status);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                audio.setStreamVolume(AudioManager.STREAM_MUSIC,i,0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initData(){
        minimonEpisode = new MinimonEpisode();
        minimonEpisode.setListener(new MinimonEpisode.MinimonEpisodeListener() {
            @Override
            public void onResponse(JSONObject info , String responseType) {
                try{
                    if("info".equals(responseType)){//현재 플레이할 에피소드 에이터
                        setEpisodeData(info);
                    }else{
                        setPlaylistData(info);
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    /*
    현재 플레이할 에피소드 데이터
     */
    private void sendEpisodeData(String idx){
        ContentValues values = new ContentValues();
        values.put("ep_idx",idx);
        values.put("quality","his");
        values.put("id",UserInfo.getInstance().getUID());

        minimonEpisode.info(values);
    }

    /**
     * 하단 재생목록을 불러오기
     */
    private void sendPlaylistData(){
        ContentValues values = new ContentValues();
        values.put("c_idx",EpisodeInfo.getInsatnace().getC_idx());
        values.put("start","1");
        values.put("limit",0);
        values.put("order","ASC");
        values.put("id",UserInfo.getInstance().getUID());
        minimonEpisode.list_(values);
    }

    private void setEpisodeData(JSONObject info){
        try {
            JSONArray videoArr = (JSONArray) info.getJSONObject("data").getJSONObject("list").getJSONObject("list_mp").get("video");
            JSONObject list = (JSONObject) info.getJSONObject("data").get("list");
            JSONObject videoObj = null;
            if(ConfigInfo.getInstance().getBandwidth()!= 3)
                videoObj= (JSONObject) videoArr.get(ConfigInfo.getInstance().getBandwidth());
            else
                videoObj= (JSONObject) videoArr.get(0);
            videoUrl = videoObj.getString("playUrl");
            EpisodeInfo.getInsatnace().setIdx(list.getString("idx"));
            EpisodeInfo.getInsatnace().setVideoUrl(videoUrl);
            if(!isChangeBandWidth)
                EpisodeInfo.getInsatnace().setResumePosition(0);
            else {
                isChangeBandWidth = false;
            }
            initializePlayer();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void setPlaylistData(JSONObject info){
        try{
            JSONArray jArr = (JSONArray)info.getJSONObject("data").get("list");
            arrEpisode.clear();
            for(int i=0; i<jArr.length(); i++){
                JSONObject objEpisode = (JSONObject) jArr.get(i);
                Drama episode = new Drama();
                episode.setIdx(objEpisode.getString("idx"));
                episode.setEp(objEpisode.getInt("ep"));
                episode.setContentTitle(objEpisode.getString("title"));
                episode.setThumbnailUrl(objEpisode.getString("image_url"));
                episode.setPoint(objEpisode.getString("point"));
                arrEpisode.add(episode);
            }
            epiAdapter.notifyDataSetChanged();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void initializePlayer() {
        if (player == null) {
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            TrackSelector trackSelector =
                    new DefaultTrackSelector(adaptiveTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(this,trackSelector);

            player.addListener(componentListener);
            player.setAudioDebugListener(componentListener);
            player.setVideoDebugListener(componentListener);
            playerView.setPlayer(player);

            player.setPlayWhenReady(playWhenReady);
        }

        MediaSource mediaSources = buildMediaSource(Uri.parse(videoUrl), "mp4");
        playerView.getPlayer().prepare(mediaSources, true, false);
        inErrorState = false;
        if(mResumePosition != 0){
            player.seekTo(mResumePosition);
            player.setPlayWhenReady(true);
        }

        videoPlayGestureDetector = new VideoPlayGestureDetector(this,this,width,height);
        gestureDetector = new TouchGestureDetector(this, videoPlayGestureDetector);
        gestureDetector.setListner(new TouchGestureDetector.touchListner() {
            @Override
            public void onUp() {
                videoPlayGestureDetector.onUp();
            }
        });
}

    private void releasePlayer() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            player.removeListener(componentListener);
            player.setAudioDebugListener(null);
            player.setVideoDebugListener(null);
            player.release();
            player = null;
        }
    }

    private void initFullscreenButton() {
        PlaybackControlView controlView = playerView.findViewById(R.id.exo_controller);
//        controlView.findViewById(R.id.exo_prev).setBackgroundResource(R.drawable.icon_b_prev);
//        controlView.findViewById(R.id.exo_play).setBackgroundResource(R.drawable.icon_b_play);
//        controlView.findViewById(R.id.exo_ffwd).setBackgroundResource(R.drawable.icon_b_ffwd);

        mPlay = controlView.findViewById(R.id.exo_play);
        mPlay.setImageResource(R.drawable.icon_b_play);
        mPrev = controlView.findViewById(R.id.exo_rew);
        mPrev.setImageResource(R.drawable.icon_b_prev);
        mNext = controlView.findViewById(R.id.exo_ffwd);
        mNext.setImageResource(R.drawable.icon_b_ffwd);
        mTitle = controlView.findViewById(R.id.tv_exo_title);
        mTitle.setText(EpisodeInfo.getInsatnace().getTitle());
        mTitle.setTextSize(19);
        TextView mPosition = controlView.findViewById(R.id.exo_position);
        TextView mDuration = controlView.findViewById(R.id.exo_duration);
        mDuration.setTextSize(19);
        mPosition.setTextSize(19);

        controlView.findViewById(R.id.tv_playing_playlist).setVisibility(View.VISIBLE);
        controlView.findViewById(R.id.img_playing_playlist).setVisibility(View.VISIBLE);

        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.a022_play_zoom_in));
        mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EpisodeInfo.getInsatnace().setResumePosition(Math.max(0, playerView.getPlayer().getContentPosition()));
                finish();
            }
        });
        mScreenLock = controlView.findViewById(R.id.img_exo_lock);
        mBottomMenu = controlView.findViewById(R.id.exo_view_play_info);
        mBottomMenu.setVisibility(View.VISIBLE);

        mShowGestureInfo = controlView.findViewById(R.id.img_exo_gesture_info);
        mShowGestureInfo.setVisibility(View.VISIBLE);
        mShowGestureInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openInfoDialog();
            }
        });

        mBandWidthView = controlView.findViewById(R.id.view_bandwidth);
        mBandWidth = controlView.findViewById(R.id.tv_bandwidth);
        mNowBandWidth = controlView.findViewById(R.id.view_now_bandwidth);

        switch (ConfigInfo.getInstance().getBandwidth()){
            case 0:
                nowBandWidth = "480p";
                break;
            case 1:
                nowBandWidth = "720p";
                break;
            case 2:
                nowBandWidth = "1080p";
                break;
            case 3:
                nowBandWidth = "자동";
                break;
        }
        mBandWidth.setText(nowBandWidth);
        mBandWidthAuto = controlView.findViewById(R.id.tv_bandwidth_auto);
        mBandWidth480 = controlView.findViewById(R.id.tv_bandwidth_480);
        mBandWidth720 = controlView.findViewById(R.id.tv_bandwidth_720);
        mBandWidth1080 = controlView.findViewById(R.id.tv_bandwidth_1080);
        mNowBandWidth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBandWidthView.getVisibility() == View.VISIBLE)
                    mBandWidthView.setVisibility(View.GONE);
                else
                    mBandWidthView.setVisibility(View.VISIBLE);
            }
        });
        mBandWidth480.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("480p".equals(nowBandWidth))
                    return;
                ConfigInfo.getInstance().setBandwidth(ConfigInfo.bandwidth480);
                mBandWidthView.setVisibility(View.GONE);
                nowBandWidth = "480p";
                mBandWidth.setText(nowBandWidth);
                isChangeBandWidth = true;
                mResumePosition = Math.max(0, playerView.getPlayer().getContentPosition());
                sendEpisodeData(EpisodeInfo.getInsatnace().getIdx());
            }
        });
        mBandWidth720.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("720p".equals(nowBandWidth))
                    return;
                ConfigInfo.getInstance().setBandwidth(ConfigInfo.bandwidth720);
                mBandWidthView.setVisibility(View.GONE);
                nowBandWidth = "720p";
                mBandWidth.setText(nowBandWidth);
                isChangeBandWidth = true;
                mResumePosition = Math.max(0, playerView.getPlayer().getContentPosition());
                sendEpisodeData(EpisodeInfo.getInsatnace().getIdx());
            }
        });
        mBandWidth1080.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if("1080p".equals(nowBandWidth))
                    return;
                ConfigInfo.getInstance().setBandwidth(ConfigInfo.bandwidth1080);
                mBandWidthView.setVisibility(View.GONE);
                nowBandWidth = "1080p";
                mBandWidth.setText(nowBandWidth);
                isChangeBandWidth = true;
                mResumePosition = Math.max(0, playerView.getPlayer().getContentPosition());
                sendEpisodeData(EpisodeInfo.getInsatnace().getIdx());
            }
        });
        mBandWidthAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBandWidthView.setVisibility(View.GONE);
                if("자동".equals(nowBandWidth))
                    return;
                ConfigInfo.getInstance().setBandwidth(ConfigInfo.banswidthAuto);
                nowBandWidth = "자동";
                mBandWidth.setText(nowBandWidth);
                isChangeBandWidth = true;
                mResumePosition = Math.max(0,playerView.getPlayer().getContentPosition());
                sendEpisodeData(EpisodeInfo.getInsatnace().getIdx());
            }
        });
    }


    private MediaSource buildMediaSource(Uri uri, String videoType) {
        DataSource.Factory mediaDataSourceFactory = buildDataSourceFactory(true);
        if("mp4".equals(videoType))
            return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri, null, null);
        else if("hls".equals(videoType))
            return new HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri, null, null);
        else
            return null;
    }
    public DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        DefaultBandwidthMeter bandwidthMeter = useBandwidthMeter ? BANDWIDTH_METER : null;
        return new DefaultDataSourceFactory(this, bandwidthMeter, buildHttpDataSourceFactory(true));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        DefaultBandwidthMeter bandwidthMeter = useBandwidthMeter ? BANDWIDTH_METER : null;
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "exAndroid"), bandwidthMeter);
    }

    private void openInfoDialog(){
        gestureInfoDialog.setContentView(R.layout.dialog_layout_gesture_info);
        gestureInfoDialog.findViewById(R.id.img_dialog_gesture_info_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeInfoDialog();
            }
        });
        gestureInfoDialog.show();
    }

    private void closeInfoDialog(){
        gestureInfoDialog.dismiss();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                return true;
        }
        return true;
    }

    private class ComponentListener implements ExoPlayer.EventListener, VideoRendererEventListener, AudioRendererEventListener {
        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int state) {
            String stateString;
            switch (state) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "STATE_IDLE";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "STATE_BUFFERING";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "STATE_READY";
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "STATE_ENDED";
                    break;
                default:
                    stateString = "UNKNOWN STATE";
                    break;
            }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }

        @Override
        public void onPositionDiscontinuity(int reason) {
        }


        @Override
        public void onSeekProcessed() {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        }

        @Override
        public void onAudioEnabled(DecoderCounters counters) {

        }

        @Override
        public void onAudioSessionId(int audioSessionId) {

        }

        @Override
        public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

        }

        @Override
        public void onAudioInputFormatChanged(Format format) {

        }

        @Override
        public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

        }

        @Override
        public void onAudioDisabled(DecoderCounters counters) {

        }

        @Override
        public void onVideoEnabled(DecoderCounters counters) {

        }

        @Override
        public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

        }

        @Override
        public void onVideoInputFormatChanged(Format format) {

        }

        @Override
        public void onDroppedFrames(int count, long elapsedMs) {

        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

        }

        @Override
        public void onRenderedFirstFrame(Surface surface) {

        }

        @Override
        public void onVideoDisabled(DecoderCounters counters) {

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(gestureDetector != null){
            if(!gestureDetector.onTouchEvent(event)){
                return false;
            }
        }
        super.onTouchEvent(event);
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(videoPlayGestureDetector != null){
            if(!gestureDetector.onTouchEvent(ev)){
                return false;
            }
            super.dispatchTouchEvent(ev);
        }
        return false;
    }

    @Override
    public void onClick(View v, final String idx) {
        Log.d("DetectorFlingTag","onclick");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("결제 진행");
        builder.setMessage(" 결제를 진행하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EpisodeInfo.getInsatnace().setIdx(idx);
                        sendEpisodeData(idx);
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            if(playerView!= null&& playerView.getPlayer()!=null){
                mResumePosition = Math.max(0, playerView.getPlayer().getContentPosition());
                EpisodeInfo.getInsatnace().setResumePosition(mResumePosition);
                releasePlayer();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            if(playerView!= null&& playerView.getPlayer()!=null){
                mResumePosition = Math.max(0, playerView.getPlayer().getContentPosition());
                EpisodeInfo.getInsatnace().setResumePosition(mResumePosition);
                releasePlayer();
            }
        }
    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.img_exo_lock:
//                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
            }
        }
    };
}
