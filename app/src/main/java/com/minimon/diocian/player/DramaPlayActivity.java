package com.minimon.diocian.player;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DramaPlayActivity extends AppCompatActivity implements MinimonWebView.MinimonWebviewListener, JavascriptInterface.JavascriptInterfaceListener{

    private final String TAG = "DramaPlayActivity";
    private final String STATE_RESUME_WINDOW = "resumeWindow";
    private final String STATE_RESUME_POSITION = "resumePosition";
    private final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";
    private final int REQUEST_FULLSCREEN = 1;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private boolean playWhenReady = false;
    private String videoUrl = "";
    private String playingVideoUrl = "";

    private FrameLayout mFullScreenButton;
    private ImageView mFullScreenIcon;
    private TextView mEpisodeTitle;
    private ImageView mLockScreen;
    private boolean isLockSreen;

    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;
    private ComponentListener componentListener;

    private String nowBandWidth = "";
    private boolean isChangeBandWidth  = false;
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

    private WebView mWebView;

    private String url;
    private String page;
    private String key;
    private String value;
    private MinimonWebView minimonWebView;
    private ImageView img_toolbar_go_back;
    private TextView tv_frag_title;

    private ProgressBar progress_bar_drama_play;
    private JavascriptInterface javascriptInterface;

    private FrameLayout view_player_thumbnail;
    private ImageView img_player_thumbnail;
    private boolean prepareVideoFlag     = false; //changePlayer시에도 초기화해주기
    private boolean isReplayVideoFlag    = false;

    private playListener m_playlistener;
//    private BroadcastReceiver broadcastReceiver;
    IntentFilter intentFilter;
    JUtil jUtil;
    boolean isShowAlert = false;
    boolean isActive;
    private JWiFiMonitor wifiMonitor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drama_play);
        progress_bar_drama_play = findViewById(R.id.progress_bar_drama_play);
        mWebView = findViewById(R.id.webview_dramaplay);
        javascriptInterface = new JavascriptInterface(this, mWebView);
        javascriptInterface.setListener(this);
        jUtil = new JUtil();
        isActive = false;
//        broadcastReceiver = new InternetConnector_Receiver_DramaPlay(){
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if(!isActive)
//                    return;
//                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//
//                if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
//                    if(networkInfo!= null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
//                        if(!ConfigInfo.getInstance().isUseData() && prepareVideoFlag && !isShowAlert){
//                            isShowAlert = true;
//                            playerView.getPlayer().setPlayWhenReady(false);
//                            jUtil.confirmNotice(context, "WiFi 환경이 아닙니다. LTE/3G 데이터를 사용하시겠습니까?\n", new JUtil.JUtilListener() {
//                                @Override
//                                public void callback(int id) {
//                                    if(id == 1){
//                                        playerView.getPlayer().setPlayWhenReady(true);
//                                    }
//                                    isShowAlert = false;
//                                }
//                            });
//                        }
//                    }
//                }
//            }
//        };
        intentFilter = new IntentFilter();
//        intentFilter.addAction("com.minimon.diocian.player.SEND_BROAD_CAST");
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mWebView.setWebViewClient(new MyWebviewClient(this, progress_bar_drama_play));
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.addJavascriptInterface(javascriptInterface,"minimon");
        mWebView.getSettings().setJavaScriptEnabled(true);

//        registerReceiver(broadcastReceiver, intentFilter);

        img_toolbar_go_back = findViewById(R.id.img_toolbar_go_back);
        img_toolbar_go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tv_frag_title = findViewById(R.id.tv_frag_title);

        view_player_thumbnail = findViewById(R.id.view_player_thumbnail);
        img_player_thumbnail = findViewById(R.id.img_player_thumbnail);

        url = getIntent().getStringExtra("url");
        page = getIntent().getStringExtra("page");
        key = getIntent().getStringExtra("key");
        value = getIntent().getStringExtra("value");
        EpisodeInfo.getInsatnace().setIdx(value);

        EpisodeInfo epInfo = EpisodeInfo.getInsatnace();
        ConfigInfo conInfo = ConfigInfo.getInstance();
        epInfo.setUseLte(conInfo.isUseData());
        Log.i("JWiFiMonitor", epInfo.isUseLte()? "use LTE":"only WIFI");
        epInfo.setBandwidth(conInfo.getBandwidth());

        minimonWebView = new MinimonWebView();
        minimonWebView.setListener(this);

        ContentValues content = new ContentValues();
        content.put("id", UserInfo.getInstance().getUID());
        content.put("loc", "Android");
        content.put("page", page);
        content.put(key, value);
        minimonWebView.goToWeb(url,content);

        componentListener = new ComponentListener();
        playerView = (SimpleExoPlayerView) findViewById(R.id.player_view);

        m_playlistener = new playListener() {
            @Override
            public void endPlay() {
                String _introVideoUrl = EpisodeInfo.getInsatnace().getIntroVideoUrl();
                String _currentVideoUrl = EpisodeInfo.getInsatnace().getCurrentVideoUrl();
                String _videoUrl = EpisodeInfo.getInsatnace().getVideoUrl();
                Log.d("endPlay_currentVideoUrl",_currentVideoUrl);
                Log.d("endPlay_videoUrl",_videoUrl);
                Log.d("endPlay_introVideoUrl",_introVideoUrl);
                if (EpisodeInfo.getInsatnace().isIntro()) {
                    EpisodeInfo.getInsatnace().setIntro(false);
                    updatePlayerVideo(_videoUrl);
                }
            }
        };

        if(!isLockSreen)
            isLockSreen = true;

    }

    private void createWifiMonitor(){
        wifiMonitor = new JWiFiMonitor(this);
        wifiMonitor.setOnChangeNetworkStatusListener(WifiChangedListener);
        registerReceiver(wifiMonitor, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
    }

    private void removeWifiMoniter() {
        unregisterReceiver(wifiMonitor);
    }

    JWiFiMonitor.OnChangeNetworkStatusListener WifiChangedListener = new JWiFiMonitor.OnChangeNetworkStatusListener() {
        @Override
        public void OnChanged(int status) {
            String tag = "JWiFiMonitor";
            switch(status) {
                case JWiFiMonitor.WIFI_STATE_DISABLED:
                    Log.i(tag, "[WifiMonitor] WIFI_STATE_DISABLED");
                    if(prepareVideoFlag)
                        confirmUseLTE();
                    break;
                case JWiFiMonitor.WIFI_STATE_DISABLING:
                    Log.i(tag, "[WifiMonitor] WIFI_STATE_DISABLING");
                    break;
                case JWiFiMonitor.WIFI_STATE_ENABLED:
                    Log.i(tag, "[WifiMonitor] WIFI_STATE_ENABLED");
                    break;
                case JWiFiMonitor.WIFI_STATE_ENABLING:
                    Log.i(tag, "[WifiMonitor] WIFI_STATE_ENABLING");
                    break;
                case JWiFiMonitor.WIFI_STATE_UNKNOWN:
                    Log.i(tag, "[WifiMonitor] WIFI_STATE_UNKNOWN");
                    break;
                case JWiFiMonitor.NETWORK_STATE_CONNECTED:
                    Log.i(tag, "[WifiMonitor] NETWORK_STATE_CONNECTED");
                    break;
                case JWiFiMonitor.NETWORK_STATE_CONNECTING:
                    Log.i(tag, "[WifiMonitor] NETWORK_STATE_CONNECTING");
                    break;
                case JWiFiMonitor.NETWORK_STATE_DISCONNECTED:
                    Log.i(tag, "[WifiMonitor] NETWORK_STATE_DISCONNECTED");
                    break;
                case JWiFiMonitor.NETWORK_STATE_DISCONNECTING:
                    Log.i(tag, "[WifiMonitor] NETWORK_STATE_DISCONNECTING");
                    break;
                case JWiFiMonitor.NETWORK_STATE_SUSPENDED:
                    Log.i(tag, "[WifiMonitor] NETWORK_STATE_SUSPENDED");
                    break;
                case JWiFiMonitor.NETWORK_STATE_UNKNOWN:
                    Log.i(tag, "[WifiMonitor] NETWORK_STATE_UNKNOWN");
                    break;
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    private void confirmUseLTE() {
        if (EpisodeInfo.getInsatnace().isUseLte())
            return;
        if(playerView.getPlayer() != null) {
            playerView.getPlayer().setPlayWhenReady(false);
            jUtil.confirmNotice(this, "WiFi 환경이 아닙니다. LTE/3G 데이터를 사용하시겠습니까?", new JUtil.JUtilListener() {
                @Override
                public void callback(int id) {
                    if (id == 1) {
                        EpisodeInfo.getInsatnace().setUseLte(true);
                        playerView.getPlayer().setPlayWhenReady(true);
                    }
                }
            });
        }
    }

    private void procUsableLTE(){
        if(!ConfigInfo.getInstance().isUseData()){
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            boolean wifiEnabled = wifiManager.isWifiEnabled();
            if(!wifiEnabled){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.alert_wifi_not_found));
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EpisodeInfo.getInsatnace().setUseLte(true);
                        procPlayIntro();
                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                builder.show();
            }else{
                //EpisodeInfo.getInsatnace().setUseLte(true);
                procPlayIntro();
            }
        }else{
            procPlayIntro();
        }
    }

    /*
    현재 플레이할 에피소드 데이터
     */
    private void requestEpisodeData(String idx){
        ContentValues values = new ContentValues();
        values.put("ep_idx",idx);
        if(ConfigInfo.getInstance().getBandwidth() == 3)
            values.put("quality","hlsabr");
        else
            values.put("quality","his");
        values.put("id",UserInfo.getInstance().getUID());

        minimonEpisode.info(values);
    }

    private void responseEpisodeData(JSONObject info){
//        setData(info);
        String remainTime  = "";
        try {
            remainTime = info.getJSONObject("data").getJSONObject("list").getString("remaining_time").replaceAll("[^0-9]", "");
        }catch (JSONException e){
            e.printStackTrace();
        }
        int nRemainTime;
        if(remainTime.isEmpty()){
            nRemainTime = 0;
        }else{
            nRemainTime = Integer.parseInt(remainTime);
        }
        if(nRemainTime > 0){
            EpisodeInfo.getInsatnace().setResumePosition(nRemainTime);
            Log.d("setPosition-4", String.valueOf(EpisodeInfo.getInsatnace().getResumePosition()));
            setData(info);
            initializePlayer();
            initFullscreenButton();
            EpisodeInfo.getInsatnace().setIntro(false);
            updatePlayerVideo(EpisodeInfo.getInsatnace().getVideoUrl());
        }else {
            if (!prepareVideoFlag) {
                procPrepareVideo(info);
            } else {
                setData(info);
                initializePlayer();
                initFullscreenButton();
                String introVideoUrl = EpisodeInfo.getInsatnace().getIntroVideoUrl();
                final String _videoUrl = EpisodeInfo.getInsatnace().getVideoUrl();
                String currentVideoUrl = EpisodeInfo.getInsatnace().getCurrentVideoUrl();
                Log.d("currentVideoUrl", currentVideoUrl);
                Log.d("videoUrl", _videoUrl);
                Log.d("introVideoUrl", introVideoUrl);
                if (EpisodeInfo.getInsatnace().isIntro()) {
                    updatePlayerVideo(introVideoUrl);
                } else {
                    updatePlayerVideo(currentVideoUrl);
                }
            }
        }
    }

    private void initData(){
        minimonEpisode = new MinimonEpisode();
        minimonEpisode.setListener(new MinimonEpisode.MinimonEpisodeListener() {
            @Override
            public void onResponse(JSONObject info , String responseType) {
                try{
                    if("info".equals(responseType)){//현재 플레이할 에피소드 에이터
                       responseEpisodeData(info);
                    }else if("purchase".equals(responseType)){
                        procUsableLTE();
                    }else if("checked".equals(responseType)){
//                        Log.d("RequestChecked",((JSONObject)info).toString());
                        responseChecked(info);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }



    private void setData(JSONObject info){
       try{
           JSONObject list_mp = (JSONObject) info.getJSONObject("data").getJSONObject("list").getJSONObject("list_mp");
           JSONArray videoArr = (JSONArray) list_mp.get("video");

           JSONObject videoObj = null;
           if(ConfigInfo.getInstance().getBandwidth()!= 3)
               videoObj = (JSONObject) videoArr.get(ConfigInfo.getInstance().getBandwidth());
           else
               videoObj = (JSONObject) videoArr.get(0);
           Log.d("VideoObjTag",videoObj.toString());
           JSONArray episodeArr = (JSONArray)info.getJSONObject("data").getJSONObject("list").get("list_ep");
           JSONObject episodeInformation = (JSONObject)info.getJSONObject("data").get("list");
           setEpisodeData(episodeInformation);

           videoUrl = videoObj.getString("playUrl");
           String thumbnailUrl = getThumbnailImage(list_mp);
           EpisodeInfo episodeInfo = EpisodeInfo.getInsatnace();
           episodeInfo.setThumbnailUrl(thumbnailUrl);
           episodeInfo.setVideoUrl(videoUrl);
           String introVideoUrl = getIntroVideoUrl(list_mp);
           episodeInfo.setIntroVideoUrl(introVideoUrl);
           if (isChangeBandWidth && EpisodeInfo.getInsatnace().getResumePosition() != 0) {
               episodeInfo.setResumePosition(0);
               Log.d("setPosition-1", String.valueOf(EpisodeInfo.getInsatnace().getResumePosition()));
           }else if(isChangeBandWidth){
               isChangeBandWidth = false;
           }
//           else{
//               isChangeBandWidth = !isChangeBandWidth;
//           }

       }catch (JSONException e){
           Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
           return;
       }
   }

   private void procPrepareVideo(JSONObject obj){
        prepareVideoFlag = true;
        setData(obj);
        Picasso.with(this).load(EpisodeInfo.getInsatnace().getThumbnailUrl()).into(img_player_thumbnail);
        img_player_thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCheckEpisode();
            }
        });
   }

   private void requestCheckEpisode(){
       ContentValues values = new ContentValues();
       values.put("id",UserInfo.getInstance().getUID());
       values.put("ep_idx",EpisodeInfo.getInsatnace().getIdx());
       minimonEpisode.checked(values);
   }

    private void responseChecked(JSONObject info){
        try {
            String resCode = info.getString("resCode");
            String errCode = info.has("data")?info.getJSONObject("data").getString("errCode") : "0207";
            if (resCode.equals("0000") || (resCode.equals("0900") && errCode.equals("0207"))) {
                procUsableLTE();
            }else{
                if("0204".equals(errCode)){
                    confirmRefillPoint();
                }else if("205".equals(errCode)){
                    confirmAdult();
                }
            }
        }catch (JSONException e){

        }
    }

   private void procAdultContent(){
       EpisodeInfo info = EpisodeInfo.getInsatnace();
       if(info.getIsAdult().equals("1")){
           UserInfo userInfo = UserInfo.getInstance();
           if(userInfo.getCertificate().equals("1")){ //본인인증 되어있는 경우
               if(userInfo.getAdult().equals("1")){ //성인인 경우
                   procFreeContent();
               }else{
                   prepareVideoFlag = false;
                   new JUtil().alertNotice(this,"성인 콘테츠이며, 이용하실 수 없습니다.",null);
               }
           }else{
               confirmAdult();
           }
       }else{
           procFreeContent();
       }
   }

   private void confirmAdult(){
       prepareVideoFlag = false;
       new JUtil().confirmNotice(this, "본인인증 후 사용할 수 있습니다.", new JUtil.JUtilListener() {
           @Override
           public void callback(int id) {
               if (id == 1)
                   gotoAuthWeb();
           }
       });
   }

    public void gotoAuthWeb(){
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("pageUrl","/certificate/kmcert/request");
        intent.putExtra("pageName","Auth");
        intent.putExtra("pageKey","");
        intent.putExtra("pageValue","");
        startActivity(intent);
    }

    private void goToPayWeb(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("pageUrl","Contents/view");
        intent.putExtra("pageName","index");
        intent.putExtra("pageKey","");
        intent.putExtra("pageValue","");
        startActivity(intent);
    }

   private void procFreeContent(){
        EpisodeInfo episodeInfo = EpisodeInfo.getInsatnace();
        int EpisodeCost =Integer.parseInt(episodeInfo.getPoint());
        if(EpisodeCost > 0){
            UserInfo userInfo = UserInfo.getInstance();
            if(Integer.parseInt(userInfo.getPoint()) > EpisodeCost){
//                procUsableLTE();
                procConfirmPayEpisode();
            }else{
                confirmRefillPoint();
            }
        }else{
            procUsableLTE();
        }
   }

   private void confirmRefillPoint(){
       prepareVideoFlag = false;
       new JUtil().confirmNotice(this, "포인트를 충전하고 이용하십시오.", new JUtil.JUtilListener() {
           @Override
           public void callback(int id) {
               if(id == 1){
                   goToPayWeb();
               }
           }
       });
   }

   private void procConfirmPayEpisode(){
       new JUtil().confirmNotice(this, "컨텐츠를 구매하시겠습니까?\n" +
               "구매 후 72시간 동안 재시청 가능합니다.\n" +
               EpisodeInfo.getInsatnace().getPoint()+"포인트가 차감됩니다.", new JUtil.JUtilListener() {
           @Override
           public void callback(int id) {
               if(id == 1){
//                   goToPayWeb();
                   ContentValues values = new ContentValues();
                   values.put("id",UserInfo.getInstance().getUID());
                   values.put("ep_idx",EpisodeInfo.getInsatnace().getIdx());
                   minimonEpisode.purchase(values);
               }else{
                   prepareVideoFlag = false;
               }
           }
       });
   }

   private String getThumbnailImage(JSONObject info){
       String resultUrl = "";
       try{
           resultUrl = info.getString("image");
       }catch (JSONException e){
           e.printStackTrace();
       }
       return resultUrl;

   }

   private String getIntroVideoUrl(JSONObject info){
       String resultUrl = "";
       try{
           resultUrl = info.getJSONArray("intro").getJSONObject(EpisodeInfo.getInsatnace().getBandwidth()).getString("playUrl");
       }catch (JSONException e){
           e.printStackTrace();
       }
       return resultUrl;
   }

   private void procPlayIntro(){
       initializePlayer();
       initFullscreenButton();
       if(isReplayVideoFlag){
           procPlayVideo();
       }else{
           EpisodeInfo.getInsatnace().setIntro(true);
           updatePlayerVideo(EpisodeInfo.getInsatnace().getIntroVideoUrl());
       }
   }

   public interface playListener{
       void endPlay();
   }

   private void procPlayVideo(){
       EpisodeInfo.getInsatnace().setIntro(false);
       updatePlayerVideo(EpisodeInfo.getInsatnace().getVideoUrl());
   }

   private void setEpisodeData(JSONObject obj){
       try {
           EpisodeInfo info = EpisodeInfo.getInsatnace();
           info.setTitle(obj.getString("title"));
           info.setC_idx(obj.getString("c_idx"));
           info.setIdx(obj.getString("idx"));
           info.setEp(obj.getString("ep"));
           info.setRemainingTime(obj.getString("remaining_time"));
           info.setIsAdult(obj.getString("is_adult"));
           info.setPoint(obj.getString("point"));
           info.setPlayTime(obj.getString("play_time"));
           info.setGrade(obj.getString("grade"));

           c_title = obj.getString("c_title");
//           setTitle(c_title);
           tv_frag_title.setText(c_title);
           nowEp = obj.getString("ep");
           JSONArray jarr = obj.getJSONArray("list_tag");
           String tags = "";
           for(int i=0; i<jarr.length(); i++){
               JSONObject objTag = (JSONObject) jarr.get(i);
               tags += "#"+objTag.getString("tag") + " ";
           }
       }catch (JSONException e){
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
        if(this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            mLockScreen.setImageResource(R.mipmap.a022_play_b_lock_on);
        else
            mLockScreen.setImageResource(R.mipmap.a022_play_b_lock_off);

        mLockScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DramaPlayActivity.this.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_SENSOR){//잠겨있던 잠금 풀 경우
                    DramaPlayActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    mLockScreen.setImageResource(R.mipmap.a022_play_b_lock_off);
                    isLockSreen = !isLockSreen;
                }else{ //다시 잠글경우
                    DramaPlayActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
                    mLockScreen.setImageResource(R.mipmap.a022_play_b_lock_on);
                    isLockSreen = !isLockSreen;
                }
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
                EpisodeInfo.getInsatnace().setResumePosition(Math.max(0, playerView.getPlayer().getContentPosition()));
                requestEpisodeData(EpisodeInfo.getInsatnace().getIdx());
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
                EpisodeInfo.getInsatnace().setResumePosition(Math.max(0, playerView.getPlayer().getContentPosition()));
                requestEpisodeData(EpisodeInfo.getInsatnace().getIdx());
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
                EpisodeInfo.getInsatnace().setResumePosition(Math.max(0, playerView.getPlayer().getContentPosition()));
                requestEpisodeData(EpisodeInfo.getInsatnace().getIdx());
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
                EpisodeInfo.getInsatnace().setResumePosition(Math.max(0, playerView.getPlayer().getContentPosition()));
                requestEpisodeData(EpisodeInfo.getInsatnace().getIdx());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG+"Test","OnStart");
        if (Util.SDK_INT > 23) {
            initData();
//            if(EpisodeInfo.getInsatnace().getIdx()==null || EpisodeInfo.getInsatnace().getIdx().isEmpty()) {
//                requestEpisodeData("645");
//            }
//            else {
                requestEpisodeData(EpisodeInfo.getInsatnace().getIdx());
//            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG+"Test","OnResume");
        isActive = true;
//        registerReceiver(broadcastReceiver, intentFilter);
        createWifiMonitor();
        if (Util.SDK_INT <= 23) {
            initData();
//            if(EpisodeInfo.getInsatnace().getIdx()==null || EpisodeInfo.getInsatnace().getIdx().isEmpty())
//                sendEpisodeData("645");
//            else
            requestEpisodeData(EpisodeInfo.getInsatnace().getIdx());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG+"Test","OnPause");
        isActive = false;
//        unregisterReceiver(broadcastReceiver);
        removeWifiMoniter();
        if (Util.SDK_INT <= 23) {
            if(playerView!= null&& playerView.getPlayer()!=null){
                EpisodeInfo.getInsatnace().setResumePosition(Math.max(0, playerView.getPlayer().getContentPosition()));
                Log.d("setPosition-2", String.valueOf(EpisodeInfo.getInsatnace().getResumePosition()));
                releasePlayer();
            }
        }
   }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG+"Test","OnStop");
        if (Util.SDK_INT > 23) {
            if(playerView!= null&& playerView.getPlayer()!=null){
                EpisodeInfo.getInsatnace().setResumePosition(Math.max(0, playerView.getPlayer().getContentPosition()));
                Log.d("setPosition-3", String.valueOf(EpisodeInfo.getInsatnace().getResumePosition()));
                releasePlayer();
            }
        }
    }

    private void goFullScreen(){
        EpisodeInfo.getInsatnace().setResumePosition(Math.max(0, playerView.getPlayer().getContentPosition()));
        Intent intent = new Intent(DramaPlayActivity.this, VideoPlayScreenActivity.class);
        startActivity(intent);
    }

    private void initializePlayer() {
        view_player_thumbnail.setVisibility(View.GONE);
        playerView.setVisibility(View.VISIBLE);
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

            player.setPlayWhenReady(true);
        }

//        MediaSource mediaSources = buildMediaSource(Uri.parse(playingVideoUrl), "mp4");
//        playerView.getPlayer().prepare(mediaSources, true, false);
//        if(EpisodeInfo.getInsatnace().getResumePosition() != 0){
//            playerView.getPlayer().seekTo(EpisodeInfo.getInsatnace().getResumePosition());
//            playerView.getPlayer().setPlayWhenReady(true);
//        }
    }

    private void updatePlayerVideo(String playUrl){
        EpisodeInfo.getInsatnace().setCurrentVideoUrl(playUrl);
        MediaSource mediaSources = buildMediaSource(Uri.parse(playUrl), "mp4");
        playerView.getPlayer().prepare(mediaSources, true, false);
        Log.d("DramaUpdatePlayerVideo",String.valueOf(EpisodeInfo.getInsatnace().getResumePosition()));
        if(EpisodeInfo.getInsatnace().getResumePosition() != 0){
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
//        prepareVideoFlag = false;
//        isReplayVideoFlag = false;
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

    @Override
    public void onResponseHtml(String html, String baseUrl) {
        Log.d("onResponseDramaPlay",baseUrl);
        Log.d("onResponseDramaPlayHtml",html);
        mWebView.loadDataWithBaseURL(baseUrl, html, "text/html", "utf-8", null);
    }

    @Override
    public void onGoToWeb(String url, String page, String key, String value) {
        UserInfo info = UserInfo.getInstance();
        ContentValues content = new ContentValues();
        content.put("id", info.getUID());
        content.put("loc", "Android");
        content.put("page", page);
        if("episode".equals(page)) {
            Log.d("episodeValue",value);
            goToEpisodeMain(url,page,key,value);
            return;
        }
        Log.d("WebViewFragmentisSearch","page is not Search");
        content.put(key, value);
        Log.d("onGoToWebUID", info.getUID());
        minimonWebView.goToWeb(url, content);
    }

    private void goToEpisodeMain(String url, String page, String key, String value)
    {
        Intent intent = new Intent(this,DramaPlayActivity.class);
        intent.putExtra("url",url);
        intent.putExtra("page",page);
        intent.putExtra("key",key);
        intent.putExtra("value",value);
        startActivity(intent);
    }

    @Override
    public void closeRefreshWeb(String url, String page, String key, String value) {
        Log.d("closeRefreshWeb", url+","+page+","+key+","+value);
    }

    @Override
    public void closeDepthRefreshWeb(String depth) {

    }

    @Override
    public void goToPg(String url, String item, String how) {

    }

    @Override
    public void goToSearch() {

    }

    @Override
    public void changePlayer(String idx) {
        Log.d("DramaPlayChangePlayer",idx);
//        EpisodeInfo.getInsatnace().setIdx(idx);
        prepareVideoFlag = false;
        requestEpisodeData(idx);
//        requestCheckEpisode();
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
            if("STATE_READY".equals(stateString)){
                playerView.hideController();
            }
            if("STATE_ENDED".equals(stateString)){
                Log.d("exoPlayer", stateString);
                if(m_playlistener != null) {
                    m_playlistener.endPlay();
                    Log.d("exoPlayer", "endPlay");
                } else {
                    Log.d("exoPlayer", "m_playlistener null");
                }
            }
//            if("STATE_READY".equals(stateString) && playWhenReady){
//                checkWifi();
//            }
            Log.d(TAG, "DramaPlaystate [" + playWhenReady + ", " + stateString + "]");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferences pref = getSharedPreferences("minimon-preference",MODE_PRIVATE);
        EpisodeInfo.getInsatnace().setResumePosition(0);
        ConfigInfo.getInstance().setBandwidth(pref.getInt("BandWidth",1));
//        unregisterReceiver(broadcastReceiver);
    }
}
