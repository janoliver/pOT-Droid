package com.mde.potdroid.helpers;

import android.content.Context;
import android.text.TextUtils;
import com.mde.potdroid.models.User;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * This class handles the Bender downloading and storing the User <-> Bender
 * information in the database.
 */
public class BenderHandler {

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
     * Get the path to the avatar of User user. If the user object has an Avatar field set,
     * this is taken (and, if needed, downloaded). If not, we look for the last seen
     * avatar of this user in the Database. If this is not available either, return null.
     *
     * @param user     User object
     * @param callback Callback
     */
    public void getAvatar(final User user, final BenderListener callback) {
        String url;
        try {
            url = getAvatarUrl(user);
        } catch (UnsupportedEncodingException e) {
            callback.onFailure();
            return;
        }

        if (!mSettings.downloadBenders()) {
            String path = getAvatarFilePathIfExists(user);
            if (path != null)
                callback.onSuccess(path);
            else
                callback.onFailure();
            return;
        }

        ImageHandler ih = ImageHandler.getBenderHandler(mContext.getApplicationContext());
        try {
            ih.retrieveImage(url, new ImageHandler.ImageHandlerCallback() {
                @Override
                public void onSuccess(String url, String path, boolean from_cache) {
                    if (!from_cache) {
                        Date date = new Date();
                        mDatabase.updateBender(user.getAvatarId(), user.getId(),
                                user.getAvatarFile(), new Timestamp(date.getTime()));
                    }
                    callback.onSuccess(path);
                }

                @Override
                public void onFailure(String url) {
                    callback.onFailure();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This function returns the theoretical path of a User's avatar, regardless of
     * whether it exists or not.
     *
     * @param user user object
     * @return path
     */
    public String getAvatarFilePathIfExists(User user) {
        String url;
        try {
            url = getAvatarUrl(user);
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        ImageHandler ih = ImageHandler.getBenderHandler(mContext.getApplicationContext());
        return ih.getImagePathIfExists(url);
    }

    public void updateLastSeenBenderInformation(List<User> user_list) {
        Date date = new Date();
        mDatabase.updateLastSeenBenderInformation(user_list, new Timestamp(date.getTime()));
    }

    /**
     * This function returns the relative URL of a user's bender as stored on the mods.de website.
     *
     * @param user The user object
     * @return The URL as string
     * @throws UnsupportedEncodingException
     */
    public String getAvatarUrl(User user) throws UnsupportedEncodingException {
        if ((user.getAvatarFile() == null || user.getAvatarFile().equals("") ||
                user.getAvatarId() == 0) && !mDatabase.setCurrentBenderInformation(user))
            return null;

        String[] parts = user.getAvatarFile().split("/");
        parts[parts.length - 1] = URLEncoder.encode(parts[parts.length - 1], "UTF-8").replace("+", "%20");
        return Utils.getAbsoluteUrl(TextUtils.join("/", parts));
    }

    /**
     * The interface to implement for notification of success or failure of bender downloads.
     */
    public interface BenderListener {

        abstract void onSuccess(String path);

        abstract void onFailure();
    }
}
