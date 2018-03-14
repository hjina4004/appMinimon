package com.minimon.diocian.player;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;


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
        JavascriptInterface.JavascriptInterfaceListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_WEBVIEW = "webViewUrl";

    private WebView mWebView;
    private ProgressBar mProgressBar;
    private MinimonWebView minimonWebView;
    private JavascriptInterface javascriptInterface;

    // TODO: Rename and change types of parameters

    public WebViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
//     * @param param1 Parameter 1.
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
        javascriptInterface = new JavascriptInterface(getActivity(),mWebView);
        javascriptInterface.setListener(this);

        mWebView = view.findViewById(R.id.webview_other);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mWebView.setWebViewClient(new MyWebviewClient(getActivity(),mProgressBar));
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.addJavascriptInterface(javascriptInterface,"minimon");
        mWebView.getSettings().setJavaScriptEnabled(true);

        UserInfo info = UserInfo.getInstance();

        ContentValues content = new ContentValues();
        content.put("id", info.getUID());
        content.put("loc","Android");
        content.put("page",WebViewInfo.getInstance().getPageName());
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
        if(mWebView.canGoBack()){
            mWebView.goBack();
        }else{
            MainActivity activity = (MainActivity) getActivity();
            activity.setOnKeypressListener(null);
            activity.onBackPressed();
        }
    }

    @Override
    public void onUpdateProgress(int progressValue) {
        mProgressBar.setProgress(progressValue);
        if(progressValue == 100){
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
        Log.d("onGoToWeb",url);
        UserInfo info = UserInfo.getInstance();
        ContentValues content = new ContentValues();
        content.put("id",info.getUID());
        content.put("loc","Android");
        content.put("page",page);
        content.put(key,value);
        Log.d("onGoToWebUID",info.getUID());
        minimonWebView.goToWeb(url, content);
    }

    @Override
    public void onResponseHtml(String html, String baseUrl) {
        Log.d("BasrUrl",baseUrl);
        Log.d("BaseUrlHtml",html);
        mWebView.loadDataWithBaseURL(baseUrl,html,"text/html","utf-8",null);
    }

    @Override
    public void closeRefreshWeb(String url, String page, String key, String value) {
        UserInfo info = UserInfo.getInstance();
        ContentValues content = new ContentValues();
        content.put("id",info.getUID());
        content.put("loc","Android");
        content.put("page",page);
        content.put(key,value);
        Log.d("onGoToWebUID",info.getUID());
        minimonWebView.goToWeb(url, content);
    }

    @Override
    public void closeDepthRefreshWeb(String depth) {
        if(mWebView.canGoBackOrForward(Integer.parseInt(depth)))
            mWebView.goBackOrForward(Integer.parseInt(depth));
    }

    @Override
    public void goToPg(String url, String item, String how) {

    }

    @Override
    public void goToSearch() {

    }
}
