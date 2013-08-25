package com.mde.potdroid3.helpers;

import android.util.Log;

import java.io.*;

/**
 * Created by oli on 5/26/13.
 */
public class Utils {

    public static final String LOGIN_URL = "http://login.mods.de/";
    public static final String BASE_URL = "http://forum.mods.de/bb/";
    public static final String ASYNC_URL = "http://forum.mods.de/bb/async/";
    public static final String FORUM_URL = "http://forum.mods.de/bb/xml/boards.php";
    public static final String BOARD_URL_BASE = "http://forum.mods.de/bb/xml/board.php?BID=";
    public static final String THREAD_URL_BASE = "http://forum.mods.de/bb/xml/thread.php?update_bookmark=1&TID=";
    public static final String BOARD_URL_POST = "http://forum.mods.de/bb/newreply.php";
    public static final String BOARD_URL_EDITPOST = "http://forum.mods.de/bb/editreply.php";
    public static final String THREAD_URL = "http://forum.mods.de/bb/thread.php?TID=";
    public static final String BOOKMARK_URL = "http://forum.mods.de/bb/xml/bookmarks.php";
    public static final String DEFAULT_ENCODING = "ISO-8859-15";
    public static final String FORUM_HOST = "forum.mods.de";
    public static final String COOKIE_LIFETIME = "31536000";

    public static final String LOG_TAG = "pOT Droid";
    public static final int WAIT_CONNECTION_TIME = 500;

    public static final String THEME_LIGHT = "theme_light";
    public static final String THEME_DARK  = "theme_dark";

    public static String inputStreamToString(InputStream in) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;

        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line + "\n");
        }

        bufferedReader.close();
        return stringBuilder.toString();
    }

    public static void log(String msg) {
        Log.v(Utils.LOG_TAG, msg);
    }

    public static String readFileAsString(String filePath) throws java.io.IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line, results = "";
        while( ( line = reader.readLine() ) != null)
        {
            results += line;
        }
        reader.close();
        return results;
    }

    /**
     * Copy all data from InputStream and write using OutputStream
     * @param is InputStream
     * @param os OutputStream
     */
    public static void CopyStream(InputStream is, OutputStream os)
    {
        final int buffer_size=1024;
        try
        {
            byte[] bytes=new byte[buffer_size];
            for(;;)
            {
                int count=is.read(bytes, 0, buffer_size);
                if(count==-1)
                    break;
                os.write(bytes, 0, count);
            }
        }
        catch(Exception ex){}
    }
}
