package com.example.tingbaoweather.Service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.tingbaoweather.Activity.WeatherActivity;
import com.example.tingbaoweather.Bean.Weather;
import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Request;

/**
 * 后台每隔8小时下载最新天气数据,然后存入缓存
 */
public class AutoUpdateService extends Service {
    private String weatherId;
    private Weather weather;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String weather = preferences.getString("weather", null);
        if (weather != null) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(weather);
                JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
                String json = jsonArray.getJSONObject(0).toString();
                this.weather = new Gson().fromJson(json, Weather.class);
                weatherId = this.weather.basic.id;
                loadData(weatherId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String picture = preferences.getString("picture", null);
        if (picture != null) {
            loadPicture();
        }
        Log.i("Zz", "yes");
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int hour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + hour;
        Intent intent1 = new Intent(this, AutoUpdateService.class);
        PendingIntent pendingIntent = PendingIntent.getService(AutoUpdateService.this, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);//取消该闹钟
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pendingIntent);//Doze模式下依旧准时
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, pendingIntent);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void loadPicture() {
        final String urlPicture = "http://guolin.tech/api/bing_pic";
        OkHttpUtils.get().url(urlPicture).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("picture", response);
                editor.apply();

            }
        });
    }

    public void loadData(String weatherId) {

        String urlAll = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=28461361cfec4a99a237cd1f3176e0d6";
        OkHttpUtils.get().url(urlAll).build().execute(new StringCallback() {

            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
                    String json = jsonArray.getJSONObject(0).toString();
                    weather = new Gson().fromJson(json, Weather.class);
                    if (weather.status.equals("ok")) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", response);
                        editor.apply();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
