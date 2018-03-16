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
        MyWebviewClient.myWebViewClientListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_WEBVIEW = "webViewUrl";

    private ObservableWebView mWebView;
    private ProgressBar mProgressBar;
    private MinimonWebView minimonWebView;
    private JavascriptInterface javascriptInterface;

    private RelativeLayout view_main_toolbar;
    private RelativeLayout view_main_toolbar2;
    private TextView main_tv_frag_title;

    private List<String> arrPageNameHistory = new ArrayList<>();

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();


    private String mPage;
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

        mPage = WebViewInfo.getInstance().getPageName();

        if(minimonWebView == null){
            minimonWebView = new MinimonWebView();
            minimonWebView.setListener(this);
        }
        javascriptInterface = new JavascriptInterface(getActivity(), mWebView);
        javascriptInterface.setListener(this);

        view_main_toolbar = getActivity().findViewById(R.id.view_main_toolbar);
        view_main_toolbar2 = getActivity().findViewById(R.id.view_main_toolbar2);
        main_tv_frag_title = getActivity().findViewById(R.id.tv_frag_title);

        mWebView = (ObservableWebView) view.findViewById(R.id.webview_other);
        mProgressBar = view.findViewById(R.id.progress_bar);
        MyWebviewClient client = new MyWebviewClient(getActivity(), mProgressBar);
        client.setMyWebViewClientListener(this);
        mWebView.setWebViewClient(client);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.addJavascriptInterface(javascriptInterface, "minimon");
        mWebView.getSettings().setJavaScriptEnabled(true);
        view_main_toolbar = getActivity().findViewById(R.id.view_main_toolbar);
        mWebView.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback() {
            @Override
            public void onScroll(int l, int t) {
                if("main".equals(mPage) || "channel".equals(mPage)|| "episode".equals(mPage)){
                    if(t!=0){
                        view_main_toolbar.setBackgroundColor(Color.parseColor("#BFFB450B"));
                    }else{
                        view_main_toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
                    }
                }
            }
        });
        changeToolbar();
    }

    public void clearHistory(){
        if(mWebView!=null)
            mWebView.clearHistory();
    }

    public void moveWebUrl(){
        UserInfo info = UserInfo.getInstance();
        if(minimonWebView == null){
            minimonWebView = new MinimonWebView();
            minimonWebView.setListener(this);
        }
        ContentValues content = new ContentValues();
        content.put("id", info.getUID());
        content.put("loc", "Android");
        content.put("page", WebViewInfo.getInstance().getPageName());
        arrPageNameHistory.add(WebViewInfo.getInstance().getPageName());
        Log.d("FragmentCreated",WebViewInfo.getInstance().getPageName());
        if("search".equals(WebViewInfo.getInstance().getPageName())){
            content.put("searchTag",WebViewInfo.getInstance().getSearch_tag());
        }
        minimonWebView.goToWeb("Contents/view", content);
//        if(mWebView!=null)
//            changeToolbar();
    }

    public void changeToolbar(){
        if(mWebView.canGoBack()){
            view_main_toolbar.setVisibility(View.GONE);
            view_main_toolbar2.setVisibility(View.VISIBLE);
        }else{
            view_main_toolbar.setVisibility(View.VISIBLE);
            view_main_toolbar2.setVisibility(View.GONE);
        }
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
            arrPageNameHistory.remove(arrPageNameHistory.size()-1);
            String temptS = "";
            if(arrPageNameHistory.size() > 0)
                temptS = arrPageNameHistory.get(arrPageNameHistory.size()-1);
//            main_tv_frag_title.setText(temptS);
            setToolbarTitle(temptS);
            changeToolbar();
        } else {
            MainActivity activity = (MainActivity) getActivity();
            activity.setOnKeypressListener(null);
            activity.onBackPressed();
        }
    }


    private void setToolbarTitle(String pageN){
        if(getResources().getString(R.string.page_name_index).equals(pageN)){
            main_tv_frag_title.setText("이용권");
        }else if(getResources().getString(R.string.page_name_purchase).equals(pageN)){
            main_tv_frag_title.setText(getResources().getString(R.string.menu_watch));
        }else if(getResources().getString(R.string.page_name_like).equals(pageN)){
            main_tv_frag_title.setText(getResources().getString(R.string.menu_favorite));
        }else if(getResources().getString(R.string.page_name_keep).equals(pageN)){
            main_tv_frag_title.setText(getResources().getString(R.string.menu_subscribe));
        }else if(getResources().getString(R.string.page_name_point_list).equals(pageN)){
            main_tv_frag_title.setText(getResources().getString(R.string.menu_point_history));
        }else if(getResources().getString(R.string.page_name_pay_list).equals(pageN)){
            main_tv_frag_title.setText(getResources().getString(R.string.menu_pay_history));
        }else if(getResources().getString(R.string.page_name_notice).equals(pageN)){
            main_tv_frag_title.setText("공지사항");
        }else if(getResources().getString(R.string.page_name_faq).equals(pageN)){
            main_tv_frag_title.setText("FAQ");
        }else if(getResources().getString(R.string.page_name_qna_list).equals(pageN)){
            main_tv_frag_title.setText("1:1 QNA");
        }else if(getResources().getString(R.string.page_name_policy).equals(pageN)){
            main_tv_frag_title.setText("이용약관");
        }else if(getResources().getString(R.string.page_name_cookie_list).equals(pageN)){
            main_tv_frag_title.setText("최근 본 영상");
        }else{
            main_tv_frag_title.setText("");
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
        Log.d("onGoToWeb", url+","+page+","+key+","+","+value);
        UserInfo info = UserInfo.getInstance();
        ContentValues content = new ContentValues();
        content.put("id", info.getUID());
        content.put("loc", "Android");
        content.put("page", page);
        mPage = page;

        if("episode".equals(page)) {
            Log.d("ongotodramaplay",url+","+page+","+key+","+value);
           goToEpisodeMain(url,page,key,value);
            return;
        }
        if("search".equals(page)) {
            Log.d("WebViewFragmentisSearch","page is Search");
            content.put(key, WebViewInfo.getInstance().getSearch_tag());
        }
        else {
            Log.d("WebViewFragmentisSearch","page is not Search");
            Log.d("WebViewFragmentvalue",key+","+value);
            content.put(key, value);
        }
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
//        changeToolbar();
//        if(!mPage.equals("main") || !"channel".equals(mPage)|| !"episode".equals(mPage) || mWebView.canGoBack())
//            view_main_search.setBackgroundColor(getResources().getColor(R.color.MainColor));
    }

    @Override
    public void closeRefreshWeb(String url, String page, String key, String value) {
        UserInfo info = UserInfo.getInstance();
        ContentValues content = new ContentValues();
        content.put("id", info.getUID());
        content.put("loc", "Android");
        content.put("page", page);
        mPage = page;
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
        Log.d("goToPgValues",url+","+item+","+how);
        UserInfo info = UserInfo.getInstance();
        ContentValues content = new ContentValues();
        content.put("id", info.getUID());
        content.put("loc", "Android");
        content.put("item",item);
        content.put("how",how);
        minimonWebView.goToPayWeb(url,content);
    }

    @Override
    public void goToSearch() {

    }

    @Override
    public void changePlayer(String idx) {

    }

    @Override
    public void loadingFinished() {
        if("main".equals(WebViewInfo.getInstance().getPageName())){
            WebViewInfo.getInstance().setPageName("");
            mWebView.clearHistory();
        }
        changeToolbar();
        WebViewInfo.getInstance().setPageName("");
    }
}