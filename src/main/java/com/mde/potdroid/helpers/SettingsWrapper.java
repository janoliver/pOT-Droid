package com.mde.potdroid.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import com.mde.potdroid.R;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * This class provides access to the preferences of the App. All settings should be
 * retrieved through this class.
 */
public class SettingsWrapper {

    // The keys to the settings

    public static final String PREF_KEY_THEME = "pref_theme";
    public static final String PREF_KEY_LOGIN = "pref_login";
    public static final String PREF_KEY_LOGOUT = "pref_logout";
    public static final String PREF_KEY_USERNAME = "user_name";
    public static final String PREF_KEY_USERID = "user_id";
    public static final String PREF_KEY_UAGENT = "unique_uagent";
    public static final String PREF_KEY_COOKIE_NAME = "cookie_name";
    public static final String PREF_KEY_COOKIE_VALUE = "cookie_value";
    public static final String PREF_KEY_COOKIE_PATH = "cookie_path";
    public static final String PREF_KEY_COOKIE_URL = "cookie_url";
    public static final String PREF_KEY_SHOW_BENDERS = "pref_bender_position";
    public static final String PREF_KEY_DEBUG = "pref_debug_mode";
    public static final String PREF_KEY_LOAD_BENDERS = "pref_load_benders";
    public static final String PREF_KEY_LOAD_IMAGES = "pref_load_images";
    public static final String PREF_KEY_LOAD_GIFS = "pref_load_gifs";
    public static final String PREF_KEY_LOAD_VIDEOS = "pref_load_videos";
    public static final String PREF_KEY_PARSE_SMILEYS = "pref_parse_smileys";
    public static final String PREF_KEY_POLL_MESSAGES = "pref_message_polling_interval";
    public static final String PREF_KEY_NOTIFICATION_VIBRATE = "pref_notification_vibrate";
    public static final String PREF_KEY_NOTIFICATION_SOUND = "pref_notification_sound";
    public static final String PREF_KEY_POSTINFO = "pref_show_postinfo";
    public static final String PREF_KEY_EDITED = "pref_show_edited";
    public static final String PREF_KEY_DARKEN = "pref_darken_old_posts";
    public static final String PREF_KEY_HIDE_GLOBAL = "pref_hide_global";
    public static final String PREF_KEY_START_ACTIVITY = "pref_start_activity";
    public static final String PREF_KEY_START_FORUM = "pref_start_forum";
    public static final String PREF_KEY_MATA = "pref_mata";
    public static final String PREF_KEY_MATA_FORUM = "pref_mata_forum";
    public static final String PREF_KEY_SHOW_MENU = "pref_show_menu";
    public static final String PREF_KEY_MARK_NEW_POSTS = "pref_mark_new_posts";
    public static final String PREF_KEY_BBCODE_EDITOR = "pref_bbcode_editor";
    public static final String PREF_KEY_CACHE_SIZE = "pref_cache_size";
    public static final String PREF_KEY_BENDER_CACHE_SIZE = "pref_bender_cache_size";
    public static final String PREF_KEY_CONNECTION_TIMEOUT = "pref_connection_timeout";
    public static final String PREF_KEY_DYNAMIC_TOOLBARS = "pref_dynamic_toolbars";
    public static final String PREF_KEY_FASTSCROLL = "pref_fastscroll";
    public static final String PREF_KEY_SHOW_PAGNIATE_TOOLBAR = "pref_show_paginate_toolbar";
    public static final String PREF_KEY_SWIPE_TO_REFRESH = "pref_swipe_to_refresh";
    public static final String PREF_KEY_SWIPE_TO_REFRESH_TOPIC = "pref_swipe_to_refresh_topic";
    public static final String PREF_KEY_SWIPE_TO_PAGINATE = "pref_swipe_to_paginate";
    public static final String PREF_KEY_FIXED_SIDEBAR = "pref_fixed_sidebar";
    public static final String PREF_KEY_READ_SIDEBAR = "pref_sidebar_showread";
    public static final String PREF_KEY_FONT_SIZE = "pref_font_size";
    public static final String PREF_KEY_BOARDS_BOOKMARKS = "pref_board_bookmarks";
    public static final String PREF_KEY_SHOW_END_INDICATOR = "pref_show_end_indicator";
    public static final String PREF_KEY_RELOAD_BOOKMARKS = "pref_reload_bookmarks";
    public static final String PREF_KEY_SWAPPED_SIDEBARS = "pref_swap_sidebars";
    public static final String PREF_KEY_PARSE_BBCODE = "pref_parse_bbcode";
    public static final String PREF_KEY_FAB = "pref_fab";
    public static final String PREF_KEY_TINTED_STATUSBAR = "pref_tinted_statusbar";
    public static final String PREF_KEY_POSTNUMBERS = "pref_show_postnumbers";
    public static final String PREF_KEY_GERMAN_TIMEZONE = "pref_german_timezone";
    public static final String PREF_EXPORT_SETTINGS = "pref_export_settings";
    public static final String PREF_IMPORT_SETTINGS = "pref_import_settings";

    public static final int START_BOARDS = 0;
    public static final int START_BOOKMARKS = 1;
    public static final int START_FORUM = 2;
    public static final int START_SIDEBAR = 3;

    // some references
    private SharedPreferences mSharedPreferences;
    private Context mContext;

    public SettingsWrapper(Context cx) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(cx);
        mContext = cx;

        // if this is a pre-3 version, delete all the preferences
        if (!mSharedPreferences.getBoolean("is_v3", false)) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.clear();
            editor.putBoolean("is_v3", true);
            editor.commit();

            // and delete the old benders
            File ext_root = Environment.getExternalStorageDirectory();
            File dir = new File(mContext.getExternalFilesDir(null), "avatare");

            if (dir.exists())
                dir.delete();
        }
    }

    public Boolean showBenders() {
        return !mSharedPreferences.getString(PREF_KEY_SHOW_BENDERS, "0").equals("0");
    }

    public Integer benderPosition() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_SHOW_BENDERS, "0"));
    }

    public Integer showMenu() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_SHOW_MENU, "3"));
    }

    public Integer pollMessagesInterval() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_POLL_MESSAGES, "0"));
    }

    public String loadBenders() {
        return mSharedPreferences.getString(PREF_KEY_LOAD_BENDERS, "0");
    }

    public String loadImages() {
        return mSharedPreferences.getString(PREF_KEY_LOAD_IMAGES, "0");
    }

    public String loadVideos() {
        return mSharedPreferences.getString(PREF_KEY_LOAD_VIDEOS, "0");
    }

    public Boolean downloadBenders() {
        String lb = mSharedPreferences.getString(PREF_KEY_LOAD_BENDERS, "0");
        return !(lb.equals("0") || (lb.equals("1") &&
                Utils.getConnectionType(mContext) != Utils.NETWORK_WIFI));
    }

    public int getTheme() {
        String theme = mSharedPreferences.getString(PREF_KEY_THEME, "PotDroidDark");
        if (theme.equals("PotDroidDark"))
            return R.style.PotDroidDark;
        if (theme.equals("PotDroidLight"))
            return R.style.PotDroidLight;
        if (theme.equals("PotDroidDarkCompact"))
            return R.style.PotDroidDarkCompact;
        if (theme.equals("PotDroidLightCompact"))
            return R.style.PotDroidLightCompact;
        if (theme.equals("PotDroidWahooka"))
            return R.style.PotDroidWahooka;
        if (theme.equals("PotDroidWahookaCompact"))
            return R.style.PotDroidWahookaCompact;
        return -1;
    }


    public Boolean downloadImages() {
        String lb = mSharedPreferences.getString(PREF_KEY_LOAD_IMAGES, "0");
        return !(lb.equals("0") || (lb.equals("1") &&
                Utils.getConnectionType(mContext) != Utils.NETWORK_WIFI));
    }

    public Boolean downloadGifs() {
        String lb = mSharedPreferences.getString(PREF_KEY_LOAD_GIFS, "0");
        return !(lb.equals("0") || (lb.equals("1") &&
                Utils.getConnectionType(mContext) != Utils.NETWORK_WIFI));
    }

    public Boolean downloadVideos() {
        String lb = mSharedPreferences.getString(PREF_KEY_LOAD_VIDEOS, "0");
        return !(lb.equals("0") || (lb.equals("1") &&
                Utils.getConnectionType(mContext) != Utils.NETWORK_WIFI));
    }

    public Boolean showPostInfo() {
        return mSharedPreferences.getBoolean(PREF_KEY_POSTINFO, true);
    }

    public Boolean showPostNumbers() {
        return mSharedPreferences.getBoolean(PREF_KEY_POSTNUMBERS, false);
    }

    public Boolean isShowEdited() {
        return mSharedPreferences.getBoolean(PREF_KEY_EDITED, true);
    }

    public int getCacheSize() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_CACHE_SIZE, "50")) * 1024 * 1024;
    }

    public int getBenderCacheSize() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_BENDER_CACHE_SIZE, "50")) * 1024 * 1024;
    }

    public int getDefaultFontSize() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_FONT_SIZE, "16"));
    }

    public int getConnectionTimeout() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_CONNECTION_TIMEOUT, "60"));
    }

    public Boolean isBBCodeEditor() {
        return mSharedPreferences.getBoolean(PREF_KEY_BBCODE_EDITOR, false);
    }

    public Boolean hideGlobalTopics() {
        return mSharedPreferences.getBoolean(PREF_KEY_HIDE_GLOBAL, false);
    }

    public Boolean darkenOldPosts() {
        return mSharedPreferences.getBoolean(PREF_KEY_DARKEN, false);
    }

    public Boolean markNewPosts() {
        return mSharedPreferences.getBoolean(PREF_KEY_MARK_NEW_POSTS, false);
    }

    public Boolean dynamicToolbars() {
        return mSharedPreferences.getBoolean(PREF_KEY_DYNAMIC_TOOLBARS, true);
    }

    public Boolean isSwipeToRefresh() {
        return mSharedPreferences.getBoolean(PREF_KEY_SWIPE_TO_REFRESH, true);
    }

    public Boolean isReloadBookmarksOnSidebarOpen() {
        return mSharedPreferences.getBoolean(PREF_KEY_RELOAD_BOOKMARKS, false);
    }

    public Boolean isTintedStatusbar() {
        return mSharedPreferences.getBoolean(PREF_KEY_TINTED_STATUSBAR, true);
    }

    public Boolean isBoardBookmarks() {
        return mSharedPreferences.getBoolean(PREF_KEY_BOARDS_BOOKMARKS, true);
    }

    public Boolean isParseSmileys() {
        return mSharedPreferences.getBoolean(PREF_KEY_PARSE_SMILEYS, true);
    }

    public Boolean isSwipeToRefreshTopic() {
        return mSharedPreferences.getBoolean(PREF_KEY_SWIPE_TO_REFRESH_TOPIC, true);
    }

    public Boolean isSwappedSidebars() {
        return mSharedPreferences.getBoolean(PREF_KEY_SWAPPED_SIDEBARS, false);
    }

    public Boolean isSwipeToPaginate() {
        return mSharedPreferences.getBoolean(PREF_KEY_SWIPE_TO_PAGINATE, true);
    }

    public Boolean isBottomToolbar() {
        return mSharedPreferences.getBoolean(PREF_KEY_SHOW_PAGNIATE_TOOLBAR, false);
    }

    public Boolean isShowEndIndicator() {
        return mSharedPreferences.getBoolean(PREF_KEY_SHOW_END_INDICATOR, true);
    }

    public Boolean isParseBBCode() {
        return mSharedPreferences.getBoolean(PREF_KEY_PARSE_BBCODE, true);
    }

    public Boolean isShowFAB() {
        return mSharedPreferences.getBoolean(PREF_KEY_FAB, true);
    }

    public Boolean fastscroll() {
        return mSharedPreferences.getBoolean(PREF_KEY_FASTSCROLL, true);
    }

    public Boolean isFixedSidebar() {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        float w = displayMetrics.widthPixels / displayMetrics.density;
        return mSharedPreferences.getBoolean(PREF_KEY_FIXED_SIDEBAR, w > 768);
    }

    public Boolean isReadSidebar() {
        return mSharedPreferences.getBoolean(PREF_KEY_READ_SIDEBAR, false);
    }

    public void setUsername(String username) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREF_KEY_USERNAME, username);
        editor.commit();
    }

    public int getStartActivity() {
        return Integer.parseInt(
                mSharedPreferences.getString(PREF_KEY_START_ACTIVITY, Integer.toString(START_BOARDS)));
    }

    public int getStartForum() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_START_FORUM, "14"));
    }

    public int getMataAction() {
        return Integer.parseInt(
                mSharedPreferences.getString(PREF_KEY_MATA, Integer.toString(START_SIDEBAR)));
    }

    public int getMataForum() {
        return Integer.parseInt(mSharedPreferences.getString(PREF_KEY_MATA_FORUM, "14"));
    }

    public Boolean hasUsername() {
        return mSharedPreferences.contains(PREF_KEY_USERNAME);
    }

    public Boolean isDebug() {
        return mSharedPreferences.getBoolean(PREF_KEY_DEBUG, false);
    }

    public Boolean isUseGermanTimezone() {
        return mSharedPreferences.getBoolean(PREF_KEY_GERMAN_TIMEZONE, true);
    }

    public Boolean isNotificationVibrate() {
        return mSharedPreferences.getBoolean(PREF_KEY_NOTIFICATION_VIBRATE, false);
    }

    public String getNotificationSoundURI() {
        return mSharedPreferences.getString(PREF_KEY_NOTIFICATION_SOUND,
                Settings.System.DEFAULT_NOTIFICATION_URI.toString());
    }

    public String getUsername() {
        return mSharedPreferences.getString(PREF_KEY_USERNAME, "");
    }

    public void clearUsername() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(PREF_KEY_USERNAME);
        editor.commit();
    }

    public void setUserId(int id) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(PREF_KEY_USERID, id);
        editor.commit();
    }

    public void clearUserId() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(PREF_KEY_USERID);
        editor.commit();
    }

    public int getUserId() {
        return mSharedPreferences.getInt(PREF_KEY_USERID, 0);
    }

    public void clearCookie() {
        PersistentCookieStore s = new PersistentCookieStore(mContext);
        s.removeAll();
    }

    public String getUserAgent() {
        return String.format(Network.UAGENT_TPL, mSharedPreferences.getString(PREF_KEY_UAGENT, ""));
    }

    public void generateUniqueUserAgent() {
        SecureRandom random = new SecureRandom();
        String uAgent = new BigInteger(50, random).toString(32);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(PREF_KEY_UAGENT, uAgent);
        editor.commit();
    }

    public boolean isVersionUpdate(Context cx) {
        try {
            int versionCode = cx.getPackageManager().getPackageInfo(cx.getPackageName(), 0).versionCode;
            return mSharedPreferences.getInt("installed_version", 0) < versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return true;
        }
    }

    public void registerVersion(Context cx) {
        try {
            int versionCode = cx.getPackageManager().getPackageInfo(cx.getPackageName(), 0).versionCode;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt("installed_version", versionCode);
            editor.commit();
        } catch (PackageManager.NameNotFoundException e) {
            // shouldn't happen
        }
    }
}
