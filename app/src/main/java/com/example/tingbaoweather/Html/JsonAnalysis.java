package com.example.tingbaoweather.Html;

import android.text.TextUtils;

import com.example.tingbaoweather.Bean.City;
import com.example.tingbaoweather.Bean.Country;
import com.example.tingbaoweather.Bean.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JsonAnalysis {

    public static List<Province> getProvinceList(String json) {
        if (TextUtils.isEmpty(json)) return null;
        List<Province> provinceList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                Province province = new Province();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                province.setProvinceCode(jsonObject.optInt("id"));
                province.setProvinceName(jsonObject.optString("name"));
                provinceList.add(province);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return provinceList;
    }

    public static List<City> getCityList(String json, int urlProvince) {
        if (TextUtils.isEmpty(json)) return null;
        List<City> cityList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                City city = new City();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                city.setCityCode(jsonObject.optInt("id"));
                city.setCityName(jsonObject.optString("name"));
                city.setProvinceId(urlProvince);
                cityList.add(city);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return cityList;
    }

    public static List<Country> getCountryList(String json, int urlCity) {
        if (TextUtils.isEmpty(json)) return null;
        List<Country> countryList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                Country country = new Country();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                country.setId(jsonObject.optInt("id"));
                country.setCountryName(jsonObject.optString("name"));
                country.setWeatherId(jsonObject.optString("weather_id"));
                country.setCityId(urlCity);
                countryList.add(country);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return countryList;
    }
}
