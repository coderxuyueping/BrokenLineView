package com.tg.test.brokenlineview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by xyp on 2018/3/28.
 * 年化折线图
 */

public class BrokenLineView extends View {
    private Context context;
    //x轴间距
    private int x_Space;
    //y轴间距
    private int y_Space;


    private float xLength = 20;//虚线的长度
    private float xHeight = 4;//虚线的高度
    private float xMargin = 40;//虚线之间的间隔

    private int lineColor;//折线颜色
    private int xColor;//坐标线颜色
    private int textColor;//坐标点字体颜色

    private Paint linePaint;
    private Paint xyPaint;
    private Paint textPaint;
    private Bitmap bitmap;

    /**
     * 这三个集合需要外部给到数据
     */
    private List<String> xText = new ArrayList<>();//放x坐标上的文字
    private List<String> yText = new ArrayList<>();//放y坐标上的文字
    private List<Float> income = new ArrayList<>();//每个月对应的收益
    private String unit;//单位

    private float maxYTextWidth;//y轴上文字的最大宽度
    private List<Float> xPoint = new ArrayList<>();//存放x轴坐标点
    private List<Float> yPoint = new ArrayList<>();//存放y轴坐标点
    private Map<String, PointF> realPoint = new ArrayMap<>();//事件每个月对应的利率坐标

    private int widthSize, heightSize;

    public BrokenLineView(Context context) {
        this(context, null);
    }

    public BrokenLineView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BrokenLineView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BrokenLineView);
        lineColor = typedArray.getColor(R.styleable.BrokenLineView_lineColor, context.getResources().getColor(R.color.line_color));
        xColor = typedArray.getColor(R.styleable.BrokenLineView_xColor, context.getResources().getColor(R.color.color_99));
        textColor = typedArray.getColor(R.styleable.BrokenLineView_textColor, context.getResources().getColor(R.color.color_99));
        x_Space = typedArray.getInt(R.styleable.BrokenLineView_x_space, 60);
        y_Space = typedArray.getInt(R.styleable.BrokenLineView_y_space, 100);
        typedArray.recycle();

        init();
    }

    private void init() {
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(4);

        xyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        xyPaint.setColor(xColor);
        xyPaint.setStrokeWidth(xHeight);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(textColor);
        textPaint.setTextSize(dp2px(context, 12));//px
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_last_point_text);
    }

    public int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    //测量View的宽高,适配了高度warp跟宽度math的情况
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //算出Y轴最大宽度
        for (String text : yText) {
            text += unit;
            maxYTextWidth = Math.max(maxYTextWidth, textPaint.measureText(text));
        }

        int xTextWidth = 0;
        for (String text : xText) {
            xTextWidth += textPaint.measureText(text);
        }

        int width = measureDimension((int) (maxYTextWidth * (xText.size() + 5)), widthMeasureSpec);
        //高度warp
        int height = measureDimension(y_Space * (yText.size() + 1) + bitmap.getHeight(), heightMeasureSpec);
        widthSize = width;
        heightSize = height;
        //宽度math
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
            x_Space = (int) ((widthSize - maxYTextWidth - xTextWidth) / xText.size() - 2);
        }
        Log.d("xudaha", "broken height:" + height + "---width:" + width + "---x_space:" + x_Space);
        setMeasuredDimension(width, height);
    }


    private int measureDimension(int defaultSize, int measureSpec) {
        int size = 0;
        int mode = MeasureSpec.getMode(measureSpec);
        int measureSize = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {//精确值
            size = measureSize;
        } else if (mode == MeasureSpec.AT_MOST) {//wrap_content,需要计算
            size = defaultSize;
        }
        return size;
    }


    //绘制方法
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.xText.size() > 0 && this.yText.size() > 0) {
            //先画坐标系
            drawXY(canvas);
            //画x和y的坐标系文字
            drawText(canvas);
        }

        if (this.income.size() > 0) {
            //画折线
            drawLine(canvas);
            //画最后一个点的图片
            drawLastPoint(canvas);
        }
    }

    private void drawLastPoint(Canvas canvas) {
        Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.icon_last_point);
        PointF pointF = realPoint.get(xText.get(xText.size() - 1));
        canvas.drawBitmap(bitmap1, pointF.x - bitmap1.getWidth() / 2, pointF.y - bitmap1.getHeight() / 2, linePaint);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        canvas.drawBitmap(bitmap, pointF.x - width, pointF.y - height, linePaint);
        textPaint.setColor(getResources().getColor(R.color.white));
        String text = income.get(income.size() - 1) + "%";
        float textWidth = textPaint.measureText(text);
        canvas.drawText(text, pointF.x - width / 2 - textWidth / 2, pointF.y - height / 2, textPaint);
        textPaint.setColor(textColor);
    }

    private void drawLine(Canvas canvas) {
        Path path = new Path();
        float yPoint = getPointY(income.get(0));
        PointF movePoint = new PointF(xPoint.get(0), yPoint);
        //起点
        path.moveTo(movePoint.x, movePoint.y);
        realPoint.put(xText.get(0), movePoint);
        for (int i = 1; i < income.size(); i++) {
            PointF pointF = new PointF(xPoint.get(i), getPointY(income.get(i)));
            path.lineTo(pointF.x, pointF.y);
            Log.d("xudaha", "pointF.x=" + pointF.x + "---pointF.y=" + pointF.y);
            realPoint.put(xText.get(i), pointF);
        }
        canvas.drawPath(path, linePaint);

//        Path path = new Path();
//        path.moveTo(xPoint.get(0),yPoint.get(2));
//        path.lineTo(xPoint.get(1),yPoint.get(1));
//        path.lineTo(xPoint.get(2),yPoint.get(2));
//        path.lineTo(xPoint.get(3),yPoint.get(0));
//        path.lineTo(xPoint.get(4),yPoint.get(3));
//        path.lineTo(xPoint.get(5),yPoint.get(1));
//        canvas.drawPath(path,linePaint);

    }

    //算出改点在Y轴哪个区间
    private float getPointY(float point) {
        int index = -1;
        for (int i = 0; i < yText.size(); i++) {
            if (point < Float.valueOf(yText.get(i))) {
                index = i;
                break;
            }
        }
        //处理边界
        if (index == 0) {
            return yPoint.get(0);
        }

        //比最大值还大
        if (index == -1) {
            return yPoint.get(yPoint.size() - 1);
        }

        //每一格对应实际的利率
        float ySpace = Float.valueOf(yText.get(index)) - Float.valueOf(yText.get(index - 1));
        float percent = (point - Float.valueOf(yText.get(index - 1))) / ySpace;//对应的占比
        return -percent * y_Space + yPoint.get(index - 1);
    }

    //画X轴的文字
    private void drawText(Canvas canvas) {
        canvas.save();
        //文字x轴绘制在y轴文字的宽度加上x间隔距离，y轴绘制在最底部再往上一个半个y间隔距离
        canvas.translate(maxYTextWidth + x_Space, heightSize - y_Space / 2);
        float space = 0;
        for (int i = 0; i < xText.size(); i++) {
            String text = xText.get(i);
            canvas.drawText(text, space, 0, textPaint);
            float textWidth = textPaint.measureText(text);
            xPoint.add(maxYTextWidth + x_Space + space + textWidth / 2);
            space += textWidth + x_Space;
        }
        canvas.restore();
    }

    //画y轴的文字以及y轴对应的虚线
    private void drawXY(Canvas canvas) {
        canvas.save();
        float[] pts = {0, 0, xLength, 0};
        float translateY = heightSize - xHeight - 100 - y_Space / 2;
        //从最底部开始绘制
        canvas.translate(x_Space / 2, translateY);
        for (int i = 0; i < yText.size(); i++) {
            float totalXSize = 0;
            String text = yText.get(i) + unit;
            canvas.drawText(text, 0, 0, textPaint);
            yPoint.add(translateY - i * y_Space);

            //画每一条虚线,需要往右偏移y文字宽度
            canvas.translate(maxYTextWidth + x_Space / 2, 0);

            while (totalXSize < widthSize) {
                canvas.drawLines(pts, xyPaint);
                //移动间隔距离
                canvas.translate(xLength + xMargin, 0);
                totalXSize = totalXSize + xLength + xMargin;
            }

            canvas.translate(-totalXSize - maxYTextWidth - x_Space / 2, -y_Space);
        }
        canvas.restore();
    }

    public void drawLine(List<String> xText, List<String> yText, List<Float> income, String unit) {
        this.xText.clear();
        this.xText.addAll(xText);
        this.yText.clear();
        this.yText.addAll(yText);
        this.income.clear();
        this.income.addAll(income);
        this.unit = unit;
        //invalidate();
        requestLayout();
    }
}
