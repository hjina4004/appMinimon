package com.minimon.diocian.player;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.*;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener{

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final String TAG = "MainActivity";
    private final String PREF_NAME = "minimon-preference";

    private RelativeLayout view_main_toolbar;
    private TextAwesome tv_toolbar_open_drawer;
    private TextAwesome tv_toolbar_search;
    private ImageView tv_toolbar_go_back;
    private EditText ed_toolbar_search;
    private DrawerLayout drawer;
    private RelativeLayout view_delete_search_history;
    private RecyclerView rec_search_history;
    private SearchhistoryAdapter adapter;
    private List<SearchItem> arrHistory = new ArrayList<SearchItem>();
    private LinearLayoutManager manager;
    private Realm realm;

    // for Google
    private static final int GOOGLE_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;

    private WebView mWebView;
    private boolean isMain;

    public interface onKeypressListenr{
        public void onBack();

    }

    private onKeypressListenr mListenr;
    public void setOnKeypressListener(onKeypressListenr listener){
        mListenr = listener;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setBackgroundColor(getResources().getColor(R.color.colorBaseBG));

        initGoogle();

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        Log.v(TAG, "User Info --- " + UserInfo.getInstance().getData());
        view_main_toolbar = (RelativeLayout)  findViewById(R.id.view_main_toolbar);
        tv_toolbar_open_drawer = (TextAwesome) findViewById(R.id.tv_toolbar_open_drawer);
        tv_toolbar_search = (TextAwesome) findViewById(R.id.tv_toolbar_search);
        tv_toolbar_go_back = findViewById(R.id.tv_toolbar_go_back);
        ed_toolbar_search = (EditText) findViewById(R.id.ed_toolbar_search);

        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        view_delete_search_history = findViewById(R.id.view_delete_search_history);
        rec_search_history = findViewById(R.id.rec_search_history);
        rec_search_history.setLayoutManager(manager);
        DividerItemDecoration deco = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        rec_search_history.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        tv_toolbar_open_drawer.setOnClickListener(toolbarClickListenr);
        tv_toolbar_search.setOnClickListener(toolbarClickListenr);
        tv_toolbar_go_back.setOnClickListener(toolbarClickListenr);
        view_delete_search_history.setOnClickListener(toolbarClickListenr);


        adapter = new SearchhistoryAdapter(this,arrHistory);
        rec_search_history.setAdapter(adapter);

        ed_toolbar_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_NEXT ||
                        i == EditorInfo.IME_ACTION_DONE ||
                        keyEvent != null &&
                                keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                                keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (keyEvent == null || !keyEvent.isShiftPressed()) {
                        // the user is done typing.
                        Date today = Calendar.getInstance().getTime();
                        textView.getText();
                        SearchItem s = new SearchItem();
                        s.setDate(String.valueOf(today.getYear()+1900)+"."+(today.getMonth()+1)+"."+today.getDate());
                        s.setHistory(textView.getText().toString());
                        SearchItemList listArr = new SearchItemList();
                        arrHistory.add(s);
                        adapter.notifyDataSetChanged();
                        realm.beginTransaction();
                        final SearchItem item = realm.copyToRealm(s);

                        return true; // consume.
                    }
                }
                return false;
            }
        });

        viewUserInfo();

        goMainWeb();
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
            if(mListenr!=null){
                mListenr.onBack();
            }else {
                if(isMain) {
                    long tempTime = System.currentTimeMillis();
                    long intervalTime = tempTime - backPressedTime;

                    if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
                        super.onBackPressed();
                    } else {
                        backPressedTime = tempTime;
                        Toast.makeText(getApplicationContext(), R.string.notice_exit_app, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    goMainWeb();
                }
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
            Fragment fragment = new SettingFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
//                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.main_media_frame, fragment);
            ft.commit();
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
        TextView tvUserPoint = view.findViewById(R.id.tv_menu_point);
        TextView tvUserFixed = view.findViewById(R.id.tv_menu_charge);

        UserInfo userInfo = UserInfo.getInstance();
        tvUserNickname.setText(userInfo.getNickname());
        tvUserPoint.setText(userInfo.getPoint());
        tvUserFixed.setText(checkFixed(userInfo.getFixed()));

//        TextView tv_serviceCenter = view.findViewById(R.id.tv_serviceCenter);
//        TextView tv_userInfo = view.findViewById(R.id.tv_userInfo);
        LinearLayout view_logout = view.findViewById(R.id.view_menu_logout);
        view_logout.setOnClickListener(drawerClickListenr);
        LinearLayout view_menu_go_home = view.findViewById(R.id.view_menu_go_home);
        view_menu_go_home.setOnClickListener(drawerClickListenr);
        ImageView img_menu_close = view.findViewById(R.id.img_menu_close);
        img_menu_close.setOnClickListener(drawerClickListenr);
        LinearLayout view_menu_cookies = view.findViewById(R.id.view_menu_cookies);
        view_menu_cookies.setOnClickListener(drawerClickListenr);
        LinearLayout view_menu_user_info = view.findViewById(R.id.view_menu_user_info);
        view_menu_user_info.setOnClickListener(drawerClickListenr);
        LinearLayout view_menu_purchase = view.findViewById(R.id.view_menu_purchase);
        view_menu_purchase.setOnClickListener(drawerClickListenr);
        LinearLayout view_menu_favorite = view.findViewById(R.id.view_menu_favorite);
        view_menu_favorite.setOnClickListener(drawerClickListenr);
        LinearLayout view_menu_subscribe = view.findViewById(R.id.view_menu_subscribe);
        view_menu_subscribe.setOnClickListener(drawerClickListenr);
        LinearLayout view_menu_point_history = view.findViewById(R.id.view_menu_point_history);
        view_menu_point_history.setOnClickListener(drawerClickListenr);
        LinearLayout view_menu_pay_history = view.findViewById(R.id.view_menu_pay_history);
        view_menu_pay_history.setOnClickListener(drawerClickListenr);
        LinearLayout view_menu_setting = view.findViewById(R.id.view_menu_setting);
        view_menu_setting.setOnClickListener(drawerClickListenr);
        LinearLayout view_menu_notice = view.findViewById(R.id.view_menu_notice);
        view_menu_notice.setOnClickListener(drawerClickListenr);
        LinearLayout view_menu_faq = view.findViewById(R.id.view_menu_faq);
        view_menu_faq.setOnClickListener(drawerClickListenr);
        LinearLayout view_menu_qna = view.findViewById(R.id.view_menu_qna);
        view_menu_qna.setOnClickListener(drawerClickListenr);
        LinearLayout view_menu_policy = view.findViewById(R.id.view_menu_policy);
        view_menu_policy.setOnClickListener(drawerClickListenr);


    }

    private String checkFixed(String fixed){
        if("0".equals(fixed)){
            return "-";
        }else{
            return fixed;
        }
    }

    private View.OnClickListener toolbarClickListenr = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.tv_toolbar_open_drawer:
                    drawer.openDrawer(Gravity.LEFT);
                    break;
                case R.id.tv_toolbar_search:
                    ed_toolbar_search.setVisibility(View.VISIBLE);
                    view_delete_search_history.setVisibility(View.VISIBLE);
                    rec_search_history.setVisibility(View.VISIBLE);
                    tv_toolbar_go_back.setVisibility(View.VISIBLE);
                    tv_toolbar_search.setVisibility(View.GONE);
                    tv_toolbar_open_drawer.setVisibility(View.GONE);
                    view_main_toolbar.setBackgroundColor(getResources().getColor(R.color.MainColor));
                    break;
                case R.id.tv_toolbar_go_back:
                    ed_toolbar_search.setVisibility(View.GONE);
                    view_delete_search_history.setVisibility(View.GONE);
                    rec_search_history.setVisibility(View.GONE);
                    tv_toolbar_go_back.setVisibility(View.GONE);
                    tv_toolbar_search.setVisibility(View.VISIBLE);
                    tv_toolbar_open_drawer.setVisibility(View.VISIBLE);
                    if(isMain){
                        view_main_toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
                    }
                    break;
                case R.id.view_delete_search_history:
                    arrHistory.clear();
                    adapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    private View.OnClickListener drawerClickListenr = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Fragment fragment = null;

            switch (v.getId()){
//                case R.id.tv_playlist:
//                    Intent intent = new Intent(getApplicationContext(), DramaPlayActivity.class);
//                    startActivity(intent);
//                    break;
//                case R.id.tv_userInfo:
//                    ConfigInfo.getInstance().setWebViewUrl("http://lmfriends.com/android-web-view/user-info/");
//                    fragment = new WebViewFragment();
//                    break;
                case R.id.view_menu_logout:
                    tryLogout();
                    break;
                case R.id.img_menu_close:
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                    if(isMain){
                        view_main_toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
                    }
                    break;
                case R.id.view_menu_go_home:
                    goMainWeb();
                    break;
                case R.id.view_menu_cookies:
                    ConfigInfo.getInstance().setWebViewUrl("http://dev.api.minimon.com/Test/view/cookie.list");
                    fragment = new WebViewFragment();
                    isMain = false;
                    break;
                case R.id.view_menu_user_info:
                    ConfigInfo.getInstance().setWebViewUrl("http://dev.api.minimon.com/Test/view/info");
                    fragment = new WebViewFragment();
                    isMain = false;
                    break;
                case R.id.view_menu_purchase:
                    ConfigInfo.getInstance().setWebViewUrl("http://dev.api.minimon.com/Test/view/purchase");
                    fragment = new WebViewFragment();
                    isMain = false;
                    break;
                case R.id.view_menu_favorite:
                    ConfigInfo.getInstance().setWebViewUrl("http://dev.api.minimon.com/Test/view/like");
                    fragment = new WebViewFragment();
                    isMain = false;
                    break;
                case R.id.view_menu_subscribe:
                    ConfigInfo.getInstance().setWebViewUrl("http://dev.api.minimon.com/Test/view/keep");
                    fragment = new WebViewFragment();
                    isMain = false;
                    break;
                case R.id.view_menu_point_history:
                    ConfigInfo.getInstance().setWebViewUrl("http://dev.api.minimon.com/Test/view/point.list");
                    fragment = new WebViewFragment();
                    isMain = false;
                    break;
                case R.id.view_menu_pay_history:
                    ConfigInfo.getInstance().setWebViewUrl("http://dev.api.minimon.com/Test/view/pay.list");
                    fragment = new WebViewFragment();
                    isMain = false;
                    break;
                case R.id.view_menu_setting:
                    fragment = new SettingFragment();
                    isMain = false;
                    break;
                case R.id.view_menu_notice:
                    ConfigInfo.getInstance().setWebViewUrl("http://dev.api.minimon.com/Test/view/notice");
                    fragment = new WebViewFragment();
                    isMain = false;
                    break;
                case R.id.view_menu_faq:
                    ConfigInfo.getInstance().setWebViewUrl("http://dev.api.minimon.com/Test/view/faq");
                    fragment = new WebViewFragment();
                    isMain = false;
                    break;
                case R.id.view_menu_qna:
                    ConfigInfo.getInstance().setWebViewUrl("http://dev.api.minimon.com/Test/view/qna.list");
                    fragment = new WebViewFragment();
                    isMain = false;
                    break;
                case R.id.view_menu_policy:
                    ConfigInfo.getInstance().setWebViewUrl("http://dev.api.minimon.com/Test/view/policy");
                    fragment = new WebViewFragment();
                    isMain = false;
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
            view_main_toolbar.setBackgroundColor(getResources().getColor(R.color.MainColor));
        }
    };

    public void goMainWeb(){
        view_main_toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
        isMain = true;
        ConfigInfo.getInstance().setWebViewUrl("http://dev.api.minimon.com/Test/view/main");
        Fragment fragment = new WebViewFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.main_media_frame,fragment);
        ft.commit();
    }

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
        String typeSocial = UserInfo.getInstance().getSocial();
        Log.d(TAG, "tryLogout:" + typeSocial);
        if("KK".equals(typeSocial)){
            UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                @Override
                public void onCompleteLogout() {
                    procLogout();
                }
            });
        }else if("NV".equals(typeSocial)){
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
        }else if("FB".equals(typeSocial)){
            if(AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut();
                procLogout();
            }
        }else if("GG".equals(typeSocial)){
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    procLogout();
                }
            });
        }else{
            procLogout();
        }
    }

    private void procLogout() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear().commit();
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
