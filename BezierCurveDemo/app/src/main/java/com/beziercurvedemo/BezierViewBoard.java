package com.beziercurvedemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Fishy on 2017/3/14.
 */

public class BezierViewBoard extends View {
    final String TAG_LOG = "BezierViewBoard";
    /**
     * 贝塞尔二次曲线
     */
    final String Bezier_2 = "bezier_2";
    /**
     * 贝塞尔三次曲线
     */
    final String Bezier_3 = "bezier_3";
    /**
     * 上下文
     */
    Context context;
    /**
     * view的宽
     */
    int layoutWidth;
    /**
     * view的高
     */
    int layoutHeight;
    /**
     * 我的画笔
     */
    Paint mPaint;
    /**
     * 默认的画笔颜色
     */
    int defaultPaintColor = Color.BLACK;
    /**
     * 默认的点的颜色
     */
    int defaultPointColor = Color.BLUE;
    /**
     * 默认的线条粗细
     */
    int defaultPathSize = 2;
    /**
     * 默认的点的粗细
     */
    int defaultPointWidth = 5;
    /**
     * 所有的绘制的点，以及线的map
     */
    Map<String, List<Point>> pointMap;
    /**
     * 当前操作的point集合
     */
    List<Point> currentPoints;
    /**
     * 当前所处的状态
     */
    BoradStatus status;
    /**
     * 编号，从0开始
     */
    int number = 0;
    /**
     * 是否为二次贝塞尔
     */
    boolean isBezier_2=true;

    /**
     * 一共四种状态，分别是空闲->起点->终点->准备画第二个定位点
     * 其中第四种状态只有在使用3次贝塞尔曲线的时候才生效
     * 使用2次贝塞尔曲线的时候只有3种状态
     */
    enum BoradStatus {
        /**
         * 空闲
         */
        IDLE,
        /**
         * 起点已画好
         */
        START_FINISH,
        /**
         * 终点已画好
         */
        END_FINISH,
        /**
         * 两个定位点的情况下才有这种状态
         */
        SECOND_PREPARE
    }

    public BezierViewBoard(Context context) {
        this(context, null);
    }

    public BezierViewBoard(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public BezierViewBoard(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            //down和move事件的时候不真正将点位置加入结合，只刷新并重绘
            case MotionEvent.ACTION_DOWN:
                Point point = new Point();
                point.x = x;
                point.y = y;
                currentPoints.add(point);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                Point point1 = currentPoints.get(currentPoints.size() - 1);
                point1.x = x;
                point1.y = y;
                invalidate();
                return true;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                //记录并改变状态
                if (status == BoradStatus.IDLE) {
                    status = BoradStatus.START_FINISH;
                } else if (status == BoradStatus.START_FINISH) {
                    status = BoradStatus.END_FINISH;
                } else if (status == BoradStatus.END_FINISH) {
                    if (isBezier2()) {
                        status = BoradStatus.IDLE;
                        commitPoints(Bezier_2, currentPoints);
                    } else {
                        status = BoradStatus.SECOND_PREPARE;
                    }
                } else {
                    status = BoradStatus.IDLE;
                    commitPoints(Bezier_3, currentPoints);
                }
                invalidate();
                Log.i(TAG_LOG,status.toString());
                return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 提交当前points并清空
     *
     * @param key
     * @param points
     */
    void commitPoints(String key, List<Point> points) {
        List<Point> newPoints = new ArrayList<>();
        newPoints.addAll(points);
        pointMap.put(key + "_" + number, newPoints);
        number++;
        points.clear();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //得到测绘模式
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //得到测绘的尺寸
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        //最后需要设置的宽、高
        int width;
        int height;
        //根据mode重新设置尺寸
        //默认将wrap_content模式当成match_parent处理
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            width = widthSize;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = heightSize;
        }
        setMeasuredDimension(width, height);
        layoutWidth = width;
        layoutHeight = height;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //这是正在画的部分
        for (Point point : currentPoints) {
            //先调节画笔,再画
            mPaint.setStrokeWidth(px2Dpi(defaultPointWidth));
            mPaint.setColor(defaultPointColor);
            canvas.drawPoint(point.x, point.y, mPaint);
            //还原
            mPaint.setStrokeWidth(px2Dpi(defaultPathSize));
            mPaint.setColor(defaultPaintColor);
            if (status == BoradStatus.END_FINISH) {
                if (currentPoints.size() == 3) {
                    drawBezier2(currentPoints, canvas);
                }
            } else if (status == BoradStatus.SECOND_PREPARE) {
                if (currentPoints.size() == 3) {
                    drawBezier2(currentPoints, canvas);
                } else if (currentPoints.size() == 4) {
                    drawBezier3(currentPoints, canvas);
                }
            }
        }
        //这是已经画好的部分
        Iterator<Map.Entry<String, List<Point>>> iterator = pointMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<Point>> entry = iterator.next();
            String key = entry.getKey();
            List<Point> points = entry.getValue();
            if (key.contains(Bezier_2)) {
                if (points.size() == 3) {
                    drawBezier2(points, canvas);
                }
            } else if (key.contains(Bezier_3)) {
                if (points.size() == 4) {
                    drawBezier3(points, canvas);
                }
            }
            //把点画上去
            for (Point point : points) {
                //先改画笔配置
                changePaintConf(true);
                canvas.drawPoint(point.x, point.y, mPaint);
                //再还原
                changePaintConf(false);
            }
        }
    }

    /**
     * 清除画板
     */
    public void clearBoard() {
        pointMap.clear();
        currentPoints.clear();
        invalidate();
    }

    /**
     * 撤销，回到上一个点
     */
    public void cancelForBack() {
        if(status==BoradStatus.IDLE){
            //TODO 暂时不影响，已经绘制完成的不撤销
        }else if(status==BoradStatus.START_FINISH){
            currentPoints.remove(currentPoints.size()-1);
            status=BoradStatus.IDLE;
        }else if(status==BoradStatus.END_FINISH){
            currentPoints.remove(currentPoints.size()-1);
            status=BoradStatus.START_FINISH;
        }else {
            currentPoints.remove(currentPoints.size()-1);
            status=BoradStatus.END_FINISH;
        }
        invalidate();
    }

    public void setBezier_2(boolean bezier_2) {
        isBezier_2 = bezier_2;
        currentPoints.clear();
        status=BoradStatus.IDLE;
        invalidate();
    }

    /**
     * 设置默认的画笔颜色
     *
     * @param defaultPaintColor
     */
    public void setDefaultPaintColor(int defaultPaintColor) {
        this.defaultPaintColor = defaultPaintColor;
        invalidate();
    }

    /**
     * 设置默认的点颜色
     * @param defaultPointColor
     */
    public void setDefaultPointColor(int defaultPointColor) {
        this.defaultPointColor = defaultPointColor;
        invalidate();
    }

    /**
     * 设置默认的线条尺寸
     *
     * @param defaultPathSize
     */
    public void setDefaultPathSize(int defaultPathSize) {
        this.defaultPathSize = defaultPathSize;
        invalidate();
    }

    /**
     * 设置默认的点的尺寸
     *
     * @param defaultPointWidth
     */
    public void setDefaultPointWidth(int defaultPointWidth) {
        this.defaultPointWidth = defaultPointWidth;
        invalidate();
    }

    /**
     * 画二次贝塞尔曲线
     *
     * @param points
     * @param canvas
     */
    void drawBezier2(List<Point> points, Canvas canvas) {
        //根据点位置画贝塞尔曲线，以下同理
        Path path = new Path();
        Point pStart = points.get(0);
        path.moveTo(pStart.x, pStart.y);
        Point pEnd = points.get(1);
        Point pHelper = points.get(2);
        path.quadTo(pHelper.x, pHelper.y, pEnd.x, pEnd.y);
        canvas.drawPath(path, mPaint);
    }

    /**
     * 画三次贝塞尔曲线
     *
     * @param points
     * @param canvas
     */
    void drawBezier3(List<Point> points, Canvas canvas) {
        Path path = new Path();
        Point pStart = points.get(0);
        path.moveTo(pStart.x, pStart.y);
        Point pEnd = points.get(1);
        Point pHelper1 = points.get(2);
        Point pHelper2 = points.get(3);
        path.cubicTo(pHelper1.x, pHelper1.y, pHelper2.x, pHelper2.y,
                pEnd.x, pEnd.y);
        canvas.drawPath(path, mPaint);
    }

    /**
     * 更改画笔设置
     *
     * @param isPoint 是否画点
     */
    void changePaintConf(boolean isPoint) {
        if (isPoint) {
            mPaint.setStrokeWidth(px2Dpi(defaultPointWidth));
            mPaint.setColor(defaultPointColor);
            mPaint.setStyle(Paint.Style.FILL);
        } else {
            mPaint.setStrokeWidth(px2Dpi(defaultPathSize));
            mPaint.setColor(defaultPaintColor);
            mPaint.setStyle(Paint.Style.STROKE);
        }
    }

    /**
     * 初始化
     */
    void init() {
        //画笔初始化
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        changePaintConf(false);
        pointMap = new HashMap<>();
        currentPoints = new ArrayList<>();
        //设置可点击，否则无法响应move事件
        setClickable(true);
        //初始状态为idle
        status = BoradStatus.IDLE;
    }

    /**
     * px转dp
     *
     * @param px
     * @return
     */
    int px2Dpi(int px) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int dp = (int) (metrics.density * px);
        return dp;
    }

    /**
     * 是否为二次贝塞尔曲线
     *
     * @return 默认为true
     */
    boolean isBezier2() {
        return isBezier_2;
    }
}
