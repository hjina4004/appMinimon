package com.minimon.diocian.player;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class TextIconmoon extends AppCompatTextView {
    private final static String NAME = "ICOMOON";
    private static LruCache<String, Typeface> sTypefaceCache = new LruCache<String, Typeface>(12);

    public TextIconmoon(Context context) {
        super(context);
        init();
    }

    public TextIconmoon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        Typeface typeface = sTypefaceCache.get(NAME);

        if (typeface == null) {

            typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/icomoon.ttf");
            sTypefaceCache.put(NAME, typeface);

        }
        setTypeface(typeface);
    }
}
