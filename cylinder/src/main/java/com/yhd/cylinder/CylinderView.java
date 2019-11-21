package com.yhd.cylinder;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 圆角柱形,可选中,而且要居中显示
 * Created by haide.yin(haide.yin@tcl.com) on 2019/7/25 13:34.
 */
public class CylinderView extends View {

    private static final String TAG = CylinderView.class.getSimpleName();

    /* ********************* 外部设置的属性 *********************** */
    //颜色属性
    private int behindColor = Color.parseColor("#8AFFA239");//背后柱形的颜色
    private int behindSelectColor = Color.parseColor("#FFFFA239");//背后柱形的颜色
    private int frontColor = Color.parseColor("#8A398EFF");//前面柱形的颜色
    private int frontSelectColor = Color.parseColor("#FF398EFF");//前面柱形的颜色
    //柱形属性
    private float barWidthRatio = 0.05f;//正常柱形宽度百分比
    private float selectBarWidthRatio = 0.07f;//选中的柱形宽度百分比
    private float distanceRatio = 0.05f;//柱形间距宽度百分比
    private float marginTextRatio = 0.03f;//柱形距离横坐标点娿距离百分比
    //画板四周边距
    private float marginLeftRatio = 0.05f;//画板左边距百分比
    private float marginRightRatio = 0.05f;//画板右边距百分比
    private float marginTopRatio = 0.05f;//画板上边距百分比
    private float marginBottomRatio = 0.05f;//画板下边距百分比
    //文字属性
    private float textRatio = 0.05f;//文字的大小百分比
    //气泡的属性
    private float bubbleHeightRatio = 0.2f;//泡泡的高度百分比
    private float bubbleWidthRatio = 0.2f;//泡泡的长度百分比
    private float triangleRatio = 0.02f;//尖部三角形边长百分比
    //动画
    private int animationTime = 1000;//动画持续时间

    /**
     * 柱形高度分布情况,是一个String[]类表,规则如下
     * float[0]:前面柱形高度百分比(0-1f)
     * float[1]:后面柱形高度百分比(0-1f)
     */
    private List<float[]> heightArray = new ArrayList<>();
    //x坐标轴的文字描述列表
    private List<String> xAxisArray = new ArrayList<>();
    //点击选中之后显示的文字.需要换行的用'/'分开
    private List<String> tipsArray = new ArrayList<>();

    /* ********************* 内部使用的属性 *********************** */
    private int textGrayColor = Color.parseColor("#61000000");//文字灰色
    private int bubbleColor = Color.parseColor("#8A000000");//文字灰色
    private Paint barPaint;//画柱形的画笔
    private Paint textPaint;//画文字画笔
    private List<RectF> barRectList = new ArrayList<>();//柱形区域列表
    private int selectIndex = -1;//当前选中的柱形
    //动画监听
    private ChartAnimator chartAnimator = new ChartAnimator(animation -> postInvalidate());

    public CylinderView(Context context) {
        this(context, null);
    }

    public CylinderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CylinderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //View被窗体移除的时候释放动画资源
        chartAnimator.release();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFoucus) {
        super.onWindowFocusChanged(hasFoucus);
        //View焦点变化
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        chartAnimator.start(animationTime);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        if (getWidth() > 0 && getHeight() > 0) {
            //清空列表
            barRectList.clear();
            //柱形的区域宽度
            float canvasWidth = (1f - marginLeftRatio - marginRightRatio) * getWidth();
            //柱形区域的高度
            float canvasHeight = (1f - marginTopRatio - marginBottomRatio - marginTextRatio - textRatio) * getHeight();
            //开始画柱形
            if (heightArray != null && heightArray.size() > 0) {
                float totalWidth = (heightArray.size() * barWidthRatio  + (heightArray.size() -1) * distanceRatio) * getWidth();
                //如果超过则平分区域
                if(totalWidth > canvasWidth){
                    barWidthRatio = (1f - marginLeftRatio - marginRightRatio) / (heightArray.size() * 2 -1);
                    distanceRatio = barWidthRatio;
                    selectBarWidthRatio = barWidthRatio * 1.3f;
                }
                //画笔的起点
                float chartTempStart = (getWidth() - (heightArray.size() * barWidthRatio * getWidth() + (heightArray.size() - 1) * distanceRatio * getWidth())) / 2;
                //开始画柱形与文字
                for (int i = 0; i < heightArray.size(); i++) {
                    float[] selectValue = heightArray.get(i);
                    if (selectValue.length >= 2) {
                        //后面的柱形先画,乘上动画比例
                        float phaseY = chartAnimator.getPhaseY();
                        float behindBarHeight = selectValue[1] * canvasHeight * phaseY;
                        //画背景柱状图矩形
                        RectF behindRectF = new RectF();
                        behindRectF.top = canvasHeight - behindBarHeight + marginTopRatio * getHeight();
                        behindRectF.bottom = canvasHeight + marginTopRatio * getHeight();
                        if (i == selectIndex) {
                            barPaint.setColor(behindSelectColor);
                            behindRectF.left = chartTempStart;
                            behindRectF.right = chartTempStart + (selectBarWidthRatio) * getWidth();
                            chartTempStart = chartTempStart + (selectBarWidthRatio + distanceRatio) * getWidth();
                        } else {
                            barPaint.setColor(behindColor);
                            behindRectF.left = chartTempStart;
                            behindRectF.right = chartTempStart + (barWidthRatio) * getWidth();
                            chartTempStart = chartTempStart + (barWidthRatio + distanceRatio) * getWidth();
                        }
                        barRectList.add(behindRectF);
                        drawSmoothBar(canvas, behindRectF, behindBarHeight, barPaint);
                        //绘制前面的柱形,乘上动画比例
                        float frontBarHeight = selectValue[0] * canvasHeight * phaseY;
                        RectF frontRectF = new RectF();
                        frontRectF.top = canvasHeight - frontBarHeight + marginTopRatio * getHeight();
                        frontRectF.left = behindRectF.left;
                        frontRectF.right = behindRectF.right;
                        frontRectF.bottom = behindRectF.bottom;
                        if (i == selectIndex) {
                            barPaint.setColor(frontSelectColor);
                        } else {
                            barPaint.setColor(frontColor);
                        }
                        drawSmoothBar(canvas, frontRectF, frontBarHeight, barPaint);
                        //开始画横坐标
                        textPaint.setTextSize(textRatio * getHeight());
                        if (i == selectIndex) {
                            textPaint.setColor(Color.BLACK);
                            textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
                        } else {
                            textPaint.setColor(textGrayColor);
                            textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
                        }
                        canvas.drawText(xAxisArray.get(i), frontRectF.centerX(), (1 - marginBottomRatio) * getHeight(), textPaint);
                        //画泡泡
                        if (i == selectIndex) {
                            //气泡显示内容
                            String tipString = tipsArray.get(i);
                            //绘制气泡
                            textPaint.setColor(bubbleColor);
                            textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
                            float bubuleWidth = getBubbleWidth(textPaint, tipString, bubbleWidthRatio * getWidth());
                            float bubbleHeight = getBubbleHeight(textPaint, tipString, bubbleHeightRatio * getHeight());
                            float bubleTop;
                            if (behindRectF.top - bubbleHeight - bubbleHeight / 2 <= marginTopRatio * getHeight()) {
                                bubleTop = marginTopRatio * getHeight();
                            } else {
                                bubleTop = behindRectF.top - bubbleHeight - bubbleHeight / 2;
                            }
                            RectF rectBuble = new RectF(behindRectF.centerX() - bubuleWidth / 2, bubleTop, behindRectF.centerX() + bubuleWidth / 2, bubleTop + bubbleHeight);
                            canvas.drawPath(drawBubble(rectBuble, triangleRatio * getWidth()), textPaint);
                            Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
                            //找出中心位置
                            float bottomLineY = rectBuble.centerY() - (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.top;
                            textPaint.setColor(Color.WHITE);

                            float textSize = textRatio * getHeight();
                            if (tipString.contains("/")) {
                                String[] tipsString = tipString.split("/");
                                String firstString = tipsString[0];
                                String secondString = tipsString[1];
                                canvas.drawText(firstString, rectBuble.centerX(), bottomLineY - textSize * 0.6f, textPaint);
                                canvas.drawText(secondString, rectBuble.centerX(), bottomLineY + textSize * 0.6f, textPaint);
                            } else {
                                canvas.drawText(tipString, rectBuble.centerX(), bottomLineY, textPaint);
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                int x = (int) event.getX();
                int y = (int) event.getY();
                for (RectF rectF : barRectList) {//选中
                    if (rectF.contains(x, y)) {
                        selectIndex = barRectList.indexOf(rectF);
                        invalidate();
                        break;
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 初始化
     */
    private void init(AttributeSet attrs) {
        //初始化属性
        if (attrs != null) {
            //初始化布局属性
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.CylinderView, 0, 0);
            barWidthRatio = typedArray.getFloat(R.styleable.CylinderView_cy_barWidthRatio, barWidthRatio);
            selectBarWidthRatio = typedArray.getFloat(R.styleable.CylinderView_cy_selectBarWidthRatio, selectBarWidthRatio);
            distanceRatio = typedArray.getFloat(R.styleable.CylinderView_cy_sepRatio, distanceRatio);
            marginTextRatio = typedArray.getFloat(R.styleable.CylinderView_cy_marginTextRatio, marginTextRatio);
            marginLeftRatio = typedArray.getFloat(R.styleable.CylinderView_cy_marginLeftRatio, marginLeftRatio);
            marginRightRatio = typedArray.getFloat(R.styleable.CylinderView_cy_marginRightRatio, marginRightRatio);
            marginTopRatio = typedArray.getFloat(R.styleable.CylinderView_cy_marginTopRatio, marginTopRatio);
            marginBottomRatio = typedArray.getFloat(R.styleable.CylinderView_cy_marginBottomRatio, marginBottomRatio);
            textRatio = typedArray.getFloat(R.styleable.CylinderView_cy_textRatio, textRatio);
            triangleRatio = typedArray.getFloat(R.styleable.CylinderView_cy_triangleRatio, triangleRatio);
            animationTime = typedArray.getInteger(R.styleable.CylinderView_cy_animationTime, animationTime);
        }
        //初始化画笔
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setAntiAlias(true); // 抗锯齿
        barPaint.setDither(true); // 防抖动
        barPaint.setStyle(Paint.Style.FILL);
        //文字画笔
        textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setDither(true); // 防抖动
        textPaint.setAntiAlias(true);// 抗锯齿
        textPaint.setStrokeWidth(1);//画笔宽度

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

        tipsArray.add("Deephhh 20 min/Light 18 min");
        tipsArray.add("Deep  min/Light min");
        tipsArray.add("Deep in/Light in");
        tipsArray.add("Deep n/Light n");
        tipsArray.add("Deep / Light");
        tipsArray.add("Deep/Light");
    }

    /**
     * 读取气泡的宽度
     */
    private float getBubbleWidth(Paint paint, String valueString, float defaultValue) {
        if (!TextUtils.isEmpty(valueString)) {
            //加字符串00是因为多测量一个多出两边的间距
            float sepSize = paint.measureText("00");
            if (valueString.contains("/")) {
                String[] tipsString = valueString.split("/");
                String firstString = tipsString[0];
                String secondString = tipsString[1];
                float firstWidth = paint.measureText(firstString);
                float secondWidth = paint.measureText(secondString);
                return Math.max(firstWidth, secondWidth) + sepSize;
            } else {
                return paint.measureText(valueString) + sepSize;
            }
        }
        return defaultValue;
    }

    /**
     * 读取气泡的宽度
     */
    private float getBubbleHeight(Paint paint, String valueString, float defaultValue) {
        if (!TextUtils.isEmpty(valueString)) {
            float sepSize = paint.measureText("00");
            if (valueString.contains("/")) {
                //多出的0.2表示两行文字的间距
                return paint.getTextSize() * 2.2f + sepSize;
            } else {
                return paint.getTextSize() + sepSize;
            }
        }
        return defaultValue;
    }

    /**
     * 绘制圆滑的柱形
     */
    private void drawSmoothBar(Canvas canvas, RectF barRectf, float barHeight, Paint paint) {
        //半径
        float radius = (barRectf.right - barRectf.left) / 2;
        //定义一个正方形
        RectF squareRectF = new RectF();
        squareRectF.left = barRectf.left;
        squareRectF.right = barRectf.right;
        squareRectF.bottom = barRectf.bottom;
        squareRectF.top = barRectf.bottom - radius * 2 - 1;//剪掉一个像素,不然出现一条色条
        //半数值直角边
        float cosHeight = radius - barHeight;
        //算出竖直边与斜边夹角
        int anger = (int) (180 * Math.acos(cosHeight / radius) / Math.PI);
        if (barHeight <= radius) {//小于半径高度绘制扇形
            canvas.drawArc(squareRectF, 90 - anger, anger * 2, false, paint);//绘制圆弧，不含圆心
        } else if (barHeight > radius && barHeight <= radius * 2) {//大一一个半径下两个半径绘制半圆加长方形
            //画一个半圆
            canvas.drawArc(squareRectF, 0, 180, false, paint);//绘制圆弧，不含圆心
            //定义一个长方形
            RectF longRectF = new RectF();
            longRectF.left = barRectf.left;
            longRectF.right = barRectf.right;
            longRectF.bottom = barRectf.bottom - radius;
            longRectF.top = barRectf.top;
            canvas.drawRect(longRectF, barPaint);//画长方形
        } else {//大于直径绘制柱形
            canvas.drawRoundRect(barRectf, (barRectf.right - barRectf.left) / 2, (barRectf.right - barRectf.left) / 2, paint);
        }
    }

    /**
     * 绘制气泡提示框
     *
     * @param myRect 形状
     * @return 路径
     */
    private Path drawBubble(RectF myRect, float triangleWidth) {
        int radius = 10;
        final Path path = new Path();
        final float left = myRect.left;
        final float top = myRect.top;
        final float right = myRect.right;
        final float bottom = myRect.bottom;
        final float centerX = myRect.centerX();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            path.addRoundRect(left, top, right, bottom, radius, radius, Path.Direction.CCW);
        } else {
            // TODO: 2019/8/1 API 21 以下绘制圆角
            //定位到左上角
            path.moveTo(left, top);
            //到右上角
            path.lineTo(right, top);
            //右边
            path.lineTo(right, bottom);
            path.lineTo(left, bottom);
            path.lineTo(left, top);
        }
        Path traAnglePath = new Path();
        traAnglePath.moveTo(centerX + triangleWidth, bottom);
        traAnglePath.lineTo(centerX, myRect.bottom + triangleWidth);
        traAnglePath.lineTo(centerX - triangleWidth, bottom);
        path.addPath(traAnglePath);
        traAnglePath.close();
        path.close();
        return path;
    }

    /**
     * 设置数据源
     *
     * @param heightArray 高度比
     * @param xAxisArray  坐标值
     * @param tipsArray   单击提示
     */
    public void setdataSource(List<float[]> heightArray, List<String> xAxisArray, List<String> tipsArray) {
        if (heightArray != null && xAxisArray != null && tipsArray != null
                && heightArray.size() == xAxisArray.size()
                && xAxisArray.size() == tipsArray.size()) {
            selectIndex = -1;
            this.heightArray = heightArray;
            this.xAxisArray = xAxisArray;
            this.tipsArray = tipsArray;
            chartAnimator.start(animationTime);
        } else {
            Log.e(TAG, "invalid data");
        }
    }

    /**
     * 清空画布
     */
    public void clearView() {
        barPaint.setShader(null);
        textPaint.setShader(null);
    }

    /**
     * 圆柱绘制持续的动画类
     */
    private class ChartAnimator {

        private float mPhaseY = 1f; //默认动画值0f-1f
        private ValueAnimator.AnimatorUpdateListener mListener;//监听
        private ObjectAnimator objectAnimator;

        private ChartAnimator(ValueAnimator.AnimatorUpdateListener listener) {
            mListener = listener;
        }

        private float getPhaseY() {
            return mPhaseY;
        }

        private void setPhaseY(float phase) {
            mPhaseY = phase;
        }

        /**
         * Y轴动画
         *
         * @param durationMillis 持续时间
         */
        private void start(int durationMillis) {
            release();
            objectAnimator = ObjectAnimator.ofFloat(this, "phaseY", 0f, 1f);
            objectAnimator.setDuration(durationMillis);
            objectAnimator.addUpdateListener(mListener);
            objectAnimator.start();
        }

        /**
         * 释放动画
         */
        private void release() {
            if (objectAnimator != null) {
                objectAnimator.end();
                objectAnimator.cancel();
                objectAnimator = null;
            }
        }
    }
}
