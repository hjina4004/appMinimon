package com.minimon.diocian.player;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class VideoPlayScreenActivity extends AppCompatActivity {

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
    private GestureDetectorCompat gestureDetectorCompat;

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


        initializePlayer();
        initFullscreenButton();
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
//            PlaybackControlView simpleExoplayerView;
            playerView.setPlayer(player);

            player.setPlayWhenReady(playWhenReady);
        }


//        videoUrl.replace("&","&26");
        MediaSource mediaSources = buildMediaSource(Uri.parse(videoUrl), "mp4");
//        player.seekTo(currentWindow, playBackPosition);
        playerView.getPlayer().prepare(mediaSources, true, false);
        inErrorState = false;
        if(mResumePosition != 0){
            player.seekTo(mResumePosition);
            player.setPlayWhenReady(true);
        }

        videoPlayGestureDetector = new VideoPlayGestureDetector(this,this,width,height);
//        gestureDetectorCompat = new GestureDetectorCompat(playerView.getContext(),videoPlayGestureDetector);
        gestureDetector = new GestureDetector(this, videoPlayGestureDetector);
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
}
