package com.cl.slack.androidpickerview;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.bigkoo.pickerview.OptionsPickerView;
import com.cl.slack.androidpickerview.area.City;
import com.cl.slack.androidpickerview.area.County;
import com.cl.slack.androidpickerview.area.Province;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by slack
 * on 17/2/26 下午3:23.
 */

public class AddressPickerTask extends AsyncTask<String, Void, ArrayList<Province>> {

    private final static String TAG = "AddressPickerTask";
    private WeakReference<Context> mContext;
    private AreaOptionsSelectListener mCallback;

    /**
     * 第一级 省 市
     */
    private ArrayList<String> mOptions1Items = new ArrayList<>();

    /**
     * 第二级 地市
     */
    private ArrayList<ArrayList<String>> mOptions2Items = new ArrayList<>();

    /**
     * 第三级 区 县
     */
    private ArrayList<ArrayList<ArrayList<String>>> mOptions3Items = new ArrayList<>();

    private OptionsPickerView mOptionsPickerView;
    private String mSelectedProvince = "", mSelectedCity = "", mSelectedCounty = "";

    public AddressPickerTask(Context context) {
        mContext = new WeakReference<Context>(context);
    }

    public AddressPickerTask setOnoptionsSelectListener(AreaOptionsSelectListener l) {
        mCallback = l;
        return this;
    }

    public AddressPickerTask dismiss() {
        if(mOptionsPickerView != null){
            mOptionsPickerView.dismiss();
        }
        return this;
    }

    @Override
    protected void onPreExecute() {
        Log.i(TAG, "onPreExecute...");
    }

    @Override
    protected ArrayList<Province> doInBackground(String... params) {
        Log.i(TAG, "doInBackground...");

        if (params != null) {
            switch (params.length) {
                case 1:
                    mSelectedProvince = params[0];
                    break;
                case 2:
                    mSelectedProvince = params[0];
                    mSelectedCity = params[1];
                    break;
                case 3:
                    mSelectedProvince = params[0];
                    mSelectedCity = params[1];
                    mSelectedCounty = params[2];
                    break;
                default:
                    break;
            }
        }

        ArrayList<Province> data = new ArrayList<>();
        try {
            String json = getJsonString("city.json");
            data.addAll(JSON.parseArray(json, Province.class));

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return data;
    }

    @Override
    protected void onPostExecute(ArrayList<Province> result) {
        Log.i(TAG, "onPostExecute...");
        if (result != null) {
            Context context = getContext();
            if (context != null) {
                showPicker(result,context);
            } else {
                Log.e(TAG, "context null");
                if (mCallback != null) {
                    mCallback.onAddressInitFailed("context null");
                }
            }
        } else {
            if (mCallback != null) {
                mCallback.onAddressInitFailed("province null");
            }
        }
    }

    private void showPicker(ArrayList<Province> result, Context context) {
        mOptionsPickerView = new OptionsPickerView(context);
        int selectedProvince = -1, selectedCity = -1, selectedCounty = -1;
        int provinces = result.size();
        for (int i = 0; i < provinces; i++) {
            Province province = result.get(i);
            mOptions1Items.add(province.name);// 1
            if(selectedProvince == -1){
                if(province.name.endsWith(mSelectedProvince)){
                    selectedProvince = i;
                }
            }
            ArrayList<String> options2Item = new ArrayList<>();
            ArrayList<ArrayList<String>> options3Item = new ArrayList<>();
            int citys = province.child.size();
            for (int j = 0; j < citys; j++) {
                City city = province.child.get(j);
                options2Item.add(city.name);// 2-2
                ArrayList<String> options3 = new ArrayList<>();
                int countys = city.child.size();
                for (int k = 0; k < countys; k++) {
                    County county = city.child.get(k);
                    options3.add(county.name);// 3-3
                    if(selectedCounty == -1 ){
                        if(county.name.endsWith(mSelectedCounty)){
                            selectedCounty = k;
                        }
                    }
                }
                options3Item.add(options3);// 3-2
                if(selectedCity == -1 ){
                    if(city.name.endsWith(mSelectedCity)){
                        selectedCity = j;
                    }
                }
            }
            mOptions3Items.add(options3Item);// 3-1
            mOptions2Items.add(options2Item);// 2-1

        }
        mOptionsPickerView.setPicker(mOptions1Items, mOptions2Items, mOptions3Items, true);
        mOptionsPickerView.setSelectOptions(
                selectedProvince == -1 ? 0 : selectedProvince,
                selectedCity == -1 ? 0 : selectedCity,
                selectedCounty == -1 ? 0 : selectedCounty);
        mOptionsPickerView.setCyclic(false, false, false);
        mOptionsPickerView.setOnoptionsSelectListener(mOnOptionsSelectListener);
        mOptionsPickerView.show();
    }

    private String getJsonString(String assertFileName) {
        StringBuilder sb = new StringBuilder();
        try {
            Context context = getContext();
            if (context != null) {
                InputStream is = context.getAssets().open(assertFileName);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    } else {
                        sb.append(line).append("\n");
                    }
                }
                reader.close();
                is.close();
            } else {
                Log.e(TAG, "context null...");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "getJsonString..." + e.toString());
        }
        return sb.toString();
    }

    private Context getContext() {
        if (mContext == null) {
            return null;
        }
        return mContext.get();
    }

    private OptionsPickerView.OnOptionsSelectListener mOnOptionsSelectListener =
            new OptionsPickerView.OnOptionsSelectListener(){

        @Override
        public void onOptionsSelect(int option1, int option2, int option3) {
            if (mCallback != null) {
                mCallback.onOptionsSelect(
                        mOptions1Items.get(option1),
                        mOptions2Items.get(option1).get(option2),
                        mOptions3Items.get(option1).get(option2).get(option3));
            }
        }
    };

    interface AreaOptionsSelectListener{
        void onAddressInitFailed(String msg);
        void onOptionsSelect(String province, String city, String county);
    }
}
