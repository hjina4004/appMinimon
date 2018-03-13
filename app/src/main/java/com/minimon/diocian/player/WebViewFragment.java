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
public class WebViewFragment extends Fragment implements MainActivity.onKeypressListenr, MyWebChromeClient.ProgressListener, MinimonUser.MinimonUserListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_WEBVIEW = "webViewUrl";

    private WebView mWebView;
    private ProgressBar mProgressBar;

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

        mWebView = view.findViewById(R.id.webview_other);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mWebView.setWebViewClient(new MyWebviewClient(getActivity(),mProgressBar));
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.getSettings().setJavaScriptEnabled(true);

//        MinimonUser user = new MinimonUser();
//        UserInfo userInfo = UserInfo.getInstance();
//        ContentValues content = new ContentValues();
//        content.put("id", userInfo.getUID());
//        content.put("loc","Android");
//        content.put("page","main");
//        user.setListener(this);
//        user.goToMain(content);
//        return;


        UserInfo info = UserInfo.getInstance();
        String postValue = "id="+info.getUID()+"&loc=Android"+"&page=main";
        HashMap<String,String> hashMap = new HashMap<String,String>();


        hashMap.put("Authorization",info.getToken());
//        mWebView.loadUrl(ConfigInfo.getInstance().getWebViewUrl()+"?"+postValue,null);

        mWebView.loadUrl(ConfigInfo.getInstance().getWebViewUrl()+"?"+postValue,hashMap);
//
//        mWebView.addJavascriptInterface(new JavascriptInterface(getActivity(),mWebView),"minimon");
////        getActivity().findViewById(R.id.view_main_toolbar).setVisibility(View.VISIBLE);
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

    @Override
    public void onResponseHtml(String html) {
        Log.d("PostAPITOKEN", html);
        mWebView.loadData(html, "text/html","utf-8");
    }
}
