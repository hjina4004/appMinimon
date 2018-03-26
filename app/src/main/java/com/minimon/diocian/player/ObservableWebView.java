package com.minimon.diocian.player;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

/**
 * Created by GOOD on 2018-03-15.
 */

public class ObservableWebView extends WebView {

    private OnScrollChangedCallback mOnScrollChangedCallback;

    private Context context;
    private GestureDetector gestDetector;
    private gestureListener mListener;

    public interface gestureListener{
        void onSwipeUp();
        void onSwipeDown();
    }

    public void setListener(gestureListener listener){
        mListener = listener;
    }

    public ObservableWebView(Context context) {
        super(context);
        init(context);
    }

    public ObservableWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ObservableWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        gestDetector = new GestureDetector(context, gestListener);
    }

    GestureDetector.SimpleOnGestureListener gestListener= new GestureDetector.SimpleOnGestureListener() {
        public boolean onDown(MotionEvent event) {
            return true;
        }

        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            float SWIPE_MIN_DISTANCE = 120;
            float SWIPE_THRESHOLD_VELOCITY = 200;

            if (event1.getRawY() - event2.getRawY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                if(mListener!=null)
                    mListener.onSwipeUp();
            } else if (event2.getRawY() - event1.getRawY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                if(mListener!=null)
                    mListener.onSwipeDown();
            }
            //you can trace any touch events here
            return true;
        }
    };

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        Log.i("WebView", "[onScrollChanged]");
        super.onScrollChanged(l, t, oldl, oldt);
        if(mOnScrollChangedCallback != null) mOnScrollChangedCallback.onScroll(l, t, oldl, oldt);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        //Log.i("WebView", "[onTouchEvent]");
        super.onTouchEvent(event);

        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            Log.d("WebView", "OnTouch : ACTION_DOWN");
        } else if(action == MotionEvent.ACTION_UP){
            Log.d("WebView", "OnTouch : ACTION_UP");
        } else if(action == MotionEvent.ACTION_MOVE){
            Log.d("WebView", "OnTouch : ACTION_MOVE");
        }

        return gestDetector.onTouchEvent(event);
    }

    public OnScrollChangedCallback getOnScrollChangedCallback()
    {
        return mOnScrollChangedCallback;
    }

    public void setOnScrollChangedCallback(final OnScrollChangedCallback onScrollChangedCallback)
    {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    /**
     * Impliment in the activity/fragment/view that you want to listen to the webview
     */
    public interface OnScrollChangedCallback
    {
        void onScroll(int l, int t, int oldl, int oldt);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }
}
