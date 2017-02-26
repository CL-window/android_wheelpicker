package com.cl.slack.androidpickerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.bigkoo.pickerview.OptionsPickerView;
import com.bigkoo.pickerview.TimePickerView;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * 时间选择器
     */
    public void birthSelect(final View view) {
        //时间选择器
        TimePickerView time = new TimePickerView(this, TimePickerView.Type.YEAR_MONTH_DAY);
        //控制时间范围
//        Calendar calendar = Calendar.getInstance();
//        pvTime.setRange(calendar.get(Calendar.YEAR) - 20, calendar.get(Calendar.YEAR));//要在setTime 之前才有效果哦
        time.setTime(new Date());
        time.setTitle("birthday");
        time.setCyclic(false);
        time.setCancelable(true);
        //时间选择后回调
        time.setOnTimeSelectListener(new TimePickerView.OnTimeSelectListener() {

            @Override
            public void onTimeSelect(Date date) {
                ((Button)view).setText(getTime(date));
            }
        });
        //弹出时间选择器
        time.show();
    }

    private String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return format.format(date).substring(0,10);
    }

    private final ArrayList<String> mSex = new ArrayList<String>(){
        {
            add("火星人");
            add("女");
            add("男");
        }
    };

    /**
     * 性别选择器
     */
    public void sexSelect(final View view) {
        OptionsPickerView sex = new OptionsPickerView(this);
        sex.setPicker(mSex);
        sex.setTitle("选择性别");
        sex.setCyclic(false);
        sex.setSelectOptions(1);
        sex.setCancelable(true);
        sex.setOnoptionsSelectListener(new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                ((Button)view).setText(mSex.get(options1));
            }
        });
        sex.show();
    }

    public void areaSelect(final View view) {
        AddressPickerTask area = new AddressPickerTask(this);
        area.setOnoptionsSelectListener(new AddressPickerTask.AreaOptionsSelectListener() {

            @Override
            public void onAddressInitFailed(String msg) {
                ((Button)view).setText(msg);
            }

            @Override
            public void onOptionsSelect(String province, String city, String county) {
                ((Button)view).setText(province + " " + city +  " " + county);
            }
        });
        area.execute("河北省","保定市","市辖区");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 返回键 的 dismiss picker
    }
}
