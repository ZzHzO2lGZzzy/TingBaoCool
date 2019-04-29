package com.example.tingbaoweather.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tingbaoweather.Base.BaseActivity;
import com.example.tingbaoweather.Bean.Weather;
import com.example.tingbaoweather.R;
import com.example.tingbaoweather.Service.AutoUpdateService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import com.google.gson.JsonObject;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Request;

public class WeatherActivity extends BaseActivity {
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    @BindView(R.id.title_textview)
    TextView titleTextView1;
    @BindView(R.id.time_textview)
    TextView timeTextView1;
    @BindView(R.id.weather_number)
    TextView numberTextView2;
    @BindView(R.id.weather_state)
    TextView stateTextView2;
    @BindView(R.id.forecast_linear)
    LinearLayout forecast_linear3;
    @BindView(R.id.weather_air_quality_number)
    TextView aqiNumberTextView4;
    @BindView(R.id.weather_air_quality_pm)
    TextView pmNumberTextView4;
    @BindView(R.id.comfort_textview)
    TextView comfortTextView5;
    @BindView(R.id.wash_textview)
    TextView washTextView5;
    @BindView(R.id.sport_textview)
    TextView sportTextView5;
    @BindView(R.id.background_imageview)
    ImageView backImageView;
    @BindView(R.id.smartrefreshlayout)
    public RefreshLayout smartRefreshLayout;
    @BindView(R.id.drawer_layout)
    public DrawerLayout drawerLayout;
    @BindView(R.id.house_imagebutton)
    ImageButton houseImageButton;
    private String url = "http://guolin.tech/api/weather?cityid=";
    private String weatherId;
    private Weather weather;
    private ProgressDialog progressDialog;
    private String weatherRefresh;


    @Override
    protected int getLayout() {
        return R.layout.weather_activity;
    }

    @Override
    protected void initView() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        String weather = preferences.getString("weather", null);
        if (weather != null) {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(weather);
                JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
                String json = jsonArray.getJSONObject(0).toString();
                this.weather = new Gson().fromJson(json, Weather.class);
                weatherRefresh = this.weather.basic.id;
                showweather(this.weather);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            weatherId = getIntent().getStringExtra("weatherId");
            loadData(weatherId);
        }
        String picture = preferences.getString("picture", null);
        if (picture != null) {
            Glide.with(WeatherActivity.this).load(picture).into(backImageView);
        } else {
            loadPicture();
        }

        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                loadData(weatherRefresh);

            }
        });

        houseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

    }

    public void loadData(String weatherId) {
        scrollView.setVisibility(View.INVISIBLE);
        String urlAll = url + weatherId + "&key=28461361cfec4a99a237cd1f3176e0d6";
        OkHttpUtils.get().url(urlAll).build().execute(new StringCallback() {
            @Override
            public void onBefore(Request request, int id) {
                super.onBefore(request, id);
                showProgressDialog();
            }

            @Override
            public void onAfter(int id) {
                super.onAfter(id);
                closeProgressDialog();
                smartRefreshLayout.finishRefresh();
            }

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
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString("weather", response);
                        editor.apply();
                        weatherRefresh = weather.basic.id;
                        showweather(weather);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void showweather(Weather weather) {
        scrollView.setVisibility(View.VISIBLE);
        titleTextView1.setText(weather.basic.city);
        timeTextView1.setText(weather.basic.update.loc);
        numberTextView2.setText(weather.now.tmp + "℃");
        stateTextView2.setText(weather.now.cond.txt);
        forecast_linear3.removeAllViews();
        for (int i = 0; i < weather.daily_forecastList.size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.weather_linear_item, forecast_linear3, false);

            TextView dataTextView3_1 = view.findViewById(R.id.data_text);
            TextView stateTextView3_1 = view.findViewById(R.id.weather_state_text);
            TextView numberTextView3_1 = view.findViewById(R.id.weather_number_text);
            TextView numberTextView2_3_1 = view.findViewById(R.id.weather_number_2_text);

            dataTextView3_1.setText(weather.daily_forecastList.get(i).date);
            stateTextView3_1.setText(weather.daily_forecastList.get(i).cond.txt_d);
            numberTextView3_1.setText(weather.daily_forecastList.get(i).tmp.max);
            numberTextView2_3_1.setText(weather.daily_forecastList.get(i).tmp.min);

            forecast_linear3.addView(view);
        }
        if (weather.aqi != null) {
            aqiNumberTextView4.setText(weather.aqi.city.aqi);
            pmNumberTextView4.setText(weather.aqi.city.pm25);
        }

        comfortTextView5.setText(weather.suggestion.comf.txt);
        washTextView5.setText(weather.suggestion.cw.txt);
        sportTextView5.setText(weather.suggestion.sport.txt);

        loadPicture();
        startService();
    }

    private void loadPicture() {
        final String urlPicture = "http://guolin.tech/api/bing_pic";
        OkHttpUtils.get().url(urlPicture).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("picture", response);
                editor.apply();

                Glide.with(WeatherActivity.this).load(response).into(backImageView);
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private void startService() {
        Intent intent = new Intent(WeatherActivity.this, AutoUpdateService.class);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
