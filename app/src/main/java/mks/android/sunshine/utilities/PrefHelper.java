package mks.android.sunshine.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import mks.android.sunshine.SunshineApplication;

/**
 * Created by Mahesh on 22/3/16.
 */
public class PrefHelper {
    Context mContext;

    public PrefHelper() {
        mContext = SunshineApplication.context;
    }

    public void setString(String key, String value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key, String defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return pref.getString(key, defaultValue);
    }

    public void setInt(String key, int value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public int getInt(String key, int defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return pref.getInt(key, defaultValue);
    }

    public void setLong(String key, long value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public long getLong(String key, long defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return pref.getLong(key, defaultValue);
    }

    public void setBoolean(String key, Boolean value) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return pref.getBoolean(key, defaultValue);
    }

    public void clearPref() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }
}
