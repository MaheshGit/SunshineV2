package mks.android.sunshine.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import mks.android.sunshine.R;

/**
 * Created by Mahesh on 15/3/16.
 */
public class WeatherInfoAdapter extends RecyclerView.Adapter<WeatherInfoAdapter.WeatherViewHolder> {
    ArrayList<String> weatherDataList;

    public WeatherInfoAdapter(ArrayList<String> weatherDataList) {
        this.weatherDataList = weatherDataList;
    }

    @Override
    public WeatherViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_forecast, parent, false);
        WeatherViewHolder viewHolder = new WeatherViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(WeatherViewHolder holder, int position) {
        String data = weatherDataList.get(position);
        holder.weatherData.setText(data);
    }

    @Override
    public int getItemCount() {
        if (weatherDataList == null || weatherDataList.size() == 0) {
            return 0;
        }
        return weatherDataList.size();
    }

    public class WeatherViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView weatherData;

        public WeatherViewHolder(View itemView) {
            super(itemView);
            weatherData = (TextView) itemView.findViewById(R.id.list_item_forecast_textview);
            weatherData.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
