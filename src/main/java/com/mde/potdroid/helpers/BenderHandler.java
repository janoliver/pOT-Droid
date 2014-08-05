package com.mde.potdroid.helpers;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.mde.potdroid.models.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Date;

/**
 * This class handles the Bender downloading and storing the User <-> Bender
 * information in the database.
 */
public class BenderHandler {

    protected static final String BENDER_STORAGE_DIR = "/files/avatars";

    // the context
    private Context mContext;

    // access to the app settings
    private SettingsWrapper mSettings;

    // the database wrapper, so we can store the bender information
    private DatabaseWrapper mDatabase;

    public BenderHandler(Context cx) {
        mContext = cx;
        mSettings = new SettingsWrapper(cx);
        mDatabase = new DatabaseWrapper(cx);
    }

    /**
     * Check, whether the external files dir is writable
     *
     * @return writable or not
     */
    private boolean isWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Get the path to the avatar of User user. If the user object has an Avatar field set,
     * this is taken (and, if needed, downloaded). If not, we look for the last seen
     * avatar of this user in the Database. If this is not available either, return null.
     *
     * @param user     User object
     * @param callback Callback
     */
    public void getAvatar(User user, BenderListener callback) {

        File avatar = getAvatarFile(user);

        // figure out, if it already exists in the storage
        if (avatar == null) {

            callback.onFailure();

        } else if (avatar.exists()) {
            callback.onSuccess(getAvatarFilePath(user));

        } else if (!isWritable()) {

            callback.onFailure();

        } else if (mSettings.downloadBenders()) {
            downloadBender(callback, user);

        }

    }

    /**
     * Perform the downloading of a bender an, upon success or failure, call the corresponding
     * method of the callback.
     *
     * @param callback The BenderListener callback implementation
     * @param user     the user object
     */
    public void downloadBender(final BenderListener callback, final User user) {
        Network network = new Network(mContext);

        // try to find out the url, call onFailure upon error.
        String url;
        try {
            url = getAvatarUrl(user);
        } catch (UnsupportedEncodingException e) {
            callback.onFailure();
            return;
        }

        // perform the download.
        String[] allowedContentTypes = new String[]{"image/png", "image/jpeg", "image/gif"};
        network.get(url, null, new BinaryHttpResponseHandler(allowedContentTypes) {
            @Override
            public void onSuccess(int statusCode, org.apache.http.Header[] headers, byte[] fileData) {
                try {
                    getBenderStorageDir().mkdirs();

                    FileOutputStream fos = new FileOutputStream(getAvatarFile(user));
                    fos.write(fileData);
                    fos.close();

                    Date date = new Date();
                    mDatabase.updateBender(user.getAvatarId(), user.getId(),
                            user.getAvatarFile(), new Timestamp(date.getTime()));

                    callback.onSuccess(getAvatarFilePath(user));
                } catch (Exception e) {
                    callback.onFailure();
                }

            }

            @Override
            public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] binaryData, java.lang.Throwable error) {
                callback.onFailure();
            }
        });

    }

    /**
     * This function returns the theoretical path of a User's avatar, regardless of
     * whether it exists or not.
     *
     * @param user user object
     * @return path
     */
    public String getAvatarFilePath(User user) {
        if(getAvatarFile(user) != null)
            return "file://" + getAvatarFile(user).getAbsolutePath();
        else
            return null;
    }

    /**
     * This function returns the theoretical path of a User's avatar, regardless of
     * whether it exists or not.
     *
     * @param user user object
     * @return path
     */
    public String getAvatarFilePathIfExists(User user) {
        File f = getAvatarFile(user);
        if(f == null || !f.exists())
            return null;
        return "file://" + getAvatarFile(user).getAbsolutePath();

    }

    /**
     * This function returns the file object of a User's avatar, regardless of
     * whether it exists or not.
     *
     * @param user User object
     * @return file object
     */
    public File getAvatarFile(User user) {
        // if the information is not known in the user object, try to retrieve it
        // from the database
        if ((user.getAvatarFile() == null || user.getAvatarFile().equals("") ||
                user.getAvatarId() == 0) &&
                !mDatabase.setCurrentBenderInformation(user))
            return null;

        String[] parts = user.getAvatarFile().split("\\.");
        String filename = user.getAvatarId() + "." + parts[parts.length - 1];

        File res = new File(getBenderStorageDir(), filename);

        if(res.exists())
        {
            Date date = new Date();
            mDatabase.updateBender(user.getAvatarId(), user.getId(),
                    user.getAvatarFile(), new Timestamp(date.getTime()));

        }

        return res;
    }

    /**
     * This function returns the relative URL of a user's bender as stored on the mods.de website.
     *
     * @param user The user object
     * @return The URL as string
     * @throws UnsupportedEncodingException
     */
    public String getAvatarUrl(User user) throws UnsupportedEncodingException {
        String[] parts = user.getAvatarFile().split("/");
        parts[parts.length - 1] = URLEncoder.encode(parts[parts.length - 1],
                "UTF-8").replace("+", "%20");
        return TextUtils.join("/", parts);
    }

    /**
     * The interface to implement for notification of success or failure of bender downloads.
     */
    public interface BenderListener {

        public abstract void onSuccess(String path);

        public abstract void onFailure();
    }

    /**
     * Return a File object pointing to the bender storage dir.
     * Compatible with API < 8
     *
     * @return File object
     */
    public File getBenderStorageDir() {
        File ext_root = Environment.getExternalStorageDirectory();
        return new File(ext_root, "Android/data/" + mContext.getPackageName() + BENDER_STORAGE_DIR);
    }
}
