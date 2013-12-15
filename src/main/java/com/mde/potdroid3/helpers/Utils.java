package com.mde.potdroid3.helpers;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by oli on 5/26/13.
 */
public class Utils {

    public static final String LOG_TAG = "pOT Droid";
    public static WebView mWebView;

    public static void log(String msg) {
        Log.v(Utils.LOG_TAG, msg);
    }

    public static Drawable getDrawableFromAsset(Context cx, String strName) throws IOException {
        AssetManager assetManager = cx.getAssets();
        InputStream istr = assetManager.open(strName);
        return Drawable.createFromStream(istr, null);
    }

    public static WebView getWebViewInstance(Context cx) {
        if(mWebView == null) {
            mWebView = new WebView(cx.getApplicationContext());
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setDomStorageEnabled(true);
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            mWebView.getSettings().setAllowFileAccess(true);
            //mWebView.setWebChromeClient(new WebChromeClient());
            //mWebView.setWebViewClient(new WebViewClient());
            mWebView.loadData("", "text/html", "utf-8");
            mWebView.setBackgroundColor(0x00000000);
        }

        return mWebView;
    }
}
