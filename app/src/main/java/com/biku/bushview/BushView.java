package com.biku.bushview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * @author czc
 * @date 2017/11/8 0008.
 */

public class BushView extends View {

    private Bitmap tape;
    private Paint mTapePaint;
    private float mLastX;
    private float mLastY;
    private float mDownX;
    private float mDownY;
    private List<TapeInfo> mTapeList = new ArrayList<>();
    private Paint p;
    private int slop;

    public BushView(Context context) {
        super(context);
        init();
    }

    public BushView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BushView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        tape = BitmapFactory.decodeResource(getResources(), R.drawable.paint_type);
        float radio = (float) tape.getWidth() / tape.getHeight();
        tape = Bitmap.createScaledBitmap(tape, (int) (100 * radio), 100, false);
        slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (TapeInfo ti : mTapeList) {
            drawTape(canvas, ti, tape);
        }
    }

    private void drawTape(Canvas canvas, TapeInfo info, Bitmap bitmap) {
        float startX = info.start.x;
        float startY = info.start.y;

        float endX = info.end.x;
        float endY = info.end.y;
        double distance = distance(startX, startY, endX, endY);
        double degree = computeDegree(startX, startY, endX, endY);
        List<RectF> mList = new ArrayList<>();
        int number = (int) (distance / bitmap.getWidth()) + 1;
        for (int i = 0; i < number; i++) {
            RectF dist = new RectF((bitmap.getWidth()) * i + startX, startY - bitmap.getHeight() / 2, bitmap.getWidth() * (i + 1) + startX, startY + bitmap.getHeight() / 2);
            mList.add(dist);
        }
        RectF r = new RectF(startX, startY - bitmap.getHeight() / 2, startX + (float) distance, startY + bitmap.getHeight() / 2);

        canvas.save();
        canvas.rotate((float) degree, startX, startY);
        canvas.clipRect(r);
        for (RectF dist : mList) {
            canvas.drawBitmap(bitmap, null, dist, null);
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        float x = event.getX();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDownX = mLastX = x;
                mDownY = mLastY = y;
                TapeInfo tapeInfo = new TapeInfo();
                tapeInfo.start = new PointF(mDownX, mDownY);
                tapeInfo.end = new PointF(mLastX, mLastY);
                mTapeList.add(tapeInfo);
                break;
            case MotionEvent.ACTION_MOVE:
                mLastX = x;
                mLastY = y;
                if (distance(mDownX, mDownY, mLastX, mLastY) > slop) {
                    TapeInfo tapeInfo1 = mTapeList.get(mTapeList.size() - 1);
                    tapeInfo1.end.set(mLastX, mLastY);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                mLastX = x;
                mLastY = y;
                if (distance(mDownX, mDownY, mLastX, mLastY) < slop) {
                    if (mTapeList.size() > 0) {
                        mTapeList.remove(mTapeList.size() - 1);
                    }
                }
                invalidate();
                break;
        }
        return true;
    }

    public static double distance(float x1, float y1, float x2, float y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    public double computeDegree(float x1, float y1, float x2, float y2) {
        double sin = (y2 - y1) / Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        //角度
        double angle = Math.asin(sin) / Math.PI * 180;
        double degree = 0;
        float tran_x = x2 - x1;
        float tran_y = y2 - y1;
        if (!Double.isNaN(degree)) {
            if (tran_x >= 0 && tran_y <= 0) {//第一象限
                degree = angle;
            } else if (tran_x <= 0 && tran_y <= 0) {//第二象限
                degree = -180 - angle;
            } else if (tran_x <= 0 && tran_y >= 0) {//第三象限
                degree = 180 - angle;
            } else if (tran_x >= 0 && tran_y >= 0) {//第四象限
                degree = angle;
            }
        }
        return degree;
    }

    public class TapeInfo {
        PointF start;
        PointF end;
    }

}
