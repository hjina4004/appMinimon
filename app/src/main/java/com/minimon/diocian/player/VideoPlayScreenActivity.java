package com.minimon.diocian.player;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
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
import android.widget.FrameLayout;
import android.widget.ImageView;

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

public class VideoPlayScreenActivity extends AppCompatActivity implements PlayListItemClickListener{

    private MinimonEpisode minimonEpisode;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private boolean playWhenReady = false;
    private boolean inErrorState;
    private String videoUrl = "";
    private long mResumePosition = 0;

    private FrameLayout mFullScreenButton;
    private ImageView mFullScreenIcon;

    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private ComponentListener componentListener;

    private int width, height= 0;
    private VideoPlayGestureDetector videoPlayGestureDetector;
    private GestureDetector gestureDetector;

    private List<Drama> arrEpisode = new ArrayList<>();// = new List<Drama>();
    private PlaylistDramaAdapter epiAdapter;
    private RecyclerView rec_playing_playlist;
    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play_screen);
        videoUrl = EpisodeInfo.getInsatnace().getVideoUrl();
        mResumePosition = EpisodeInfo.getInsatnace().getResumePosition();
        playerView = findViewById(R.id.player_view);
        componentListener = new ComponentListener();

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

        initializePlayer();
        initFullscreenButton();
        initData();
        sendPlaylistData();
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
            JSONObject videoObj = (JSONObject) videoArr.get(0);
            videoUrl = videoObj.getString("playUrl");
            EpisodeInfo.getInsatnace().setVideoUrl(videoUrl);
            EpisodeInfo.getInsatnace().setResumePosition(0);
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
        gestureDetector = new GestureDetector(this, videoPlayGestureDetector);
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
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_fullscreen_skrink));
        mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EpisodeInfo.getInsatnace().setResumePosition(Math.max(0, playerView.getPlayer().getContentPosition()));
                finish();
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
        super.onTouchEvent(event);
        Log.d("VideoPlayerActivity",event.toString());
//        gestureDetector.onTouchEvent(event);
        if(gestureDetector != null){
            return gestureDetector.onTouchEvent(event);
        }
//        return super.onTouchEvent(event);
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(videoPlayGestureDetector != null){
            gestureDetector.onTouchEvent(ev);
            super.dispatchTouchEvent(ev);
            return true;
        }
        return false;
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
}
