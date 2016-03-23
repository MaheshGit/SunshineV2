package mks.android.sunshine;

import android.app.Application;
import android.content.Context;

/**
 * Created by Mahesh on 22/3/16.
 */
public class SunshineApplication extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext(){
        return context;
    }
}
