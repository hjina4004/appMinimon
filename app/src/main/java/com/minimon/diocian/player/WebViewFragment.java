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
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import android.widget.RelativeLayout;
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
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.util.KakaoParameterException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        JavascriptInterface.JavascriptInterfaceListener,
        MyWebviewClient.myWebViewClientListener,
        ObservableWebView.gestureListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_WEBVIEW = "webViewUrl";

    private ObservableWebView mWebView;
    private ProgressBar mProgressBar;
    private MinimonWebView minimonWebView;
    private MainActivity mActivity;
    private JavascriptInterface javascriptInterface;

    private RelativeLayout view_main_toolbar;
    private RelativeLayout view_main_toolbar2;
    private TextView tv_frag_title;
    private FrameLayout view_main;

//    private List<String> arrPageNameHistory = new ArrayList<>();
    private String webViewPageName;
    private String webViewUrl;
    private String webViewKey;
    private String webViewValue;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    public WebViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mActivity = (MainActivity) getActivity();
        mActivity.setOnKeypressListener(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        webViewPageName = getArguments().getString("pageName");
        webViewUrl = getArguments().getString("pageUrl");
        webViewKey = getArguments().getString("pageKey");
        webViewValue = getArguments().getString("pageValue");
        return inflater.inflate(R.layout.fragment_web_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mPage = WebViewInfo.getInstance().getPageName();

        if(minimonWebView == null){
            minimonWebView = new MinimonWebView();
            minimonWebView.setListener(this);
        }
        javascriptInterface = new JavascriptInterface(getActivity(), mWebView);
        javascriptInterface.setListener(this);

        view_main_toolbar = getActivity().findViewById(R.id.view_main_toolbar);
        view_main_toolbar2 = getActivity().findViewById(R.id.view_main_toolbar2);
        tv_frag_title = getActivity().findViewById(R.id.tv_frag_title);
        view_main = getActivity().findViewById(R.id.view_main);

        mWebView = (ObservableWebView) view.findViewById(R.id.webview_other);
        mProgressBar = view.findViewById(R.id.progress_bar);
        MyWebviewClient client = new MyWebviewClient(getActivity(), mProgressBar);
        client.setMyWebViewClientListener(this);
        mWebView.setWebViewClient(client);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.addJavascriptInterface(javascriptInterface, "minimon");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setListener(this);
//        mWebView.gestListener = new GestureDetector.SimpleOnGestureListener(){
//            @Override
//            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
////                if (e1.getRawY() > e2.getRawY()) {
////                    Log.i("WebView", "[swipe] up");
////                } else {
////                    Log.i("WebView", "[swipe] down");
////                }
//                if("main".equals(webViewPageName) || "channel".equals(webViewPageName)){
//                    Log.d("webViewPageName",webViewPageName);
//                    if(e1.getRawY() > e2.getRawY()){
//                        Log.d("webViewPageName","tisbigger");
//                        view_main_toolbar.setVisibility(View.GONE);
//                    }else if (e1.getRawY() < e2.getRawY()){
//                        Log.d("webViewPageName","oldtisbigger");
//                        view_main_toolbar.setVisibility(View.VISIBLE);
//                    }
//                }else{
//                    if(e1.getRawY() > e2.getRawY()){
//                        view_main_toolbar2.setVisibility(View.GONE);
//                    }else if (e1.getRawY() < e2.getRawY()){
//                        view_main_toolbar2.setVisibility(View.VISIBLE);
//                    }
//                }
//                return super.onFling(e1, e2, velocityX, velocityY);
//            }
//        };

//        mWebView.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback() {
//            @Override
//            public void onScroll(int l, int t, int oldl, int oldt) {
//                int availableDepth = 30;
//                if("main".equals(webViewPageName) || "channel".equals(webViewPageName)){
//                    Log.d("webViewPageName",webViewPageName);
//                    if(t-oldt > availableDepth){
//                        Log.d("webViewPageName","tisbigger");
//                        view_main_toolbar.setVisibility(View.GONE);
//                    }else if (oldt-t > availableDepth){
//                        Log.d("webViewPageName","oldtisbigger");
//                        view_main_toolbar.setVisibility(View.VISIBLE);
//                    }
//                }else{
//                    if(t-oldt > availableDepth){
//                        view_main_toolbar2.setVisibility(View.GONE);
//                    }else if (oldt-t > availableDepth){
//                        view_main_toolbar2.setVisibility(View.VISIBLE);
//                    }
//                }
////                if(t!=0){
////                    view_main_toolbar.setBackgroundColor(Color.parseColor("#"+"BF"+"FB450B"));
////                }else{
////                    view_main_toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
////                }
//
//            }
//        });
        moveWebUrl();
    }

    @Override
    public void onSwipeUp() {
        if("main".equals(webViewPageName) || "channel".equals(webViewPageName)){
            view_main_toolbar.setVisibility(View.GONE);
        }

    }

    @Override
    public void onSwipeDown() {
        if("main".equals(webViewPageName) || "channel".equals(webViewPageName)){
            view_main_toolbar.setVisibility(View.VISIBLE);
        }
    }

    public void moveWebUrl(){
        UserInfo info = UserInfo.getInstance();
        if(minimonWebView == null){
            minimonWebView = new MinimonWebView();
            minimonWebView.setListener(this);
        }
        ContentValues content = new ContentValues();
        content.put("id", info.getUID());
        if("paying".equals(webViewPageName)){
            content.put("item",getArguments().getString("item"));
            content.put("how",getArguments().getString("how"));
            content.put("loc", "Android");
            minimonWebView.goToPayWeb(webViewUrl, content);
        }else {
            content.put("page", webViewPageName);
            if ("search".equals(webViewPageName)) {
                content.put("search_tag", WebViewInfo.getInstance().getSearch_tag());
                content.put("loc", "Android");
            } else {
                if(!webViewKey.isEmpty() && webViewKey != null && !webViewPageName.equals("Auth")) {
                    content.put(webViewKey, webViewValue);
                    content.put("loc","Android");
                }else{
                    content.put("loc","android");
                }
            }
            minimonWebView.goToWeb(webViewUrl, content);
        }
    }

    @Override
    public void onBack() {
        if (mWebView.canGoBack() && !"paying".equals(webViewPageName)) {
            mWebView.goBack();
        } else {
            MainActivity activity = (MainActivity) getActivity();
            activity.setOnKeypressListener(null);
            activity.onBackPressed();
        }
    }

    private void setAcListener(){
        mActivity.setOnKeypressListener(this);
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

    @Override
    public void onGoToWeb(String url, String page, String key, String value) {
        Log.d("onGoToWeb", url+","+page+","+key+","+","+value);
        UserInfo info = UserInfo.getInstance();
        ContentValues content = new ContentValues();
        content.put("id", info.getUID());
        content.put("page", page);

        if("episode".equals(page)) {
            Log.d("ongotodramaplay",url+","+page+","+key+","+value);
           goToEpisodeMain(url,page,key,value);
            return;
        }
        else if("search".equals(page)) {
            Log.d("WebViewFragmentisSearch","page is Search");
            content.put(key, WebViewInfo.getInstance().getSearch_tag());
            content.put("loc", "Android");
        }
        else {
            Log.d("WebViewFragmentisSearch","page is not Search");
            Log.d("WebViewFragmentvalue",key+","+value);
            if(key != null && !key.isEmpty()) {
                content.put(key, value);
                content.put("loc", "Android");
            }else{
                content.put("loc", "android");
            }
        }
        if(page == null || page.isEmpty() || "".equals(page)){
            minimonWebView.goToWeb(url, content);
        }
        else
            goToWebMain(url,page,key,value);
//        minimonWebView.goToWeb(url, content);
    }

    private void goToEpisodeMain(String url, String page, String key, String value)
    {
        EpisodeInfo.getInsatnace().setIdx(value);
        Intent intent = new Intent(getActivity(), DramaPlayActivity.class);
        intent.putExtra("url",url);
        intent.putExtra("page",page);
        intent.putExtra("key",key);
        intent.putExtra("value",value);
        startActivity(intent);
    }

    private void goToWebMain(String url, String page, String key, String value){
        Intent intent = new Intent(getActivity(),MainActivity.class);
        intent.putExtra("pageUrl",url);
        intent.putExtra("pageName",page);
        intent.putExtra("pageKey",key);
        intent.putExtra("pageValue",value);
        startActivity(intent);
    }

    private void goToPayWeb(String url, String item, String how, String title){
        Intent intent = new Intent(getActivity(),MainActivity.class);
        intent.putExtra("pageUrl",url);
        intent.putExtra("pageName","paying");
        intent.putExtra("item",item);
        intent.putExtra("how",how);
        intent.putExtra("title",title);
        startActivity(intent);
    }



    @Override
    public void onResponseHtml(String html, String baseUrl) {
        setAcListener();
        mWebView.loadDataWithBaseURL(baseUrl, html, "text/html", "utf-8", null);
    }

    @Override
    public void closeRefreshWeb(String url, String page, String key, String value) {
        getActivity().finish();
        goToWebMain(url,page,key,value);
    }

    @Override
    public void closeDepthRefreshWeb(String depth) {
        Log.d("closeDepthRefreshWeb","inWebViewFragment");
//        ArrayList<WebViewHistory> arrWebViewHistory = WebViewInfo.getInstance().getWebviewHistory();
//        int webDepth = Integer.valueOf(depth);
//        WebViewHistory depthHistory = arrWebViewHistory.get(arrWebViewHistory.size()-webDepth);
//        for(int i=0;i<webDepth; i++){
//            arrWebViewHistory.remove(arrWebViewHistory.size()-1);
//        }
//        goToWebMain(depthHistory.getPageUrl(), depthHistory.getPageName(), depthHistory.getPageKey(), depthHistory.getPageValue());
//        goToWebMain("main","","","");

        int dep  = Integer.parseInt(depth);
        if(dep > 1)
            getActivity().finish();
        WebViewHistory history = null;
        for(int i=0;i<dep;i++){
            history = WebViewInfo.getInstance().historyPop();
        }
        if("goToPayWeb".equals(history.getPageType())){
            minimonWebView.goToPayWeb(history.getPageUrl(), history.getContent());
        }else{
            minimonWebView.goToWeb(history.getPageUrl(), history.getContent());
        }

//        Intent intent = new Intent(getActivity().getApplicationContext(), MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
    }

    @Override
    public void goToPg(String url, String item, String how, String title) {
        Log.d("goToPgValues",url+","+item+","+how + "," + title);
        goToPayWeb(url,item,how,title);
    }

    @Override
    public void closeWebView() {
        getActivity().finish();
    }

    @Override
    public void goToSearch() {}

    @Override
    public void changePlayer(String idx) {

    }

    @Override
    public void loadingFinished() {
    }

    @Override
    public void setTitle(String title) {
        tv_frag_title.setText(title);
    }

    @Override
    public void closeGoToNative(String page) {
        if ("intro".equals(page)) {
            getActivity().finish();
            Intent intent = new Intent(getActivity().getApplicationContext(), GateActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            goToWebMain("Contents/view", page, "", "");
        }
    }

    @Override
    public void shareSNS(String loc, String url, String img) {
        // shareSNS: TT, http://www.minimon.com/semoy1, https://static.minimon.com/content/2018/03/06/eb018323d00f8635c3f309535c97d87e.jpg
        // shareSNS: GG, http://www.minimon.com/semoy1, https://static.minimon.com/content/2018/03/06/eb018323d00f8635c3f309535c97d87e.jpg
        // shareSNS: FB, http://www.minimon.com/semoy1, https://static.minimon.com/content/2018/03/06/eb018323d00f8635c3f309535c97d87e.jpg
        // shareSNS: KK, http://www.minimon.com/semoy1, https://static.minimon.com/content/2018/03/06/eb018323d00f8635c3f309535c97d87e.jpg
    }

    private void shareKakaotalk() {
        try {
            KakaoLink link = KakaoLink.getKakaoLink(getActivity().getApplicationContext());
            KakaoTalkLinkMessageBuilder builder=link.createKakaoTalkLinkMessageBuilder();

            builder.addText("Minimon");
            builder.addAppButton("앱으로 이동하기");
            link.sendMessage(builder, getActivity().getApplicationContext());

        } catch (KakaoParameterException e) {
            e.printStackTrace();
        }
    }
}