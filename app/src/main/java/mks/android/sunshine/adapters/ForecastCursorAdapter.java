package mks.android.sunshine.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import mks.android.sunshine.R;
import mks.android.sunshine.SunshineApplication;
import mks.android.sunshine.database.WeatherContract;
import mks.android.sunshine.utilities.Utils;

/**
 * Created by Mahesh on 29/3/16.
 */
public class ForecastCursorAdapter extends CursorAdapter {

    public ForecastCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tv = (TextView) view;
        tv.setText(convertCursorRowToUXFormat(cursor));
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        boolean isMetric = Utils.isMetric(SunshineApplication.getContext());
        String highLowStr = Utils.formatTemperature(high, isMetric) + "/" + Utils.formatTemperature(low, isMetric);
        return highLowStr;
    }

    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.
     */
    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        int idx_max_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
        int idx_min_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
        int idx_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
        int idx_short_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);

        String highAndLow = formatHighLows(
                cursor.getDouble(idx_max_temp),
                cursor.getDouble(idx_min_temp));

        return Utils.getReadableDateString(cursor.getLong(idx_date)) +
                " - " + cursor.getString(idx_short_desc) +
                " - " + highAndLow;
    }

}
