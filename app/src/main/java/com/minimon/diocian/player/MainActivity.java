package com.minimon.diocian.player;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
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
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.OnConnectionFailedListener,
        DBHelper.dbHelperListenr,
        SearchhistoryAdapter.SearchHistoryAdapterListener{

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
    private static final String TAG = "MainActivity";
    private final String PREF_NAME = "minimon-preference";

    private RelativeLayout view_main_toolbar;
    private ImageView img_toolbar;
    private ImageView img_toolbar_search;
    private ImageView tv_toolbar_go_back;
    public EditText ed_toolbar_search;
    private DrawerLayout drawer;
    private RelativeLayout view_delete_search_history;
    private RecyclerView rec_search_history;

    private RelativeLayout view_main_toolbar2;
    private ImageView tv_toolbar_frag_go_back2;
    private TextView tv_frag_title;

    private SearchhistoryAdapter adapter;
    private LinearLayoutManager manager;
    private List<SearchItem> arrHistory = new ArrayList<SearchItem>();
    private LinearLayoutManager manager2;

    // for Google
    private static final int GOOGLE_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;

    private DBHelper dbHelper;

    private WebViewFragment webViewFragment;
    private String mPageName;
    private String mPageUrl;
    private String mPageKey;
    private String mPageValue;

    @Override
    public void onSucess(JSONObject data) {
        try{
            String funcName = data.getString("functionName");
            if("update".equals(funcName)){
                String history = data.getString("history");
                int index = findAtList(history);
                SearchItem item = new SearchItem();
                item.setHistory(data.getString("history"));
                item.setDate(data.getString("date"));
                arrHistory.set(index,item);
            }else if("insert".equals(funcName)){
                SearchItem item = new SearchItem();
                item.setHistory(data.getString("history"));
                item.setDate(data.getString("date"));
                arrHistory.add(item);
            }
        }catch (JSONException e){

        }

    }

    @Override
    public void onFail(JSONObject data) {

    }

    private int findAtList(String history){
        for(int i=0; i<arrHistory.size(); i++){
            SearchItem item = arrHistory.get(i);
            if(item.getHistory().equals(history)){
                return i;
            }
        }
        return -1;
    }

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

        dbHelper = new DBHelper(getApplicationContext(),"SearchLog.db",null,1);
        dbHelper.setListener(this);
        arrHistory = dbHelper.getResult();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setBackgroundColor(getResources().getColor(R.color.colorBaseBG));

        initGoogle();

        Log.v(TAG, "User Info --- " + UserInfo.getInstance().getData());
        view_main_toolbar = (RelativeLayout)  findViewById(R.id.view_main_toolbar);
        view_main_toolbar2 = (RelativeLayout)  findViewById(R.id.view_main_toolbar2);
        tv_frag_title = (TextView) findViewById(R.id.tv_frag_title);
        img_toolbar = (ImageView) findViewById(R.id.img_toolbar);
        img_toolbar_search = (ImageView) findViewById(R.id.img_toolbar_search);
        tv_toolbar_go_back = findViewById(R.id.tv_toolbar_go_back);
        ed_toolbar_search = (EditText) findViewById(R.id.ed_toolbar_search);

        manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        view_delete_search_history = findViewById(R.id.view_delete_search_history);
        rec_search_history = findViewById(R.id.rec_search_history);
        rec_search_history.setLayoutManager(manager);
        DividerItemDecoration deco = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        rec_search_history.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        img_toolbar.setOnClickListener(toolbarClickListenr);
        img_toolbar_search.setOnClickListener(toolbarClickListenr);
        tv_toolbar_go_back.setOnClickListener(toolbarClickListenr);
        view_delete_search_history.setOnClickListener(toolbarClickListenr);

        manager2 = new LinearLayoutManager(this);
        manager2.setOrientation(LinearLayoutManager.VERTICAL);
        tv_toolbar_frag_go_back2 = findViewById(R.id.tv_toolbar_frag_go_back2);
        tv_toolbar_frag_go_back2.setOnClickListener(toolbarClickListenr);

        adapter = new SearchhistoryAdapter(this,arrHistory);
        adapter.setHistorySearchListener(this);
        rec_search_history.setAdapter(adapter);
        ed_toolbar_search.setImeOptions(EditorInfo.IME_ACTION_DONE);
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
                        Date today = Calendar.getInstance().getTime();Log.d("DBHELPER","add");
                        SearchItem s = new SearchItem();
                        s.setDate(String.valueOf(today.getYear()+1900)+"."+(today.getMonth()+1)+"."+today.getDate());
                        s.setHistory(textView.getText().toString());
                        dbHelper.insert(textView.getText().toString(),String.valueOf(today.getYear()+1900)+"."+(today.getMonth()+1)+"."+today.getDate());
//                        arrHistory.add(s);
//                        arrHistory = dbHelper.getResult();
                        adapter.notifyDataSetChanged();
                        hideSearch();
                        WebViewInfo.getInstance().setPageName(getResources().getString(R.string.page_name_search));
                        view_main_toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
                        String searchTag = ed_toolbar_search.getText().toString();
                        Log.d("searchTag",searchTag);
                        WebViewInfo.getInstance().setSearch_tag(searchTag);
                        ed_toolbar_search.setText("");
                        newActivity("search");
                        return true; // consume.
                    }
                }
                return false;
            }
        });

        viewUserInfo();

        mPageUrl = getIntent().getStringExtra("pageUrl");
        mPageName = getIntent().getStringExtra("pageName");
        mPageKey = getIntent().getStringExtra("pageKey");
        if(mPageKey == null)
            mPageKey = "";
        mPageValue = getIntent().getStringExtra("pageValue");
        if(mPageValue == null)
            mPageValue = "";

//        if(mPageKey.isEmpty() || mPageKey == null){
//            Bundle bundle = new Bundle();
//            mPageName = "";
//            bundle.putString("pageName", mPageName);
//            bundle.putString("pageUrl", mPageUrl);
//            bundle.putString("pageKey","");
//            bundle.putString("pageValue","");
//            webViewFragment = new WebViewFragment();
//            webViewFragment.setArguments(bundle);
//            changeFragment(webViewFragment);
//            setToolbar();
//            return;
//        }
        if(mPageName == null || mPageName.isEmpty())
            mPageName = "main";
        if(mPageUrl == null || mPageUrl.isEmpty())
            mPageUrl = "Contents/view";

        if("setting".equals(mPageName)) {
            changeFragment(new SettingFragment());
            setToolbar();
        }
        else if("paying".equals(mPageName)){
            Bundle bundle = new Bundle();
            bundle.putString("pageName", mPageName);
            bundle.putString("pageUrl", mPageUrl);
            bundle.putString("item", getIntent().getStringExtra("item"));
            bundle.putString("how", getIntent().getStringExtra("how"));
            WebViewInfo.getInstance().setPayHow(getIntent().getStringExtra("how"));
            bundle.putString("title",getIntent().getStringExtra("title"));
            webViewFragment = new WebViewFragment();
            webViewFragment.setArguments(bundle);
            changeFragment(webViewFragment);
            setToolbar();
        }
        else if("search".equals(mPageName)){
            Bundle bundle = new Bundle();
            bundle.putString("pageName", mPageName);
            bundle.putString("pageUrl", mPageUrl);
            bundle.putString("pageKey", "search_tag");
            bundle.putString("pageValue", WebViewInfo.getInstance().getSearch_tag());
            webViewFragment = new WebViewFragment();
            webViewFragment.setArguments(bundle);
            changeFragment(webViewFragment);
            setToolbar();
        }
        else {
            Bundle bundle = new Bundle();
            bundle.putString("pageName", mPageName);
            bundle.putString("pageUrl", mPageUrl);
            bundle.putString("pageKey", mPageKey);
            bundle.putString("pageValue", mPageValue);
            webViewFragment = new WebViewFragment();
            webViewFragment.setArguments(bundle);
            changeFragment(webViewFragment);
            setToolbar();
        }
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
                if("main".equals(mPageName)) {
                    long tempTime = System.currentTimeMillis();
                    long intervalTime = tempTime - backPressedTime;

                    if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
                        super.onBackPressed();
                    } else {
                        backPressedTime = tempTime;
                        Toast.makeText(getApplicationContext(), R.string.notice_exit_app, Toast.LENGTH_SHORT).show();
                    }
                }else{
                    finish();
                }
//                }
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

    public static String toNumFormat(int num) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(num);
    }

    private void viewUserInfo() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View view  = navigationView.getHeaderView(0);

        TextView tvUserNickname = view.findViewById(R.id.tv_user_nickname);
        TextView tvUserPoint = view.findViewById(R.id.tv_menu_point);
        TextView tvUserFixed = view.findViewById(R.id.tv_menu_charge);

        UserInfo userInfo = UserInfo.getInstance();
        tvUserNickname.setText(userInfo.getNickname());
        Integer point = Integer.parseInt(userInfo.getPoint());
        tvUserPoint.setText(String.format("%,d", point));

        tvUserFixed.setText(checkFixed(userInfo.getFixed()));

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
        LinearLayout view_menu_fix = view.findViewById(R.id.view_menu_fix);
        view_menu_fix.setOnClickListener(drawerClickListenr);

    }

    private String checkFixed(String fixed){
        if("0".equals(fixed)){
            return "0000.00.00";
        }
        fixed = fixed.replaceAll("[^0-9]", "");
        fixed = fixed.substring(0,4)+"."+fixed.substring(4,6)+"."+fixed.substring(6,8);
        return fixed;
    }

    private void SearchResult(){

    }

    private View.OnClickListener toolbarClickListenr = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.img_toolbar:
                    if("main".equals(mPageName))
                        drawer.openDrawer(Gravity.LEFT);
                    else
                        finish();
                    break;
                case R.id.img_toolbar_search:
                    ed_toolbar_search.setVisibility(View.VISIBLE);
                    view_delete_search_history.setVisibility(View.VISIBLE);
                    rec_search_history.setVisibility(View.VISIBLE);
                    tv_toolbar_go_back.setVisibility(View.VISIBLE);
                    img_toolbar_search.setVisibility(View.GONE);
                    img_toolbar.setVisibility(View.GONE);
                    view_main_toolbar.setBackgroundColor(getResources().getColor(R.color.MainColor));
                    break;
                case R.id.tv_toolbar_go_back:
                    hideSearch();
                    break;
                case R.id.view_delete_search_history:
                    dbHelper.deleteAll();
                    arrHistory.clear();
                    adapter.notifyDataSetChanged();
                    break;
                case R.id.tv_toolbar_frag_go_back2:
                    onBackPressed();
                    break;
            }
        }
    };

    private void hideSearch(){
        view_main_toolbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.background_toolbar));
        ed_toolbar_search.setVisibility(View.GONE);
        view_delete_search_history.setVisibility(View.GONE);
        rec_search_history.setVisibility(View.GONE);
        tv_toolbar_go_back.setVisibility(View.GONE);
        img_toolbar_search.setVisibility(View.VISIBLE);
        img_toolbar.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(ed_toolbar_search.getWindowToken(),0);
    }

    private View.OnClickListener drawerClickListenr = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            WebViewInfo info = WebViewInfo.getInstance();
            info.setC_id("");
            info.setC_idx("");
            info.setCategory("");
            info.setEp_idx("");
            info.setPageName("");
            info.setSearch_tag("");
            switch (v.getId()){
                case R.id.view_menu_logout:
                    tryLogout();
                    break;
                case R.id.img_menu_close:
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);
                    break;
                case R.id.view_menu_go_home:
                    goMainWeb();
                    break;
                case R.id.view_menu_cookies:
                    info.setPageName(getResources().getString(R.string.page_name_cookie_list));
                    newActivity(info.getPageName());
                    break;
                case R.id.view_menu_user_info:
                    info.setPageName(getResources().getString(R.string.page_name_info));
                    newActivity(info.getPageName());
                    break;
                case R.id.view_menu_purchase:
                    info.setPageName(getResources().getString(R.string.page_name_purchase));
                    newActivity(info.getPageName());
                    break;
                case R.id.view_menu_favorite:
                    info.setPageName(getResources().getString(R.string.page_name_like));
                    newActivity(info.getPageName());
//                    isMain = false;
                    break;
                case R.id.view_menu_subscribe:
                    info.setPageName(getResources().getString(R.string.page_name_keep));
                    newActivity(info.getPageName());
                    break;
                case R.id.view_menu_point_history:
                    info.setPageName(getResources().getString(R.string.page_name_point_list));
                    newActivity(info.getPageName());
                    break;
                case R.id.view_menu_pay_history:
                    info.setPageName(getResources().getString(R.string.page_name_pay_list));
                    newActivity(info.getPageName());
                    break;
                case R.id.view_menu_notice:
                    info.setPageName(getResources().getString(R.string.page_name_notice));
                    newActivity(info.getPageName());
                    break;
                case R.id.view_menu_faq:
                    info.setPageName(getResources().getString(R.string.page_name_faq));
                    newActivity(info.getPageName());
                    break;
                case R.id.view_menu_qna:
                    info.setPageName(getResources().getString(R.string.page_name_qna_list));
                    newActivity(info.getPageName());
                    break;
                case R.id.view_menu_policy:
                    info.setPageName(getResources().getString(R.string.page_name_policy));
                    newActivity(info.getPageName());
                    break;
                case R.id.view_menu_fix:
                    info.setPageName(getResources().getString(R.string.page_name_index));
                    newActivity(info.getPageName());
                    break;
                case R.id.view_menu_setting:
                    newActivity("setting");
                    return;
            }
        }
    };

    private void changeFragment(Fragment frag){
        if(frag != null){
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.main_media_frame, frag);
            ft.commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    private void newActivity(String pageName){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        if(pageName.equals(mPageName)){
            return;
        }
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.putExtra("pageName",pageName);
        if("main".equals(pageName))
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setToolbar(){
        if("main".equals(mPageName) || "channel".equals(mPageName)){
            view_main_toolbar.setVisibility(View.VISIBLE);
            view_main_toolbar2.setVisibility(View.GONE);
            if("main".equals(mPageName)){
                img_toolbar.setImageResource(R.drawable.ic_open_drawer);
            }else{
                img_toolbar.setImageResource(R.drawable.ic_back);
            }
        }else{
            tv_frag_title.setText("");
            view_main_toolbar.setVisibility(View.GONE);
            view_main_toolbar2.setVisibility(View.VISIBLE);
            if(mPageName.equals(getResources().getString(R.string.page_name_info))){
                tv_frag_title.setText("회원정보");
            }else if(mPageName.equals(getResources().getString(R.string.page_name_purchase))){
                tv_frag_title.setText("시청영상");
            }else if(mPageName.equals(getResources().getString(R.string.page_name_like))){
                tv_frag_title.setText("찜한영상");
            }else if(mPageName.equals(getResources().getString(R.string.page_name_keep))){
                tv_frag_title.setText("구독채널");
            }else if(mPageName.equals(getResources().getString(R.string.page_name_point_list))){
                tv_frag_title.setText("포인트내역");
            }else if(mPageName.equals(getResources().getString(R.string.page_name_pay_list))){
                tv_frag_title.setText("결제내역");
            }else if(mPageName.equals(getResources().getString(R.string.page_name_notice))){
                tv_frag_title.setText("공지사항");
            }else if(mPageName.equals(getResources().getString(R.string.page_name_faq))){
                tv_frag_title.setText("FAQ");
            }else if(mPageName.contains("qna")){
                tv_frag_title.setText("온라인 문의");
            }else if(mPageName.equals(getResources().getString(R.string.page_name_policy))){
                tv_frag_title.setText("운영정책");
            }else if(mPageName.equals("Auth")){
                tv_frag_title.setText("본인인증");
            }else if(mPageName.equals("index")){
                tv_frag_title.setText("이용권 구매");
            }else if(mPageName.equals("setting")){
                tv_frag_title.setText("환경설정");
            }else if(mPageName.equals("search")){
                tv_frag_title.setText(WebViewInfo.getInstance().getSearch_tag());
            }else if(mPageName.equals("paying")){
                if("PHONE".equals(WebViewInfo.getInstance().getPayHow()))
                    tv_frag_title.setText("휴대폰 결제");
                else if("CARD".equals(WebViewInfo.getInstance().getPayHow()))
                    tv_frag_title.setText("신용카드 결제");
                else if("BANK".equals(WebViewInfo.getInstance().getPayHow()))
                    tv_frag_title.setText("계좌이체");
                else if("VBANK".equals(WebViewInfo.getInstance().getPayHow()))
                    tv_frag_title.setText("가상계좌");
                else if("CULTURE".equals(WebViewInfo.getInstance().getPayHow()))
                    tv_frag_title.setText("문화상푼권 결제");
                else if("BOOK".equals(WebViewInfo.getInstance().getPayHow()))
                    tv_frag_title.setText("도서문화상품권 결제");
                else if("HAPPY".equals(WebViewInfo.getInstance().getPayHow()))
                    tv_frag_title.setText("해피머니 상품권 결제");
                else if("GAME".equals(WebViewInfo.getInstance().getPayHow()))
                    tv_frag_title.setText("게임문화상품권 결제");
                else if("PAYPAL".equals(WebViewInfo.getInstance().getPayHow()))
                    tv_frag_title.setText("PAYPAL 결제");
                else
                    tv_frag_title.setText("이용권 구매");
            }
        }
    }

    public void goMainWeb(){
        mPageName = "main";
        newActivity(mPageName);
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

    @Override
    public void onClick(String his) {
        hideSearch();
        if(findViewById(R.id.view_main_search).getVisibility() == View.VISIBLE) {
            WebViewInfo.getInstance().setPageName(getResources().getString(R.string.page_name_search));
            WebViewInfo.getInstance().setSearch_tag(his);
//            view_main_toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
            newActivity("search");
        }
    }
}
