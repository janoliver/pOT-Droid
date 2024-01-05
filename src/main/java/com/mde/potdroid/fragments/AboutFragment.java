package com.mde.potdroid.fragments;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Keep;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.mde.potdroid.R;
import com.mde.potdroid.helpers.CssStyleWrapper;
import com.mde.potdroid.helpers.Network;
import com.samskivert.mustache.Mustache;

import java.io.*;

/**
 * Fragment that shows some information of the app
 */
public class AboutFragment extends BaseFragment {

    /**
     * Create a new instance of AboutFragment and set the arguments
     *
     * @return AboutFragment instance
     */
    public static AboutFragment newInstance() {
        return new AboutFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View v = inflater.inflate(R.layout.layout_about, container, false);
        WebView webView = (WebView) v.findViewById(R.id.about);
        webView.getSettings().setDefaultFontSize(mSettings.getDefaultFontSize());
        try {
            webView.loadDataWithBaseURL("file:///android_asset/", getAboutHtml(),
                    "text/html", Network.ENCODING_UTF8, null);
        } catch (IOException e) {
            // passiert nicht! :mad:
        }

        return v;
    }

    public String getAboutHtml() throws IOException {
        InputStream is = getActivity().getResources().getAssets().open("about.html");
        Reader reader = new InputStreamReader(is);
        StringWriter sw = new StringWriter();
        Mustache.compiler().compile(reader).execute(new AboutContext(getActivity()), sw);
        return sw.toString();
    }

    @Keep
    public static class AboutContext {
        protected Activity mActivity;
        private CssStyleWrapper mStyle;

        public AboutContext(Activity act) {
            mActivity = act;
            mStyle = new CssStyleWrapper(act);
        }

        public CssStyleWrapper getStyle() {
            return mStyle;
        }

        public String getVersionString() {
            String version;
            try {
                version = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionName;
            } catch (PackageManager.NameNotFoundException e) {
                version = "";
            }
            return version;
        }

        public Integer getVersionCode() {
            Integer version;
            try {
                version = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0).versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                version = 0;
            }
            return version;
        }

        public String getAndroidVersion() {
            return Build.VERSION.RELEASE;
        }

        public Integer getDensity() {
            DisplayMetrics metrics = mActivity.getResources().getDisplayMetrics();
            return (int) (metrics.density * 160f);
        }

    }


}
