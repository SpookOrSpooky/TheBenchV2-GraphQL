package com.uottawa.thebench.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.uottawa.thebench.model.Chore;

/**
 * Class for storing and retrieving shared preferences.
 */
public class PrefUtils {

    private static final String PREF_NAMESPACE = "com.sportsfanquiz.sportsfanquiz.utils.USER";

    /**
     * Get the shared preferences object according to the namespace and the private mode.
     *
     * @param context Context
     * @return SharedPreferences object
     */
    public static SharedPreferences getUserPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAMESPACE, Context.MODE_PRIVATE);
    }

    /**
     * Set the user id for the current user. Also reset other shared prefs when new user id is set.
     *
     * @param currentUserId User id for user in the current session
     * @param context Context
     */
    public static void setCurrentUserId(long currentUserId, Context context) {
        getUserPreferences(context).edit().clear().commit();
        getUserPreferences(context).edit().putLong("currentUserId", currentUserId).commit();
    }

    /**
     * Get the current user id.
     *
     * @param context Context
     * @return User id for user in the current session
     */
    public static long getCurrentUserId(Context context) {
        return getUserPreferences(context).getLong("currentUserId", -1);
    }

    /**
     * Set the last selected status on the chores page.
     *
     * @param status Status
     * @param context Context
     */
    public static void setLastSelectedChoresStatus(Chore.Status status, Context context) {
        getUserPreferences(context).edit().putString("lastSelectedChoresStatus", status.name()).commit();
    }

    /**
     * Get the last selected status on the chores page.
     *
     * @param context Context
     * @return Status
     */
    public static Chore.Status getLastSelectedChoresStatus(Context context) {
        return Chore.Status.valueOf(getUserPreferences(context).getString("lastSelectedChoresStatus", Chore.Status.ACTIVE.name()));
    }

    /**
     * Set whether or not to filter by current user's chores.
     *
     * @param filterMyChores Filter by my chores only
     * @param context Context
     */
    public static void setFilterMyChores(boolean filterMyChores, Context context) {
        getUserPreferences(context).edit().putBoolean("filterMyChores", filterMyChores).commit();
    }

    /**
     * Get whether or not to filter by current user's chores.
     *
     * @param context Context
     * @return True if filtered by user's chore, false otherwise
     */
    public static boolean getFilterMyChores(Context context) {
        return getUserPreferences(context).getBoolean("filterMyChores", true);
    }

}
