package com.minimon.diocian.player;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WIFI_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class WebViewFragment extends Fragment implements MainActivity.onKeypressListenr,
        MyWebChromeClient.ProgressListener,
        MinimonUser.MinimonUserListener,
        MinimonWebView.MinimonWebviewListener,
        JavascriptInterface.JavascriptInterfaceListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_WEBVIEW = "webViewUrl";

    private ObservableWebView mWebView;
    private ProgressBar mProgressBar;
    private MinimonWebView minimonWebView;
    private JavascriptInterface javascriptInterface;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    /*
    플레이어 변수
     */
    private boolean playWhenReady = false;
    private String videoUrl = "";

    private FrameLayout mFullScreenButton;
    private ImageView mFullScreenIcon;
    private TextView mEpisodeTitle;
    private ImageView mLockScreen;
    private boolean isLockSreen;

    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private LinearLayout layout_player_view;
    private ComponentListener componentListener;

    private String nowBandWidth = "";
    private boolean isChangeBandWidth;
    private LinearLayout mNowBandWidth;
    private TextView mBandWidth;
    private TextView mBandWidthAuto;
    private TextView mBandWidth480;
    private TextView mBandWidth720;
    private TextView mBandWidth1080;
    private LinearLayout mBandWidthView;
    MinimonEpisode minimonEpisode;

    //하단 재생목록, 인기목록
    private String c_title;
    private String c_idx;
    private String nowEp;
    private String mPage;
    LinearLayout view_main_search;
    // TODO: Rename and change types of parameters

    public WebViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * <p>
     * //     * @param param1 Parameter 1.
     *
     * @return A new instance of fragment WebViewFragment.
     */
    // TODO: Rename and change types and number of parameters
//    public static WebViewFragment newInstance(String param1) {
//        WebViewFragment fragment = new WebViewFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM_WEBVIEW, param1);
//        fragment.setArguments(args);
//        return fragment;
//    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_web_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        minimonWebView = new MinimonWebView();
        minimonWebView.setListener(this);
        javascriptInterface = new JavascriptInterface(getActivity(), mWebView);
        javascriptInterface.setListener(this);

        mWebView = (ObservableWebView) view.findViewById(R.id.webview_other);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mWebView.setWebViewClient(new MyWebviewClient(getActivity(), mProgressBar));
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.addJavascriptInterface(javascriptInterface, "minimon");
        mWebView.getSettings().setJavaScriptEnabled(true);
        view_main_search = getActivity().findViewById(R.id.view_main_search);
        mWebView.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback() {
            @Override
            public void onScroll(int l, int t) {
                Log.d("scroll changed",String.valueOf(l) + "," + String.valueOf(t));
                if("main".equals(mPage)){
                    if(t!=0){
                        Log.d("webViewScroll","notTop");
                        view_main_search.setBackgroundColor(Color.parseColor("#BFFB450B"));
                    }else{
                        Log.d("webViewScroll","isTop");
                        view_main_search.setBackgroundColor(getResources().getColor(R.color.transparent));
                    }
                }
            }
        });

        componentListener = new ComponentListener();
        playerView = (SimpleExoPlayerView) view.findViewById(R.id.player_view);
        layout_player_view = view.findViewById(R.id.layout_player_view);
//        layout_player_view.setVisibility(View.VISIBLE);
        if(!isLockSreen)
            isLockSreen = true;

        initData();

        UserInfo info = UserInfo.getInstance();

        ContentValues content = new ContentValues();
        content.put("id", info.getUID());
        content.put("loc", "Android");
        content.put("page", WebViewInfo.getInstance().getPageName());
        mPage = WebViewInfo.getInstance().getPageName();
        minimonWebView.goToWeb("Contents/view", content);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity activity = (MainActivity) getActivity();
        activity.setOnKeypressListener(this);
    }

    @Override
    public void onBack() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            MainActivity activity = (MainActivity) getActivity();
            activity.setOnKeypressListener(null);
            activity.onBackPressed();
        }
    }

    @Override
    public void onUpdateProgress(int progressValue) {
        mProgressBar.setProgress(progressValue);
        if (progressValue == 100) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResponse(JSONObject info) {
        Log.d("PostAPITOKEN", info.toString());
    }
//
//    @Override
//    public void onResponseHtml(String html) {
//        Log.d("PostAPITOKEN", html);
//        mWebView.loadData(html, "text/html","utf-8");
//    }

    @Override
    public void onGoToWeb(String url, String page, String key, String value) {
        Log.d("onGoToWeb", url);
        UserInfo info = UserInfo.getInstance();
        ContentValues content = new ContentValues();
        content.put("id", info.getUID());
        content.put("loc", "Android");
        content.put("page", page);
        mPage = page;
        if("episode".equals(page)) {
//            playerView.setVisibility(View.VISIBLE);
//            changePlayerVisibility(true);
//            EpisodeInfo.getInsatnace().setIdx(value);
//            sendEpisodeData(value);
            Log.d("episodeValue",value);
           goToEpisodeMain(url,page,key,value);
            return;
        }
        content.put(key, value);
        Log.d("onGoToWebUID", info.getUID());
        minimonWebView.goToWeb(url, content);
    }

    private void goToEpisodeMain(String url, String page, String key, String value)
    {
        Intent intent = new Intent(getActivity(),DramaPlayActivity.class);
        intent.putExtra("url",url);
        intent.putExtra("page",page);
        intent.putExtra("key",key);
        intent.putExtra("value",value);
        startActivity(intent);
    }

    @Override
    public void onResponseHtml(String html, String baseUrl) {
        Log.d("BasrUrl", baseUrl);
        Log.d("BaseUrlHtml", html);
        mWebView.loadDataWithBaseURL(baseUrl, html, "text/html", "utf-8", null);
    }

    @Override
    public void closeRefreshWeb(String url, String page, String key, String value) {
        UserInfo info = UserInfo.getInstance();
        ContentValues content = new ContentValues();
        content.put("id", info.getUID());
        content.put("loc", "Android");
        content.put("page", page);
        content.put(key, value);
        Log.d("onGoToWebUID", info.getUID());
        minimonWebView.goToWeb(url, content);
    }

    @Override
    public void closeDepthRefreshWeb(String depth) {
        if (mWebView.canGoBackOrForward(Integer.parseInt(depth)))
            mWebView.goBackOrForward(Integer.parseInt(depth));
    }

    @Override
    public void goToPg(String url, String item, String how) {

    }

    @Override
    public void goToSearch() {

    }

//    플레이어 세팅

//    private void checkWifi(){
//        if(!ConfigInfo.getInstance().isUseData()){
//            WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
//            boolean wifiEnabled = wifiManager.isWifiEnabled();
//            if(!wifiEnabled){
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setMessage(getString(R.string.alert_wifi_not_found));
//                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        ConfigInfo.getInstance().setUseData(true);
//                        SharedPreferences preferences = getActivity().getSharedPreferences("minimon-preference", MODE_PRIVATE);
//                        preferences.edit().putBoolean("useData",ConfigInfo.getInstance().isUseData());
//                        preferences.edit().apply();
//                    }
//                });
//                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        finish();
//                    }
//                });
//                builder.show();
//            }
//        }
//    }

    /*
    현재 플레이할 에피소드 데이터
     */
    private void sendEpisodeData(String idx) {
        Log.d("sendEpisodeData", idx);
        ContentValues values = new ContentValues();
        values.put("ep_idx", idx);
        if (ConfigInfo.getInstance().getBandwidth() == 3)
            values.put("quality", "hlsabr");
        else
            values.put("quality", "his");
        values.put("id", UserInfo.getInstance().getUID());

        minimonEpisode.info(values);
    }

    private void initData() {
        minimonEpisode = new MinimonEpisode();
        minimonEpisode.setListener(new MinimonEpisode.MinimonEpisodeListener() {
            @Override
            public void onResponse(JSONObject info, String responseType) {
                try {
                    if ("info".equals(responseType)) //현재 플레이할 에피소드 에이터
                        setData(info);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void setData(JSONObject info) {
        try {
            JSONArray videoArr = (JSONArray) info.getJSONObject("data").getJSONObject("list").getJSONObject("list_mp").get("video");
            JSONObject videoObj = null;
            if (ConfigInfo.getInstance().getBandwidth() != 3)
                videoObj = (JSONObject) videoArr.get(ConfigInfo.getInstance().getBandwidth());
            else
                videoObj = (JSONObject) videoArr.get(0);
            Log.d("VideoObjTag", videoObj.toString());
            JSONArray episodeArr = (JSONArray) info.getJSONObject("data").getJSONObject("list").get("list_ep");
            JSONObject episodeInformation = (JSONObject) info.getJSONObject("data").get("list");
            setEpisodeData(episodeInformation);
            videoUrl = videoObj.getString("playUrl");
            EpisodeInfo.getInsatnace().setVideoUrl(videoUrl);
            if (!isChangeBandWidth && EpisodeInfo.getInsatnace().getResumePosition() != 0) {
                EpisodeInfo.getInsatnace().setResumePosition(0);
            } else {
                isChangeBandWidth = !isChangeBandWidth;
            }
            initializePlayer();
            initFullscreenButton();
        } catch (JSONException e) {
//            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private void setEpisodeData(JSONObject obj) {
        try {
            EpisodeInfo.getInsatnace().setTitle(obj.getString("title"));
            EpisodeInfo.getInsatnace().setC_idx(obj.getString("c_idx"));
            EpisodeInfo.getInsatnace().setIdx(obj.getString("idx"));
            c_title = obj.getString("c_title");
            getActivity().setTitle(c_title);
            nowEp = obj.getString("ep");
            JSONArray jarr = obj.getJSONArray("list_tag");
            String tags = "";
            for (int i = 0; i < jarr.length(); i++) {
                JSONObject objTag = (JSONObject) jarr.get(i);
                tags += "#" + objTag.getString("tag") + " ";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

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
        if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            mLockScreen.setImageResource(R.mipmap.a022_play_b_lock_on);
        else
            mLockScreen.setImageResource(R.mipmap.a022_play_b_lock_off);

        controlView.findViewById(R.id.tv_playing_playlist).setVisibility(View.GONE);
        controlView.findViewById(R.id.img_playing_playlist).setVisibility(View.GONE);
        mLockScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getActivity().getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR) {//잠겨있던 잠금 풀 경우
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    mLockScreen.setImageResource(R.mipmap.a022_play_b_lock_off);
                    isLockSreen = !isLockSreen;
                } else { //다시 잠글경우
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                    mLockScreen.setImageResource(R.mipmap.a022_play_b_lock_on);
                    isLockSreen = !isLockSreen;
                }
            }
        });

        mBandWidthView = controlView.findViewById(R.id.view_bandwidth);
        mBandWidth = controlView.findViewById(R.id.tv_bandwidth);
        mNowBandWidth = controlView.findViewById(R.id.view_now_bandwidth);

        switch (ConfigInfo.getInstance().getBandwidth()) {
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
                if (mBandWidthView.getVisibility() == View.VISIBLE)
                    mBandWidthView.setVisibility(View.GONE);
                else
                    mBandWidthView.setVisibility(View.VISIBLE);
            }
        });
        mBandWidth480.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("480p".equals(nowBandWidth))
                    return;
                ConfigInfo.getInstance().setBandwidth(ConfigInfo.bandwidth480);
                mBandWidthView.setVisibility(View.GONE);
                nowBandWidth = "480p";
                mBandWidth.setText(nowBandWidth);
                isChangeBandWidth = true;
                EpisodeInfo.getInsatnace().setResumePosition(Math.max(0, playerView.getPlayer().getContentPosition()));
                sendEpisodeData(EpisodeInfo.getInsatnace().getIdx());
            }
        });
        mBandWidth720.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("720p".equals(nowBandWidth))
                    return;
                ConfigInfo.getInstance().setBandwidth(ConfigInfo.bandwidth720);
                mBandWidthView.setVisibility(View.GONE);
                nowBandWidth = "720p";
                mBandWidth.setText(nowBandWidth);
                isChangeBandWidth = true;
                EpisodeInfo.getInsatnace().setResumePosition(Math.max(0, playerView.getPlayer().getContentPosition()));
                sendEpisodeData(EpisodeInfo.getInsatnace().getIdx());
            }
        });
        mBandWidth1080.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("1080p".equals(nowBandWidth))
                    return;
                ConfigInfo.getInstance().setBandwidth(ConfigInfo.bandwidth1080);
                mBandWidthView.setVisibility(View.GONE);
                nowBandWidth = "1080p";
                mBandWidth.setText(nowBandWidth);
                isChangeBandWidth = true;
                EpisodeInfo.getInsatnace().setResumePosition(Math.max(0, playerView.getPlayer().getContentPosition()));
                sendEpisodeData(EpisodeInfo.getInsatnace().getIdx());
            }
        });
        mBandWidthAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBandWidthView.setVisibility(View.GONE);
                if ("자동".equals(nowBandWidth))
                    return;
                ConfigInfo.getInstance().setBandwidth(ConfigInfo.banswidthAuto);
                nowBandWidth = "자동";
                mBandWidth.setText(nowBandWidth);
                isChangeBandWidth = true;
                EpisodeInfo.getInsatnace().setResumePosition(Math.max(0, playerView.getPlayer().getContentPosition()));
                sendEpisodeData(EpisodeInfo.getInsatnace().getIdx());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
//            initData();
            if (EpisodeInfo.getInsatnace().getIdx() == null || EpisodeInfo.getInsatnace().getIdx().isEmpty()) {
                sendEpisodeData("645");
            } else {
                sendEpisodeData(EpisodeInfo.getInsatnace().getIdx());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23) {
//            initData();

            if (EpisodeInfo.getInsatnace().getIdx() == null || EpisodeInfo.getInsatnace().getIdx().isEmpty())
                sendEpisodeData("645");
            else
                sendEpisodeData(EpisodeInfo.getInsatnace().getIdx());
        }
    }

    private void goFullScreen() {
        Intent intent = new Intent(getActivity(), VideoPlayScreenActivity.class);
        startActivity(intent);
    }

    private void initializePlayer() {
        if (player == null) {
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            TrackSelector trackSelector =
                    new DefaultTrackSelector(adaptiveTrackSelectionFactory);
            player = ExoPlayerFactory.newSimpleInstance(getActivity(), trackSelector);

            player.addListener(componentListener);
            player.setAudioDebugListener(componentListener);
            player.setVideoDebugListener(componentListener);
            playerView.setPlayer(player);

            player.setPlayWhenReady(playWhenReady);
        }


        MediaSource mediaSources = buildMediaSource(Uri.parse(videoUrl), "mp4");
        playerView.getPlayer().prepare(mediaSources, true, false);
        if (EpisodeInfo.getInsatnace().getResumePosition() != 0) {
            playerView.getPlayer().seekTo(EpisodeInfo.getInsatnace().getResumePosition());
            playerView.getPlayer().setPlayWhenReady(true);
        }
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

    private MediaSource buildMediaSource(Uri uri, String videoType) {

        DataSource.Factory mediaDataSourceFactory = buildDataSourceFactory(true);
        if ("mp4".equals(videoType))
            return new ExtractorMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri, null, null);
        else if ("hls".equals(videoType))
            return new HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri, null, null);
        else
            return null;
    }

    public DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        DefaultBandwidthMeter bandwidthMeter = useBandwidthMeter ? BANDWIDTH_METER : null;
        return new DefaultDataSourceFactory(getActivity(), bandwidthMeter, buildHttpDataSourceFactory(true));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        DefaultBandwidthMeter bandwidthMeter = useBandwidthMeter ? BANDWIDTH_METER : null;
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(getActivity(), "exAndroid"), bandwidthMeter);
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
}
