package com.minimon.diocian.player;

import android.app.Fragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class WebViewFragment extends Fragment implements MainActivity.onKeypressListenr, MyWebChromeClient.ProgressListener{
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
        mProgressBar = view.findViewById(R.id.progress_bar);
        mWebView = view.findViewById(R.id.webview_other);
        mWebView.setWebViewClient(new MyWebviewClient(getActivity(),mProgressBar));
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.loadUrl(ConfigInfo.getInstance().getWebViewUrl());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new JavascriptInterface(getActivity(),mWebView),"minimon");
//        getActivity().findViewById(R.id.view_main_toolbar).setVisibility(View.VISIBLE);
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
}
