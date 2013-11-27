package com.mde.potdroid3.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import com.mde.potdroid3.models.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by oli on 8/11/13.
 */
public class BenderHandler {
    private Context mContext;
    private File mRoot;
    private SettingsWrapper mSettings;

    public BenderHandler(Context cx) {
        mContext = cx;
        mSettings = new SettingsWrapper(cx);
        File ext_root = Environment.getExternalStorageDirectory();
        Utils.log("Android/data/" + mContext.getPackageName() + "/avatars/");

        mRoot = new File(ext_root, "Android/data/" + mContext.getPackageName() + "/avatars/");
        Utils.log(mRoot.getPath());
    }

    private boolean isWritable() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state))
            return true;

        return false;
    }

    public String getAvatar(User u, TopicJSInterface.JSInterfaceListener listener) {

        File avatar = u.getAvatarLocalFile(mContext);
        String avatar_url = u.getAvatarLocalFileUrl(mContext);

        // figure out, if it already exists in the storage
        if(avatar.exists()) {
            listener.updateBender(u.getId());
            return avatar_url;
        }

        // if not, start a new thread to download it and return a loading image
        else if(!isWritable())
            return null;

        // download it
        // Open InputStream to download the image.
        if(mSettings.downloadBenders())
            new DownloadAvatarTask(listener).execute(u);

        return null;

    }

    private class DownloadAvatarTask extends AsyncTask<User, Integer, User> {
        private TopicJSInterface.JSInterfaceListener mListener;

        public DownloadAvatarTask(TopicJSInterface.JSInterfaceListener listener) {
            mListener = listener;
        }

        protected User doInBackground(User... user) {
            User u = user[0];

            File avatar_file = u.getAvatarLocalFile(mContext);

            if(avatar_file.exists())
                return u;

            try {
                mRoot.mkdirs();
                String[] parts = u.getAvatarFile().split("/");
                parts[parts.length-1] = URLEncoder.encode(parts[parts.length-1], "UTF-8").replace("+", "%20");
                String url = Network.BASE_URL + TextUtils.join("/",parts);
                InputStream is = new URL(url).openStream();
                OutputStream os = new FileOutputStream(avatar_file);
                Utils.CopyStream(is, os);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return u;
        }

        protected void onPostExecute(User u) {
            if(u != null) {
                mListener.updateBender(u.getId());
            }
        }
    }

    private String createFilename(User u) {
        // get extension
        String[] parts = u.getAvatarFile().split("\\.");
        return u.getAvatarId() + "." + parts[parts.length-1];
    }
}
