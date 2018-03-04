package com.minimon.diocian.player;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DramaPlayActivity extends AppCompatActivity implements PlayListItemClickListener {

    private final String TAG = "DramaPlayActivity";
    private final String STATE_RESUME_WINDOW = "resumeWindow";
    private final String STATE_RESUME_POSITION = "resumePosition";
    private final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";
    private final int REQUEST_FULLSCREEN = 1;

    private NestedScrollView dramaPlayMainScrollView;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private boolean playWhenReady = false;
    private boolean inErrorState;
//    private long playBackPosition;
//    private int currentWindow;
    private String videoUrl = "";

    private boolean mExoPlayerFullscreen = false;
    private FrameLayout mFullScreenButton;
    private ImageView mFullScreenIcon;
    private TextView mEpisodeTitle;
    private ImageView mLockScreen;
    private boolean isLockSreen;
//    private Dialog mFullScreenDialog;

    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private ComponentListener componentListener;

    private TextView tv_content_title;
    private TextView tv_content_point;
    private TextView tv_episode_description;
    private TextView tv_episode_tag;

//    private boolean isnew = true;

    private int mResumeWindow;
    private long mResumePosition;
    MinimonEpisode minimonEpisode;

    //하단 재생목록, 인기목록
    LinearLayoutManager layoutManager;
    RecyclerView rc_playlist;
    List<Drama> arrEpisode = new ArrayList<>();// = new List<Drama>();
    PlaylistDramaAdapter epiAdapter;
    private String c_title;
    private String c_idx;
    private String nowEp;

    FloatingActionButton fbScrollToTop;
    FloatingActionButton fbSortLastest;
    FloatingActionButton fbSortEp;

    VideoPlayGestureDetector detector;
    GestureDetectorCompat detectorCompat;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drama_play);
        dramaPlayMainScrollView = findViewById(R.id.drama_play_main_scrollview);
        if(savedInstanceState != null){
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW);
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION);
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN);
        }
        tv_content_point = findViewById(R.id.tv_content_point);
        tv_content_title = findViewById(R.id.tv_content_title);
        tv_episode_description = findViewById(R.id.tv_episode_description);
        tv_episode_tag = findViewById(R.id.tv_episode_tag);

        componentListener = new ComponentListener();
        playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);

        rc_playlist = findViewById(R.id.rc_playlist);
        rc_playlist.setNestedScrollingEnabled(false);

        epiAdapter = new PlaylistDramaAdapter(this, arrEpisode,"info");
        rc_playlist.setAdapter(epiAdapter);
        epiAdapter.setClickListener(this);

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rc_playlist.setLayoutManager(layoutManager);

        fbScrollToTop = findViewById(R.id.fb_scroll_to_top);
        fbSortLastest = findViewById(R.id.fb_sort_lastest);
        fbSortEp = findViewById(R.id.fb_sort_episode);
        fbScrollToTop.setOnClickListener(mClickListener);
        fbSortLastest.setOnClickListener(mClickListener);
        fbSortEp.setOnClickListener(mClickListener);

        if(ConfigInfo.getInstance().getBandwidth() !=0 && ConfigInfo.getInstance().getBandwidth() !=1 && ConfigInfo.getInstance().getBandwidth() !=2){
            ConfigInfo.getInstance().setBandwidth(ConfigInfo.bandwidth480);
        }

        if(!isLockSreen)
            isLockSreen = true;
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

    private void initData(){
        minimonEpisode = new MinimonEpisode();
        minimonEpisode.setListener(new MinimonEpisode.MinimonEpisodeListener() {
            @Override
            public void onResponse(JSONObject info , String responseType) {
                try{
                    if("info".equals(responseType)) //현재 플레이할 에피소드 에이터
                        setData(info);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onClick(View v, final String idx) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("결제 진행");
        builder.setMessage(" 결제를 진행하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendEpisodeData(idx);
                        dramaPlayMainScrollView.scrollTo(0,0);
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

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.fb_scroll_to_top:
                    dramaPlayMainScrollView.scrollTo(0,0);
                    break;
                case R.id.fb_sort_episode:
                    sortEpiArr("asc");
                    break;
                case R.id.fb_sort_lastest:
                    sortEpiArr("desc");
                    break;
            }
        }
    };

    private void setData(JSONObject info){
       try{
           JSONArray videoArr = (JSONArray)info.getJSONObject("data").getJSONObject("list").getJSONObject("list_mp").get("video");
           JSONObject videoObj = (JSONObject) videoArr.get(ConfigInfo.getInstance().getBandwidth());
           JSONArray episodeArr = (JSONArray)info.getJSONObject("data").getJSONObject("list").get("list_ep");
           JSONObject episodeInformation = (JSONObject)info.getJSONObject("data").get("list");
           setEpisodeData(episodeInformation);
           setPlaylistData(episodeArr);
           videoUrl = videoObj.getString("playUrl");
           EpisodeInfo.getInsatnace().setVideoUrl(videoUrl);
           initializePlayer();
           initFullscreenButton();
       }catch (JSONException e){
           Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
           return;
       }
   }

//   private void setPlayingPlaylistData(JSONObject list){
//        try{
//            JSONArray playlistArray = (JSONArray) list.
//        }
//   }

   public static class Descending implements Comparator<Drama>{

       @Override
       public int compare(Drama o1, Drama o2) {
           return o2.getEp() - o1.getEp();
       }
   }
    public static class Asc implements Comparator<Drama>{

        @Override
        public int compare(Drama o1, Drama o2) {
            return o1.getEp() - o2.getEp();
        }
    }
   private void sortEpiArr(String type){
       if("asc".equals(type)){
           Asc asc = new Asc();
           Collections.sort(arrEpisode,asc);
       }else{
           Descending desc = new Descending();
           Collections.sort(arrEpisode, desc);
       }
       epiAdapter.notifyDataSetChanged();
   }

   private void setPlaylistData(JSONArray jarr){
       try {
           arrEpisode.clear();
           for (int i = 0; i < jarr.length(); i++) {
               JSONObject objEpisode = (JSONObject) jarr.get(i);
               Drama episode = new Drama();
               episode.setIdx(objEpisode.get("idx").toString());
               episode.setContentTitle(objEpisode.getString("title"));
               episode.setPoint(objEpisode.getString("point"));
               episode.setEp(objEpisode.getInt("ep"));
               episode.setPlayTime(objEpisode.getString("play_time"));
               episode.setHeartCount(objEpisode.getString("like_cnt"));
               episode.setPlayCount(objEpisode.getString("play_cnt"));
               episode.setThumbnailUrl(objEpisode.getString("image_url"));
               episode.setC_title(c_title);
               arrEpisode.add(episode);
           }
           epiAdapter.notifyDataSetChanged();
       }catch (JSONException e){
           e.printStackTrace();
       }
   }

   private void setEpisodeData(JSONObject obj){
       try {
           tv_content_title.setText(obj.getString("title"));
           tv_content_point.setText(obj.getString("point"));
           EpisodeInfo.getInsatnace().setTitle(obj.getString("title"));
           EpisodeInfo.getInsatnace().setC_idx(obj.getString("c_idx"));
           EpisodeInfo.getInsatnace().setIdx(obj.getString("idx"));
           c_title = obj.getString("c_title");
//           c_idx = ;
           nowEp = obj.getString("ep");
           tv_episode_description.setText(obj.getString("summary"));
           JSONArray jarr = obj.getJSONArray("list_tag");
           String tags = "";
           for(int i=0; i<jarr.length(); i++){
               JSONObject objTag = (JSONObject) jarr.get(i);
               tags += "#"+objTag.getString("tag") + " ";
           }
           tv_episode_tag.setText(tags);
       }catch (JSONException e){
           e.printStackTrace();
       }
   }

//    private void initFullscreenDialog() {
//
//        mFullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
//            public void onBackPressed() {
//                if (mExoPlayerFullscreen)
//                    closeFullscreenDialog();
//                super.onBackPressed();
//            }
//        };
//    }

//    private void openFullscreenDialog() {
//        ((ViewGroup) playerView.getParent()).removeView(playerView);
//        mFullScreenDialog.addContentView(playerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_skrink));
//        mExoPlayerFullscreen = true;
//        mFullScreenDialog.show();
//    }
//
//
//    private void closeFullscreenDialog() {
//        ((ViewGroup) playerView.getParent()).removeView(playerView);
//        ((FrameLayout) findViewById(R.id.fragment_video)).addView(playerView);
//        mExoPlayerFullscreen = false;
//        mFullScreenDialog.dismiss();
//        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_expand));
//    }

    private void initFullscreenButton() {
        PlaybackControlView controlView = playerView.findViewById(R.id.exo_controller);
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goFullScreen();
            }
        });
        mEpisodeTitle = controlView.findViewById(R.id.tv_exo_title);
        mEpisodeTitle.setText(EpisodeInfo.getInsatnace().getTitle());
        mLockScreen = controlView.findViewById(R.id.img_exo_lock);
        if(isLockSreen)
            mLockScreen.setImageResource(R.mipmap.a022_play_b_lock_on);
        else
            mLockScreen.setImageResource(R.mipmap.a022_play_b_lock_off);
        mLockScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isLockSreen){//잠겨있던 잠금 풀 경우
                    DramaPlayActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    mLockScreen.setImageResource(R.mipmap.a022_play_b_lock_off);
                    isLockSreen = !isLockSreen;
                }else{ //다시 잠글경우
                    DramaPlayActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                    mLockScreen.setImageResource(R.mipmap.a022_play_b_lock_on);
                    isLockSreen = !isLockSreen;
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG+"Test","OnStart");
        if (Util.SDK_INT > 23) {
//            initFullscreenDialog();

            initData();
//            initFullscreenButton();
            if(EpisodeInfo.getInsatnace().getIdx()==null || EpisodeInfo.getInsatnace().getIdx().isEmpty())
                sendEpisodeData("645");
            else
                sendEpisodeData(EpisodeInfo.getInsatnace().getIdx());
//            if(mExoPlayerFullscreen){
//                ((ViewGroup) playerView.getParent()).removeView(playerView);
//                mFullScreenDialog.addContentView(playerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_skrink));
//                mFullScreenDialog.show();
//            }
//            initFullscreenButton();
//            initFullscreenDialog();
//            ContentValues values = new ContentValues();
//            values.put("ep_idx","645");
//            values.put("quality","his");
//            values.put("id",UserInfo.getInstance().getUID());
//            minimonEpisode.info(values);
//            if(mExoPlayerFullscreen){
//                ((ViewGroup) playerView.getParent()).removeView(playerView);
//                mFullScreenDialog.addContentView(playerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_skrink));
//                mFullScreenDialog.show();
//            }
//            isnew = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG+"Test","OnResume");
        if (Util.SDK_INT <= 23) {
//            initFullscreenDialog();

            initData();

            if(EpisodeInfo.getInsatnace().getIdx()==null || EpisodeInfo.getInsatnace().getIdx().isEmpty())
                sendEpisodeData("645");
            else
                sendEpisodeData(EpisodeInfo.getInsatnace().getIdx());
//            if(mExoPlayerFullscreen){
//                ((ViewGroup) playerView.getParent()).removeView(playerView);
//                mFullScreenDialog.addContentView(playerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//                mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_skrink));
//                mFullScreenDialog.show();
//            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG+"Test","OnPause");
        if (Util.SDK_INT <= 23) {
            if(playerView!= null&& playerView.getPlayer()!=null){
                mResumeWindow = playerView.getPlayer().getCurrentWindowIndex();
                mResumePosition = Math.max(0, playerView.getPlayer().getContentPosition());
                EpisodeInfo.getInsatnace().setResumePosition(mResumePosition);
                releasePlayer();

            }
//            if (mFullScreenDialog != null)
//                mFullScreenDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG+"Test","OnStop");
        if (Util.SDK_INT > 23) {
            if(playerView!= null&& playerView.getPlayer()!=null){
                mResumeWindow = playerView.getPlayer().getCurrentWindowIndex();
                mResumePosition = Math.max(0, playerView.getPlayer().getContentPosition());
                releasePlayer();
            }
//            if (mFullScreenDialog != null)
//                mFullScreenDialog.dismiss();
        }
    }

    private void goFullScreen(){
        Intent intent = new Intent(DramaPlayActivity.this, VideoPlayScreenActivity.class);
        startActivity(intent);
    }

    private void initializePlayer() {
        if (player == null) {
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            TrackSelector trackSelector =
                    new DefaultTrackSelector(adaptiveTrackSelectionFactory);
//            ExoPlayerFactory.newSimpleInstance(getActivity(),trackSelector);
            player = ExoPlayerFactory.newSimpleInstance(this,trackSelector);
//                    ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(getActivity()),
//                    new DefaultTrackSelector(adaptiveTrackSelectionFactory), new DefaultLoadControl());

            player.addListener(componentListener);
            player.setAudioDebugListener(componentListener);
            player.setVideoDebugListener(componentListener);
//            PlaybackControlView simpleExoplayerView;
            playerView.setPlayer(player);

            player.setPlayWhenReady(playWhenReady);
        }


//        videoUrl.replace("&","&26");
        MediaSource mediaSources = buildMediaSource(Uri.parse(videoUrl), "mp4");
//        player.seekTo(currentWindow, playBackPosition);
        playerView.getPlayer().prepare(mediaSources, true, false);
        inErrorState = false;
        if(EpisodeInfo.getInsatnace().getResumePosition() != 0){
            playerView.getPlayer().seekTo(EpisodeInfo.getInsatnace().getResumePosition());
            playerView.getPlayer().setPlayWhenReady(true);
        }
    }

    private void releasePlayer() {
        if (player != null) {
            mResumeWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.removeListener(componentListener);
            player.setAudioDebugListener(null);
            player.setVideoDebugListener(null);
            player.release();
            player = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri, String videoType) {
//        Uri mp4VideoUri = uri;
//        DefaultBandwidthMeter bandwidthMeter1 = new DefaultBandwidthMeter();
//        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,Util.getUserAgent(getActivity(), "yourApplicationName"),
//                bandwidthMeter1); ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
//        MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri,dataSourceFactory, extractorsFactory, null, null);
//        player.prepare(videoSource); player_view.setUseController(false);

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
            Log.d(TAG, "state [" + playWhenReady + ", " + stateString + "]");
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            inErrorState = true;
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            if (inErrorState) {
                // This will only occur if the user has performed a seek whilst in the error state. Update
                // the resume position so that if the user then retries, playback will resume from the
                // position to which they seeked.
//                updateResumePosition();
            }
        }


        @Override
        public void onSeekProcessed() {
            Log.d(TAG, "seekProcessed");
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            Log.d(TAG, "playbackParameters " + String.format(
                    "[speed=%.2f, pitch=%.2f]", playbackParameters.speed, playbackParameters.pitch));
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
