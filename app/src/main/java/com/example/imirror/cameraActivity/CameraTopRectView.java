package com.example.imirror.cameraActivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

class CameraTopRectView extends View {

    private int panelWidth;
    private int panelHeght;

    private int viewWidth;
    private int viewHeight;

    public int rectWidth;
    public int rectHeght;

    private int rectTop;
    private int rectLeft;
    private int rectRight;
    private int rectBottom;

    private int lineLen;
    private int lineWidht;
    private static final int LINE_WIDTH = 5;
    private static final int TOP_BAR_HEIGHT = 50;
    private static final int BOTTOM_BTN_HEIGHT = 66;

//    private static final int TOP_BAR_HEIGHT = Constant.RECT_VIEW_TOP;
//    private static final int BOTTOM_BTN_HEIGHT = Constant.RECT_VIEW_BOTTOM;

    private static final int LEFT_PADDING = 10;
    private static final int RIGHT_PADDING = 10;
    private static final String TIPS = "請對準鏡頭";

    private Paint linePaint;
    private Paint wordPaint;
    private Rect rect;
    private int baseline;

    public CameraTopRectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        Activity activity = (Activity) context;
        //
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        panelWidth = getResources().getDisplayMetrics().widthPixels;
        panelHeght = getResources().getDisplayMetrics().heightPixels;

        //高度不需要dp转换px,不然整体相机会向上移动一小节
//        viewHeight = panelHeght - (int) DisplayUtil.dp2px(activity,TOP_BAR_HEIGHT + BOTTOM_BTN_HEIGHT);

        //viewHeight 界面的高；viewWidth 界面的宽
        viewHeight = panelHeght;
        viewWidth = panelWidth;

        /*rectWidth = panelWidth
                - UnitUtils.getInstance(activity).dip2px(
                        LEFT_PADDING + RIGHT_PADDING);*/

        rectWidth = panelWidth - (int) DisplayUtil.dp2px(activity,LEFT_PADDING + RIGHT_PADDING);

        rectHeght = (int) (rectWidth * 54 / 85.6);
        // 相对于此view
        rectTop = (viewHeight - rectHeght) / 2;
        rectLeft = (viewWidth - rectWidth) / 2;
        rectBottom = rectTop + rectHeght;
        rectRight = rectLeft + rectWidth;

        lineLen = panelWidth / 8;

        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.rgb(0xff, 0xff, 0xff)); // 設置顏色(white)
        linePaint.setStyle(Style.STROKE);
        linePaint.setStrokeWidth(LINE_WIDTH);// 設置線寬
        linePaint.setAlpha(255);

        wordPaint = new Paint();
        wordPaint.setAntiAlias(true);
        wordPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wordPaint.setStrokeWidth(3);
        wordPaint.setTextSize(35);

        //疑似是拍攝視窗?"請對準鏡頭"下面那個框
        rect = new Rect(rectLeft, rectTop - 80, rectRight, rectTop - 10);
        FontMetricsInt fontMetrics = wordPaint.getFontMetricsInt();
        baseline = rect.top + (rect.bottom - rect.top - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;
        wordPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        wordPaint.setColor(Color.TRANSPARENT);
        canvas.drawRect(rect, wordPaint);

        //黑色透明的底
        wordPaint.setColor(0xa0000000); //#F7F7FA

        rect = new Rect(0, viewHeight/2+rectHeght/2, viewWidth, viewHeight); //下
        canvas.drawRect(rect, wordPaint);

        rect = new Rect(0, 0, viewWidth, viewHeight/2-rectHeght/2); //上
        canvas.drawRect(rect, wordPaint);

        //左
        //rect = new Rect(0, viewHeight/2-rectHeght/2, (viewWidth-rectWidth)/2, viewHeight/2+rectHeght/2);
        rect = new Rect(0, (viewHeight/2-rectHeght/2), (viewWidth-rectWidth)/2+200, viewHeight/2+rectHeght/2);
        canvas.drawRect(rect, wordPaint);

        //右
        //rect = new Rect(viewWidth-(viewWidth-rectWidth)/2, viewHeight/2-rectHeght/2, viewWidth, viewHeight/2+rectHeght/2);
        rect = new Rect(viewWidth-(viewWidth-rectWidth)/2-200, viewHeight/2-rectHeght/2, viewWidth, viewHeight/2+rectHeght/2);
        canvas.drawRect(rect, wordPaint);


        //重制rect  並畫文字  把文字置於rect中間
        rect = new Rect(rectLeft, rectTop - 80, rectRight, rectTop - 10);
        wordPaint.setColor(Color.WHITE); //白色
        canvas.drawText(TIPS, rect.centerX(), baseline, wordPaint); //文字

        /*canvas.drawLine(rectLeft, rectTop, rectLeft + lineLen, rectTop,
                linePaint);
          canvas.drawLine(rectRight - lineLen, rectTop, rectRight, rectTop,
                linePaint);
          canvas.drawLine(rectLeft, rectTop, rectLeft, rectTop + lineLen,
                linePaint);
          canvas.drawLine(rectRight, rectTop, rectRight, rectTop + lineLen,
                linePaint)*/
        //最上面兩條白色的橫線
        canvas.drawLine(rectLeft+200, rectTop, rectLeft + lineLen+200, rectTop,
                linePaint);
        canvas.drawLine(rectRight - lineLen-200, rectTop, rectRight-200, rectTop,
                linePaint);
        //兩條白色的直線
        canvas.drawLine(rectLeft+200, rectTop, rectLeft+200, rectTop + lineLen,
                linePaint);
        canvas.drawLine(rectRight-200, rectTop, rectRight-200, rectTop + lineLen,
                linePaint);

        /*canvas.drawLine(rectLeft, rectBottom, rectLeft + lineLen, rectBottom,
                linePaint);
        canvas.drawLine(rectRight - lineLen, rectBottom, rectRight, rectBottom,
                linePaint);
        canvas.drawLine(rectLeft, rectBottom - lineLen, rectLeft, rectBottom,
                linePaint);
        canvas.drawLine(rectRight, rectBottom - lineLen, rectRight, rectBottom,
                linePaint);*/
        //下面的兩條白色直線
        canvas.drawLine(rectLeft+200, rectBottom, rectLeft + lineLen+200, rectBottom,
                linePaint);
        canvas.drawLine(rectRight - lineLen-200, rectBottom, rectRight-200, rectBottom,
                linePaint);
        //下面的兩條白色橫線
        canvas.drawLine(rectLeft+200, rectBottom - lineLen, rectLeft+200, rectBottom,
                linePaint);
        canvas.drawLine(rectRight-200, rectBottom - lineLen, rectRight-200, rectBottom,
                linePaint);
    }

    public int getRectLeft() {
        return rectLeft;
    }

    public int getRectTop() {
        return rectTop;
    }

    public int getRectRight() {
        return rectRight;
    }

    public int getRectBottom() {
        return rectBottom;
    }

    public int getViewWidth() {
        return viewWidth;
    }

    public int getViewHeight() {
        return viewHeight;
    }

}