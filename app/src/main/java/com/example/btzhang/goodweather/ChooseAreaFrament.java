package com.example.btzhang.goodweather;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.test.ProviderTestCase2;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.btzhang.goodweather.db.City;
import com.example.btzhang.goodweather.db.Country;
import com.example.btzhang.goodweather.db.Province;
import com.example.btzhang.goodweather.util.HttpUtil;
import com.example.btzhang.goodweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by BTZHANG on 2018/1/19.
 */

public class ChooseAreaFrament extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;
    private ProgressDialog progressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;
    private Province selectedP;
    private City selectedC;
    private Country selectedCY;
    private int currentLevel;

//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        titleText = (TextView) view.findViewById(R.id.title_text);
        backButton = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel==LEVEL_PROVINCE) {
                    selectedP = provinceList.get(position);
                    queryCities();
                } else if(currentLevel==LEVEL_CITY){
                    selectedC = cityList.get(position);
                    queryCountries();
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel==LEVEL_COUNTRY) {
                    queryCities();
                } else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size()>0) {
            dataList.clear();
            for (Province province:provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    private void queryCities() {
        titleText.setText(selectedP.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
//        City cj=DataSupport.findFirst(City.class);
        cityList = DataSupport.where("provinceId = ? ", String.valueOf(selectedP.getId())).find(City.class);
//        cityList = DataSupport.findAll(City.class);
        if (cityList.size()>0) {
            dataList.clear();
            for (City city:cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        } else {
            int provincecode = selectedP.getProvinceCode();
            String address = "http://guolin.tech/api/china/"+provincecode;
            queryFromServer(address,"city");
        }
    }

    private void queryCountries() {
        titleText.setText(selectedC.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countryList = DataSupport.where("cityId = ? ", String.valueOf(selectedC.getId())).find(Country.class);
        if (countryList.size()>0) {
            dataList.clear();
            for (Country country:countryList) {
                dataList.add(country.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTRY;
        } else {
            int provincecode = selectedP.getProvinceCode();
            int citycode = selectedC.getCityCode();
            String address = "http://guolin.tech/api/china/"+provincecode+"/"+citycode;
            queryFromServer(address,"country");
        }
    }

    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String reponseText = response.body().string();
                boolean result=false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(reponseText);
                } else if("city".equals(type)){
                    result = Utility.handleCityResponse(reponseText,selectedP.getId());
                } else if ("country".equals(type)) {
                    result = Utility.handleCountryResponse(reponseText,selectedC.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("country".equals(type)) {
                                queryCountries();
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showProgressDialog() {
        if (progressDialog==null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    private void closeProgressDialog() {
        if (progressDialog!=null) {
            progressDialog.dismiss();
        }
    }
}
