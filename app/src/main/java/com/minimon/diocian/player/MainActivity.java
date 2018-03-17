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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener, DBHelper.dbHelperListenr, SearchhistoryAdapter.SearchHistoryAdapterListener{

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

    private RelativeLayout view_main_toolbar2;
    private TextAwesome tv_toolbar_open_drawer2;
    private ImageView tv_toolbar_frag_go_back2;
    private TextAwesome tv_toolbar_search2;
    private ImageView tv_toolbar_go_back2;
    private EditText ed_toolbar_search2;
//    private TextView tv_frag_title;
//    private DrawerLayout drawer2;
    private RelativeLayout view_delete_search_history2;
    private RecyclerView rec_search_history2;

    private SearchhistoryAdapter adapter;
    private LinearLayoutManager manager;
    private List<SearchItem> arrHistory = new ArrayList<SearchItem>();
    private LinearLayoutManager manager2;
//    private Realm realm;

    // for Google
    private static final int GOOGLE_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;

    private WebView mWebView;
//    private boolean isMain;
    private boolean isShowSearch = false;

    private DBHelper dbHelper;

    private WebViewFragment webViewFragment;
//    private Fragment fragment;

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

        manager2 = new LinearLayoutManager(this);
        manager2.setOrientation(LinearLayoutManager.VERTICAL);
        tv_toolbar_open_drawer2 = (TextAwesome) findViewById(R.id.tv_toolbar_open_drawer2);
        tv_toolbar_search2 = (TextAwesome) findViewById(R.id.tv_toolbar_search2);
        tv_toolbar_go_back2 = findViewById(R.id.tv_toolbar_go_back2);
        tv_toolbar_frag_go_back2 = findViewById(R.id.tv_toolbar_frag_go_back2);
        ed_toolbar_search2 = (EditText) findViewById(R.id.ed_toolbar_search2);
//        tv_frag_title = findViewById(R.id.tv_frag_title);

        view_delete_search_history2 = findViewById(R.id.view_delete_search_history2);
        rec_search_history2 = findViewById(R.id.rec_search_history2);
        rec_search_history2.setLayoutManager(manager2);
        rec_search_history2.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        tv_toolbar_open_drawer2.setOnClickListener(toolbarClickListenr);
        tv_toolbar_search2.setOnClickListener(toolbarClickListenr);
        tv_toolbar_go_back2.setOnClickListener(toolbarClickListenr);
        tv_toolbar_frag_go_back2.setOnClickListener(toolbarClickListenr);
        view_delete_search_history2.setOnClickListener(toolbarClickListenr);

        adapter = new SearchhistoryAdapter(this,arrHistory);
        adapter.setHistorySearchListener(this);
        rec_search_history.setAdapter(adapter);
        rec_search_history2.setAdapter(adapter);

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
                        textView.setText("");
//                        arrHistory = dbHelper.getResult();
                        adapter.notifyDataSetChanged();
                        hideSearch();
                        WebViewInfo.getInstance().setPageName(getResources().getString(R.string.page_name_search));
                        WebViewInfo.getInstance().setSearch_tag(ed_toolbar_search.getText().toString());
                        webViewFragment.moveWebUrl();
                        changeFragment(webViewFragment);
                        return true; // consume.
                    }
                }
                return false;
            }
        });

        ed_toolbar_search2.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
                        textView.setText("");
//                        arrHistory = dbHelper.getResult();
                        adapter.notifyDataSetChanged();
                        hideSearch2();
                        WebViewInfo.getInstance().setPageName(getResources().getString(R.string.page_name_search));
                        WebViewInfo.getInstance().setSearch_tag(ed_toolbar_search2.getText().toString());
                        webViewFragment.moveWebUrl();
                        changeFragment(webViewFragment);
                        return true; // consume.
                    }
                }
                return false;
            }
        });

        viewUserInfo();

        webViewFragment = new WebViewFragment();

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
                isShowSearch = true;
//                changeToolbarVisibility(isMain);
                mListenr.onBack();
            }else {
//                if(isMain) {
                    long tempTime = System.currentTimeMillis();
                    long intervalTime = tempTime - backPressedTime;

                    if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
                        super.onBackPressed();
                    } else {
                        backPressedTime = tempTime;
                        Toast.makeText(getApplicationContext(), R.string.notice_exit_app, Toast.LENGTH_SHORT).show();
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
            return "-";
        }else{
            return fixed;
        }
    }

    private void SearchResult(){

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
                    hideSearch();
                    break;
                case R.id.view_delete_search_history2:
                    dbHelper.deleteAll();
                    arrHistory.clear();
                    adapter.notifyDataSetChanged();
                    break;
                case R.id.tv_toolbar_open_drawer2:
                    drawer.openDrawer(Gravity.LEFT);
                    break;
                case R.id.tv_toolbar_search2:
                    ed_toolbar_search2.setVisibility(View.VISIBLE);
                    view_delete_search_history2.setVisibility(View.VISIBLE);
                    rec_search_history2.setVisibility(View.VISIBLE);
                    tv_toolbar_go_back2.setVisibility(View.VISIBLE);
                    tv_toolbar_search2.setVisibility(View.GONE);
                    tv_toolbar_open_drawer2.setVisibility(View.GONE);
                    view_main_toolbar2.setBackgroundColor(getResources().getColor(R.color.MainColor));
                    break;
                case R.id.tv_toolbar_go_back2:
                    hideSearch2();
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
        ed_toolbar_search.setVisibility(View.GONE);
        view_delete_search_history.setVisibility(View.GONE);
        rec_search_history.setVisibility(View.GONE);
        tv_toolbar_go_back.setVisibility(View.GONE);
        tv_toolbar_search.setVisibility(View.VISIBLE);
        tv_toolbar_open_drawer.setVisibility(View.VISIBLE);
//        if(isMain){
//            view_main_toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
//        }
    }

    private void hideSearch2(){
        ed_toolbar_search2.setVisibility(View.GONE);
        view_delete_search_history2.setVisibility(View.GONE);
        rec_search_history2.setVisibility(View.GONE);
        tv_toolbar_go_back2.setVisibility(View.GONE);
        tv_toolbar_search2.setVisibility(View.VISIBLE);
        tv_toolbar_open_drawer2.setVisibility(View.VISIBLE);
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
//                    if(isMain){
//                        view_main_toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
//                    }
                    break;
                case R.id.view_menu_go_home:
                    goMainWeb();
                    break;
                case R.id.view_menu_cookies:
                    info.setPageName(getResources().getString(R.string.page_name_cookie_list));
                    webViewFragment.moveWebUrl();
//                    tv_frag_title.setText("최근 본 영상");
                    isShowSearch = true;
//                    isMain = false;
                    break;
                case R.id.view_menu_user_info:
                    info.setPageName(getResources().getString(R.string.page_name_info));
                    webViewFragment.moveWebUrl();
//                    tv_frag_title.setVisibility(View.VISIBLE);
//                    tv_frag_title.setText(getResources().getString(R.string.menu_user_page));
                    isShowSearch = false;
//                    isMain = false;
                    break;
                case R.id.view_menu_purchase:
                    info.setPageName(getResources().getString(R.string.page_name_purchase));
                    webViewFragment.moveWebUrl();
//                    tv_frag_title.setVisibility(View.VISIBLE);
//                    tv_frag_title.setText(getResources().getString(R.string.menu_watch));
                    isShowSearch = true;
//                    isMain = false;
                    break;
                case R.id.view_menu_favorite:
                    info.setPageName(getResources().getString(R.string.page_name_like));
                    webViewFragment.moveWebUrl();
//                    tv_frag_title.setVisibility(View.VISIBLE);
//                    tv_frag_title.setText(getResources().getString(R.string.menu_favorite));
                    isShowSearch = true;
//                    isMain = false;
                    break;
                case R.id.view_menu_subscribe:
                    info.setPageName(getResources().getString(R.string.page_name_keep));
                    webViewFragment.moveWebUrl();
//                    tv_frag_title.setVisibility(View.VISIBLE);
//                    tv_frag_title.setText(getResources().getString(R.string.menu_subscribe));
                    isShowSearch = true;
//                    isMain = false;
                    break;
                case R.id.view_menu_point_history:
                    info.setPageName(getResources().getString(R.string.page_name_point_list));
                    webViewFragment.moveWebUrl();
//                    tv_frag_title.setVisibility(View.VISIBLE);
//                    tv_frag_title.setText(getResources().getString(R.string.menu_point_history));
                    isShowSearch = false;
//                    isMain = false;
                    break;
                case R.id.view_menu_pay_history:
                    info.setPageName(getResources().getString(R.string.page_name_pay_list));
                    webViewFragment.moveWebUrl();
//                    tv_frag_title.setVisibility(View.VISIBLE);
//                    tv_frag_title.setText(getResources().getString(R.string.menu_pay_history));
                    isShowSearch = false;
//                    isMain = false;
                    break;
                case R.id.view_menu_notice:
                    info.setPageName(getResources().getString(R.string.page_name_notice));
                    webViewFragment.moveWebUrl();
//                    tv_frag_title.setVisibility(View.VISIBLE);
//                    tv_frag_title.setText("공지사항");
//                    isMain = false;
                    break;
                case R.id.view_menu_faq:
                    info.setPageName(getResources().getString(R.string.page_name_faq));
                    webViewFragment.moveWebUrl();
//                    tv_frag_title.setVisibility(View.VISIBLE);
//                    tv_frag_title.setText("FAQ");
                    isShowSearch = false;
//                    isMain = false;
                    break;
                case R.id.view_menu_qna:
                    info.setPageName(getResources().getString(R.string.page_name_qna_list));
                    webViewFragment.moveWebUrl();
//                    tv_frag_title.setVisibility(View.VISIBLE);
//                    tv_frag_title.setText("1:1 QNA");
                    isShowSearch = false;
//                    isMain = false;
                    break;
                case R.id.view_menu_policy:
                    info.setPageName(getResources().getString(R.string.page_name_policy));
                    webViewFragment.moveWebUrl();
//                    tv_frag_title.setVisibility(View.VISIBLE);
//                    tv_frag_title.setText("이용약관");
                    isShowSearch = false;
//                    isMain = false;
                    break;
                case R.id.view_menu_fix:
                    info.setPageName(getResources().getString(R.string.page_name_index));
                    webViewFragment.moveWebUrl();
//                    tv_frag_title.setVisibility(View.VISIBLE);
//                    tv_frag_title.setText("이용권");
                    isShowSearch = false;
//                    isMain = false;
                    break;
                case R.id.view_menu_setting:
                    tv_toolbar_search2.setVisibility(View.GONE);
//                    webViewFragment.moveWebUrl();
//                    tv_frag_title.setVisibility(View.VISIBLE);
//                    tv_frag_title.setText(getResources().getString(R.string.menu_setting));
                    isShowSearch = false;
//                    isMain = false;
                    changeFragment(new SettingFragment());
                    return;
            }
            changeFragment(webViewFragment);
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
//        changeToolbarVisibility(isMain);
    }

//    private void changeToolbarVisibility(boolean ismain){
//        if(ismain){
//            view_main_toolbar.setVisibility(View.VISIBLE);
//            view_main_toolbar2.setVisibility(View.GONE);
//        }else{
//            view_main_toolbar.setVisibility(View.GONE);
//            view_main_toolbar2.setVisibility(View.VISIBLE);
//            if(isShowSearch){
//                tv_toolbar_open_drawer2.setVisibility(View.VISIBLE);
////                tv_toolbar_search2.setVisibility(View.VISIBLE);
//                tv_toolbar_frag_go_back2.setVisibility(View.GONE);
//            }else{
//                tv_toolbar_open_drawer2.setVisibility(View.GONE);
////                tv_toolbar_search2.setVisibility(View.GONE);
//                tv_toolbar_frag_go_back2.setVisibility(View.VISIBLE);
//            }
//        }
//    }
    public void goMainWeb(){
//        isMain = true;
        WebViewInfo.getInstance().setPageName(getResources().getString(R.string.page_name_main));
        webViewFragment.moveWebUrl();
//        changeToolbarVisibility(true);
        changeFragment(webViewFragment);
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
    public void onClick() {
        hideSearch();
        hideSearch2();
        if(findViewById(R.id.view_main_search).getVisibility() == View.VISIBLE) {
            WebViewInfo.getInstance().setPageName(getResources().getString(R.string.page_name_search));
            WebViewInfo.getInstance().setSearch_tag(ed_toolbar_search.getText().toString());
            webViewFragment.moveWebUrl();
            changeFragment(webViewFragment);
        }else if (findViewById(R.id.view_main_search2).getVisibility() == View.VISIBLE){
            WebViewInfo.getInstance().setPageName(getResources().getString(R.string.page_name_search));
            WebViewInfo.getInstance().setSearch_tag(ed_toolbar_search2.getText().toString());
            webViewFragment.moveWebUrl();
            changeFragment(webViewFragment);
        }
    }
}
