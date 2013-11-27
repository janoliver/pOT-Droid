package com.mde.potdroid3.helpers;

import android.util.Log;

import java.io.*;

/**
 * Created by oli on 5/26/13.
 */
public class Utils {

    public static final String LOG_TAG = "pOT Droid";

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
