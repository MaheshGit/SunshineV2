package mks.android.sunshine.utilities;

import android.util.Log;

import java.text.SimpleDateFormat;

import mks.android.sunshine.R;
import mks.android.sunshine.SunshineApplication;

/**
 * Created by Mahesh on 21/3/16.
 */
public class Utils {
    public static final String TAG = Utils.class.getSimpleName();

    public static String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time * 1000);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    public static String formatHighLows(double high, double low, String unitType) {
        if (unitType.equals(SunshineApplication.context.getString(R.string.pref_units_imperial))) {
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        } else if (!unitType.equals(SunshineApplication.context.getString(R.string.pref_units_metric))) {
            Log.d(TAG, "Unit type not found: " + unitType);
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }
}
