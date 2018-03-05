package com.minimon.diocian.player;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by ICARUSUD on 2018. 3. 5..
 */

public class TouchGestureDetector extends GestureDetector {
    public interface touchListner{
        void onUp();
    }

    private touchListner listner = null;

    public void setListner(touchListner mListener){
        listner = mListener;
    }

    public TouchGestureDetector(Context context, OnGestureListener listener) {
        super(context, listener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        if(ev.getAction() == MotionEvent.ACTION_UP){
            if(listner != null){
                listner.onUp();
            }
        }
        return result;
    }
}
