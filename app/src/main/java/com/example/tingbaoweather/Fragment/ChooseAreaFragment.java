package com.example.tingbaoweather.Fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tingbaoweather.Base.BaseFragment;
import com.example.tingbaoweather.Bean.City;
import com.example.tingbaoweather.Bean.Country;
import com.example.tingbaoweather.Bean.Province;
import com.example.tingbaoweather.Html.JsonAnalysis;
import com.example.tingbaoweather.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import okhttp3.Call;
import okhttp3.Request;


public class ChooseAreaFragment extends BaseFragment {
    @BindView(R.id.back_image_button)
    ImageButton backButton;
    @BindView(R.id.area_text)
    TextView areaText;
    @BindView(R.id.listview)
    ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> areaList = new ArrayList<>();

    private ProgressDialog progressDialog;

    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;
    private int Level;

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;

    private int urlProvince;
    private int urlCity;

    private Province chooseProvince;
    private City chooseCity;

    @Override
    protected void initView(View view, Bundle savedInstanceState) {
        adapter = new ArrayAdapter<>(mActivity, android.R.layout.simple_list_item_1, areaList);
        listView.setAdapter(adapter);
        showProvinces();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.choose_area;
    }

    @Override
    public void initData() {
        super.initData();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Level == LEVEL_PROVINCE) {
                    chooseProvince = provinceList.get(position);
                    showCity();
                } else if (Level == LEVEL_CITY) {
                    chooseCity = cityList.get(position);
                    showCountry();
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (Level) {
                    case LEVEL_CITY:
                        showProvinces();
                        break;
                    case LEVEL_COUNTRY:
                        showCity();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void showProvinces() {
        areaText.setText("中国");
        backButton.setVisibility(View.INVISIBLE);
        provinceList = LitePal.findAll(Province.class);
        Level = LEVEL_PROVINCE;
        if (provinceList.size() > 0) { //从数据库提取
            areaList.clear();
            for (Province province : provinceList) {
                areaList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
        } else {
            String url = "http://guolin.tech/api/china";
            loadData(url, Level);
        }
    }

    private void loadData(String url, final int Level) {
        OkHttpUtils.get().url(url).build().execute(new StringCallback() {

            @Override
            public void onBefore(Request request, int id) {
                showProgressDialog();
                super.onBefore(request, id);
            }

            @Override
            public void onAfter(int id) {
                closeProgressDialog();
                super.onAfter(id);
            }

            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                switch (Level) {
                    case LEVEL_PROVINCE:
                        provinceList = JsonAnalysis.getProvinceList(response);
                        //写入数据库
                        if (provinceList != null && provinceList.size() > 0) {
                            for (int i = 0; i < provinceList.size(); i++) {
                                Province province = new Province();
                                province.setProvinceName(provinceList.get(i).getProvinceName());
                                province.setProvinceCode(provinceList.get(i).getProvinceCode());
                                province.save();
                            }
                        }
                        showProvinces();
                        break;
                    case LEVEL_CITY:
                        cityList = JsonAnalysis.getCityList(response, urlProvince);
                        //写入数据库
                        if (cityList != null && cityList.size() > 0) {
                            for (int i = 0; i < cityList.size(); i++) {
                                City city = new City();
                                city.setCityName(cityList.get(i).getCityName());
                                Log.i("Zz", "name=" + cityList.get(i).getCityName());

                                city.setCityCode(cityList.get(i).getCityCode());
                                city.setProvinceId(urlProvince);
                                city.save();
                            }
                        }
                        showCity();
                        break;
                    case LEVEL_COUNTRY:

                        countryList = JsonAnalysis.getCountryList(response, urlCity);
                        //写入数据库
                        if (countryList != null && countryList.size() > 0) {
                            for (int i = 0; i < countryList.size(); i++) {
                                Country country = new Country();
                                country.setWeatherId(countryList.get(i).getWeatherId());
                                country.setCountryName(countryList.get(i).getCountryName());
                                country.setCityId(urlCity);
                                country.save();
                            }
                        }
                        showCountry();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void showCity() {
        areaText.setText(chooseProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceId=?", String.valueOf(chooseProvince.getId())).find(City.class);
//        cityList = LitePal.findAll(City.class);
        Level = LEVEL_CITY;
        if (cityList.size() > 0) { //从数据库提取
            areaList.clear();
            for (City city : cityList) {
                areaList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
        } else {
            urlProvince = chooseProvince.getProvinceCode();
            String url = "http://guolin.tech/api/china/" + urlProvince;
            loadData(url, Level);
        }
    }

    private void showCountry() {
        areaText.setText(chooseCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countryList = LitePal.where("cityId=?", String.valueOf(chooseCity.getCityCode())).find(Country.class);
        Level = LEVEL_COUNTRY;
        if (countryList.size() > 0) { //从数据库提取
            areaList.clear();
            for (Country country : countryList) {
                areaList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            Log.i("Zz", "数据库提取country");
        } else {
            urlCity = chooseCity.getCityCode();
            String url = "http://guolin.tech/api/china/" + urlProvince + "/" + urlCity;
            loadData(url, Level);
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
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
}
