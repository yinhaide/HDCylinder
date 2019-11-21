package com.yhd.cylinder.app.frag;

import android.view.View;

import com.de.rocket.ue.frag.RoFragment;
import com.de.rocket.ue.injector.BindView;
import com.de.rocket.ue.injector.Event;
import com.yhd.cylinder.CylinderView;
import com.yhd.cylinder.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 圆柱状图
 * Created by haide.yin(haide.yin@tcl.com) on 2019/6/6 16:12.
 */
public class Frag_cylinder extends RoFragment {


    @BindView(R.id.bcv)
    private CylinderView cylinderView;

    @Override
    public int onInflateLayout() {
        return R.layout.frag_cylinder;
    }

    @Override
    public void initViewFinish(View inflateView) {

    }

    @Override
    public void onNexts(Object object) {

    }

    @Event(R.id.bt_update)
    private void update(View view) {
        /**
         * 柱形高度分布情况,是一个String[]类表,规则如下
         * float[0]:前面柱形高度百分比(0-1f)
         * float[1]:后面柱形高度百分比(0-1f)
         */
        List<float[]> heightArray = new ArrayList<>();
        //x坐标轴的文字描述列表
        List<String> xAxisArray = new ArrayList<>();
        //点击选中之后显示的文字.需要换行的用'/'分开
        List<String> tipsArray = new ArrayList<>();
        //设置默认值

        heightArray.add(new float[]{0.02f, 0.5f});
        heightArray.add(new float[]{0.05f, 0.8f});
        heightArray.add(new float[]{0.08f, 0.9f});
        heightArray.add(new float[]{0.1f, 1f});
        heightArray.add(new float[]{0.3f, 0.5f});
        heightArray.add(new float[]{0.4f, 0.7f});

        xAxisArray.add("1");
        xAxisArray.add("2");
        xAxisArray.add("3");
        xAxisArray.add("4");
        xAxisArray.add("5");
        xAxisArray.add("6");

        tipsArray.add("Deephhhhdd 20 min/Light 18 min");
        tipsArray.add("Deep 10 min/Light 15 min");
        tipsArray.add("Deep 30 min/Light 28 min");
        tipsArray.add("Deep 40 min/Light 16 min");
        tipsArray.add("Deep/Light");
        tipsArray.add("Deep 50 min/Light 18 min");

        cylinderView.setdataSource(heightArray, xAxisArray, tipsArray);
    }
}
