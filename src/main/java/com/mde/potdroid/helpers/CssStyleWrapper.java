package com.mde.potdroid.helpers;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.Keep;
import com.mde.potdroid.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;

@Keep
public class CssStyleWrapper {

    private Context mContext;

    public CssStyleWrapper(Context cx) {
        mContext = cx;
    }

    public String getWindowBackgroundColor() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbTopicBackgroundColor));
    }

    public String getPostBackgroundColor() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbPostBackgroundColor));
    }

    public String getHeaderBackgroundColor() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbPostHeaderBackgroundColor));
    }

    public String getTextColorPrimary() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbTextColorPrimary));
    }

    public String getTextColorSecondary() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbTextColorSecondary));
    }

    public String getTopicTextColor() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbTopicTextColor));
    }

    public String getTextColorTertiary() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbTextColorTertiary));
    }

    public String getErrorColor() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbErrorColor));
    }

    public String getQuoteBackgroundColor() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbQuoteBackgroundColor));
    }

    public String getQuoteHeaderColor() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbQuoteHeaderColor));
    }

    public String getMediaBackgroundColor() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbMediaBackgroundColor));
    }

    public String getMediaForegroundColor() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbMediaForegroundColor));
    }

    public String getNewPostGlowColor() {
        return cf(Utils.getColorByAttr(mContext, R.attr.bbNewPostGlowColor));
    }

    public String cf(int c) {
        return String.format(Locale.US, "rgba(%d, %d, %d, %.2f)", Color.red(c), Color.green(c), Color.blue(c), Color.alpha(c) / 255.);
    }

    public String getCustomCss() {
        String customCss = "";
        File customDir = new File(mContext.getExternalFilesDir(null), "custom_style");
        if (!customDir.exists() || !customDir.isDirectory())
            return "";

        File[] files = customDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".css");
            }
        });
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });

        for (File inFile : files) {
            try {
                customCss += "\n\n" + Utils.getStringFromFile(inFile.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return customCss;
    }


    public String getCustomJs() {
        String customJs = "";
        File customDir = new File(mContext.getExternalFilesDir(null), "custom_style");

        if (!customDir.exists() || !customDir.isDirectory())
            return "";

        File[] files = customDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(".js");
            }
        });
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });

        for (File inFile : files) {
            try {
                customJs += "\n\n" + Utils.getStringFromFile(inFile.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return customJs;
    }
}
