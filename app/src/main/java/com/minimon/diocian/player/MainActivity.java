package com.minimon.diocian.player;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener{

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final String TAG = "MainActivity";

    // for Google
    private static final int GOOGLE_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;

//    private SimpleExoPlayer player;
//    private SimpleExoPlayerView playerView;
//    private ComponentListener componentListener;
//
//    private long playBackPosition;
//    private int currentWindow;
//    private boolean playWhenReady = true;
//    private boolean inErrorState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        componentListener = new ComponentListener();
//        playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setBackgroundColor(getResources().getColor(R.color.colorBaseBG));

        initGoogle();

        Log.v(TAG, "User Info --- " + UserInfo.getInstance().getData());
        viewUserInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            long tempTime = System.currentTimeMillis();
            long intervalTime = tempTime - backPressedTime;

            if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
                super.onBackPressed();
            } else {
                backPressedTime = tempTime;
                Toast.makeText(getApplicationContext(), R.string.notice_exit_app, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        if (Util.SDK_INT > 23) {
//            initializePlayer();
//        }
//    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        hideSytemUi();
//        if (Util.SDK_INT <= 23 || player == null) {
//            initializePlayer();
//        }
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        if (Util.SDK_INT <= 23) {
//            releasePlayer();
//        }
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//        if (Util.SDK_INT > 23) {
//            releasePlayer();
//        }
//    }

    // Internal methods

//    private void initializePlayer() {
//        if (player == null) {
//            TrackSelection.Factory adaptiveTrackSelectionFactory =
//                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
//            player = ExoPlayerFactory.newSimpleInstance(new DefaultRenderersFactory(this),
//                    new DefaultTrackSelector(adaptiveTrackSelectionFactory), new DefaultLoadControl());
//
//            player.addListener(componentListener);
//            player.setAudioDebugListener(componentListener);
//            player.setVideoDebugListener(componentListener);
//            PlaybackControlView simpleExoplayerView;
//            playerView.setPlayer(player);
//
//            player.setPlayWhenReady(playWhenReady);
//        }
//
//        MediaSource mediaSources = buildMediaSource(Uri.parse(getString(R.string.media_url_hls)));
//        player.seekTo(currentWindow, playBackPosition);
//        player.prepare(mediaSources, true, false);
//        inErrorState = false;
//    }
//
//    private void releasePlayer() {
//        if (player != null) {
//            currentWindow = player.getCurrentWindowIndex();
//            playWhenReady = player.getPlayWhenReady();
//            player.removeListener(componentListener);
//            player.setAudioDebugListener(null);
//            player.setVideoDebugListener(null);
//            player.release();
//            player = null;
//        }
//    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory mediaDataSourceFactory = buildDataSourceFactory(true);
        return new HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri, null, null);
    }


    public DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        DefaultBandwidthMeter bandwidthMeter = useBandwidthMeter ? BANDWIDTH_METER : null;
        return new DefaultDataSourceFactory(this, bandwidthMeter,
                buildHttpDataSourceFactory(true));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        DefaultBandwidthMeter bandwidthMeter = useBandwidthMeter ? BANDWIDTH_METER : null;
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "exAndroid"), bandwidthMeter);
    }

    @SuppressLint("InlineApi")
    private void hideSytemUi() {
//        playerView.setSytemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//            | View.SYSTEM_UI_FLAG_FULLSCREEN
//            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

//    private class ComponentListener implements ExoPlayer.EventListener, VideoRendererEventListener, AudioRendererEventListener {
//        @Override
//        public void onTimelineChanged(Timeline timeline, Object manifest) {
//
//        }
//
//        @Override
//        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
//
//        }
//
//        @Override
//        public void onLoadingChanged(boolean isLoading) {
//
//        }
//
//        @Override
//        public void onPlayerStateChanged(boolean playWhenReady, int state) {
//            String stateString;
//            switch (state) {
//                case ExoPlayer.STATE_IDLE:
//                    stateString = "STATE_IDLE";
//                    break;
//                case ExoPlayer.STATE_BUFFERING:
//                    stateString = "STATE_BUFFERING";
//                    break;
//                case ExoPlayer.STATE_READY:
//                    stateString = "STATE_READY";
//                    break;
//                case ExoPlayer.STATE_ENDED:
//                    stateString = "STATE_ENDED";
//                    break;
//                default:
//                    stateString = "UNKNOWN STATE";
//                    break;
//            }
//            Log.d(TAG, "state [" + playWhenReady + ", " + stateString + "]");
//        }
//
//        @Override
//        public void onRepeatModeChanged(int repeatMode) {
//
//        }
//
//        @Override
//        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
//
//        }
//
//        @Override
//        public void onPlayerError(ExoPlaybackException error) {
//            inErrorState = true;
//        }
//
//        @Override
//        public void onPositionDiscontinuity(int reason) {
//            if (inErrorState) {
//                // This will only occur if the user has performed a seek whilst in the error state. Update
//                // the resume position so that if the user then retries, playback will resume from the
//                // position to which they seeked.
//                updateResumePosition();
//            }
//        }
//
//        private void updateResumePosition() {
//            currentWindow = player.getCurrentWindowIndex();
//            playBackPosition = Math.max(0, player.getContentPosition());
//        }
//
//
//        @Override
//        public void onSeekProcessed() {
//            Log.d(TAG, "seekProcessed");
//        }
//
//        @Override
//        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
//            Log.d(TAG, "playbackParameters " + String.format(
//                    "[speed=%.2f, pitch=%.2f]", playbackParameters.speed, playbackParameters.pitch));
//        }
//
//        @Override
//        public void onAudioEnabled(DecoderCounters counters) {
//
//        }
//
//        @Override
//        public void onAudioSessionId(int audioSessionId) {
//
//        }
//
//        @Override
//        public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
//
//        }
//
//        @Override
//        public void onAudioInputFormatChanged(Format format) {
//
//        }
//
//        @Override
//        public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
//
//        }
//
//        @Override
//        public void onAudioDisabled(DecoderCounters counters) {
//
//        }
//
//        @Override
//        public void onVideoEnabled(DecoderCounters counters) {
//
//        }
//
//        @Override
//        public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
//
//        }
//
//        @Override
//        public void onVideoInputFormatChanged(Format format) {
//
//        }
//
//        @Override
//        public void onDroppedFrames(int count, long elapsedMs) {
//
//        }
//
//        @Override
//        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
//
//        }
//
//        @Override
//        public void onRenderedFirstFrame(Surface surface) {
//
//        }
//
//        @Override
//        public void onVideoDisabled(DecoderCounters counters) {
//
//        }
//    }

    private void viewUserInfo() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View view  = navigationView.getHeaderView(0);

        TextView tvUserNickname = view.findViewById(R.id.tv_user_nickname);
        TextView tvUserPoint = view.findViewById(R.id.tv_user_point);
        TextView tvUserFixed = view.findViewById(R.id.tv_user_fixed);

        UserInfo userInfo = UserInfo.getInstance();
        tvUserNickname.setText(userInfo.getNickname());
        tvUserPoint.setText(userInfo.getPoint());
        tvUserFixed.setText(checkFixed(userInfo.getFixed()));

        TextView tv_serviceCenter = view.findViewById(R.id.tv_serviceCenter);
        TextView tv_userInfo = view.findViewById(R.id.tv_userInfo);
        TextView tv_logout = view.findViewById(R.id.tv_logout);
        tv_serviceCenter.setOnClickListener(drawerClickListenr);
        tv_userInfo.setOnClickListener(drawerClickListenr);
        tv_logout.setOnClickListener(drawerClickListenr);
    }

    private String checkFixed(String fixed){
        if(fixed.equals("0")){
            return "-";
        }else{
            return fixed;
        }
    }

    private View.OnClickListener drawerClickListenr = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Fragment fragment = null;

            switch (v.getId()){
                case R.id.tv_playlist:
                    break;
                case R.id.tv_serviceCenter:
                    fragment = new VideoPlayerFragment();
                    break;
                case R.id.tv_userInfo:
                    fragment = new UserInfoFragment();
                    break;
                case R.id.tv_logout:
                    tryLogout();
                    break;
            }

            if(fragment != null){
                FragmentTransaction ft = getFragmentManager().beginTransaction();
//                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.main_media_frame, fragment);
                ft.commit();
            }
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }
    };

    private void initGoogle() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(MainActivity.this, "" + connectionResult, Toast.LENGTH_SHORT).show();
    }

    private void tryLogout(){
        Log.d(TAG, "tryLogout:" + UserInfo.getInstance().getSocial());
        if("KK".equals(UserInfo.getInstance().getSocial())){
            UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                @Override
                public void onCompleteLogout() {
                    procLogout();
                }
            });
        }else if("NV".equals(UserInfo.getInstance().getSocial())){
            NaverLogin naverLogin = new NaverLogin(getApplicationContext());
            naverLogin.setListener(new NaverLogin.NaverLoginListener() {
                @Override
                public void onLogined(String uid, String email) {

                }

                @Override
                public void onLogout() {
                    procLogout();
                }
            });
            naverLogin.forceLogout();
        }else if("FB".equals(UserInfo.getInstance().getSocial())){
            if(AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut();
                procLogout();
            }
        }else if("GG".equals(UserInfo.getInstance().getSocial())){
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    procLogout();
                }
            });
        }
    }

    private void procLogout() {
        new MinimonUser().logout();
        new JUtil().alertNotice(MainActivity.this, getResources().getString(R.string.notice_logout), new JUtil.JUtilListener() {
            @Override
            public void callback(int id) {
                gotoGate();
            }
        });
    }

    private void gotoGate(){
        Intent intent = new Intent(MainActivity.this, GateActivity.class);
        startActivity(intent);
        finish();
    }
}
