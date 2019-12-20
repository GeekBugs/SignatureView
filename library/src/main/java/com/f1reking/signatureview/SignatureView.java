
/*
 * Copyright 2018 F1ReKing.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.f1reking.signatureview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.ColorInt;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author F1ReKing
 * @date 2018/10/31 15:09
 * @Description 签名板
 */
public class SignatureView extends View {

    private static final String TAG = SignatureView.class.getSimpleName();

    public static final int PEN_WIDTH = 10;
    public static final int PEN_COLOR = Color.BLACK;
    public static final int BACK_COLOR = Color.WHITE;

    //画笔x坐标起点
    private float mPenX;
    //画笔y坐标起点
    private float mPenY;
    private Paint mPaint = new Paint();
    private Path mPath = new Path();
    private Canvas mCanvas;
    private Bitmap cacheBitmap;
    //画笔宽度
    private int mPentWidth = PEN_WIDTH;
    //画笔颜色
    private int mPenColor = PEN_COLOR;
    //画板颜色
    private int mBackColor = BACK_COLOR;
    private boolean isTouched = false;
    private String mSavePath = null;

    public SignatureView(Context context) {
        this(context, null);
    }

    public SignatureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignatureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SignatureView);
        mPenColor = typedArray.getColor(R.styleable.SignatureView_penColor, PEN_COLOR);
        mBackColor = typedArray.getColor(R.styleable.SignatureView_backColor, BACK_COLOR);
        mPentWidth = typedArray.getInt(R.styleable.SignatureView_penWidth, PEN_WIDTH);
        typedArray.recycle();
        init();
    }

    private void init() {
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mPentWidth);
        mPaint.setColor(mPenColor);
    }

    public boolean getTouched() {
        return isTouched;
    }

    public void setPentWidth(int pentWidth) {
        mPentWidth = pentWidth;
    }

    public void setPenColor(@ColorInt int penColor) {
        mPenColor = penColor;
    }

    public void setBackColor(@ColorInt int backColor) {
        mBackColor = backColor;
    }

    /**
     * 清空签名
     */
    public void clear() {
        if (mCanvas != null) {
            isTouched = false;
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mCanvas.drawColor(mBackColor);
            invalidate();
        }
    }

    /**
     * 保存图片
     *
     * @param path 保存的地址
     * @param clearBlank 是否清除空白区域
     * @param blank 空白区域留空距离
     * @throws IOException
     */
    public void save(String path, boolean clearBlank, int blank) throws IOException {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        mSavePath = path;
        Bitmap bitmap = cacheBitmap;
        if (clearBlank) {
            bitmap = clearBlank(bitmap, blank);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] buffer = bos.toByteArray();
        if (buffer != null) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            OutputStream os = new FileOutputStream(file);
            os.write(buffer);
            os.close();
            bos.close();
        }
    }

    /**
     * 获取Bitmap缓存
     */
    public Bitmap getBitmap() {
        setDrawingCacheEnabled(true);
        buildDrawingCache();
        Bitmap bitmap = getDrawingCache();
        setDrawingCacheEnabled(false);
        return bitmap;
    }

    /**
     * 获取保存路径
     */
    public String getSavePath() {
        return mSavePath;
    }

    /**
     * 逐行扫描，清除边界空白
     *
     * @param blank 边界留多少个像素
     */
    private Bitmap clearBlank(Bitmap bmp, int blank) {
        int height = bmp.getHeight();
        int width = bmp.getWidth();
        int top = 0, left = 0, right = 0, bottom = 0;
        int[] pixs = new int[width];
        boolean isStop;
        //扫描上边距不等于背景颜色的第一个点
        for (int i = 0; i < height; i++) {
            bmp.getPixels(pixs, 0, width, 0, i, width, 1);
            isStop = false;
            for (int pix :
                pixs) {
                if (pix != mBackColor) {
                    top = i;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        //扫描下边距不等于背景颜色的第一个点
        for (int i = height - 1; i >= 0; i--) {
            bmp.getPixels(pixs, 0, width, 0, i, width, 1);
            isStop = false;
            for (int pix :
                pixs) {
                if (pix != mBackColor) {
                    bottom = i;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        pixs = new int[height];
        //扫描左边距不等于背景颜色的第一个点
        for (int x = 0; x < width; x++) {
            bmp.getPixels(pixs, 0, 1, x, 0, 1, height);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    left = x;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        //扫描右边距不等于背景颜色的第一个点
        for (int x = width - 1; x > 0; x--) {
            bmp.getPixels(pixs, 0, 1, x, 0, 1, height);
            isStop = false;
            for (int pix : pixs) {
                if (pix != mBackColor) {
                    right = x;
                    isStop = true;
                    break;
                }
            }
            if (isStop) {
                break;
            }
        }
        if (blank < 0) {
            blank = 0;
        }
        //计算加上保留空白距离之后的图像大小
        left = left - blank > 0 ? left - blank : 0;
        top = top - blank > 0 ? top - blank : 0;
        right = right + blank > width - 1 ? width - 1 : right + blank;
        bottom = bottom + blank > height - 1 ? height - 1 : bottom + blank;
        return Bitmap.createBitmap(bmp, left, top, right - left, bottom - top);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cacheBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(cacheBitmap);
        mCanvas.drawColor(mBackColor);
        isTouched = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(cacheBitmap, 0, 0, mPaint);
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPenX = event.getX();
                mPenY = event.getY();
                mPath.moveTo(mPenX, mPenY);
                return true;
            case MotionEvent.ACTION_MOVE:
                isTouched = true;
                float x = event.getX();
                float y = event.getY();
                float penX = mPenX;
                float penY = mPenY;
                float dx = Math.abs(x - penX);
                float dy = Math.abs(y - penY);
                if (dx >= 3 || dy >= 3) {
                    float cx = (x + penX) / 2;
                    float cy = (y + penY) / 2;
                    mPath.quadTo(penX, penY, cx, cy);
                    mPenX = x;
                    mPenY = y;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mCanvas.drawPath(mPath, mPaint);
                mPath.reset();
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }
}
