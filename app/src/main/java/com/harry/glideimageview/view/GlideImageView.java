/*
 * Copyright (C) 20015 MaiNaEr All rights reserved
 */
package com.harry.glideimageview.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.harry.glideimageview.R;

/**
 * 类/接口描述
 *
 * @author Harry
 * @date 2016/3/28.
 */
public class GlideImageView extends ImageView {
    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.TRANSPARENT;
    private int placeholderId = 0;
    private int failureImageId = 0;
    private boolean roundAsCircle = false;
    private int roundedCornerRadius = 0;
    private int mBorderColor = DEFAULT_BORDER_COLOR;
    private int mBorderWidth = DEFAULT_BORDER_WIDTH;

    private Paint mBorderPaint = new Paint();
    private final RectF mBorderRect = new RectF();
    private RequestManager manager;
    private DrawableTypeRequest builder;
    private RoundTransform mRoundTrans;
    private CircleTransform mCircleTrans;

    public GlideImageView(Context context) {
        super(context);
    }

    public GlideImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public GlideImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        if (isInEditMode()) {
            return;
        }
        manager = Glide.with(context);
        mCircleTrans = new CircleTransform(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GlideImageView);
        try {
            placeholderId = a.getResourceId(R.styleable.GlideImageView_placeholderImage, placeholderId);
            failureImageId = a.getResourceId(R.styleable.GlideImageView_failureImage, failureImageId);
            roundAsCircle = a.getBoolean(R.styleable.GlideImageView_roundAsCircle, roundAsCircle);
            roundedCornerRadius = a.getDimensionPixelSize(R.styleable.GlideImageView_roundedCornerRadius,
                roundedCornerRadius);
            mBorderColor = a.getColor(R.styleable.GlideImageView_roundingBorderColor, mBorderColor);
            mBorderWidth = a.getDimensionPixelSize(R.styleable.GlideImageView_roundingBorderWidth, mBorderWidth);
        } finally {
            a.recycle();
        }
        mRoundTrans = new RoundTransform(context);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mBorderRect.set(0, 0, getWidth(), getHeight());
        if (roundAsCircle && mBorderWidth != 0) {
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, Math.min(
                    (mBorderRect.height() - mBorderWidth) / 2.0f, (mBorderRect.width() - mBorderWidth) / 2.0f),
                mBorderPaint);
        }
    }

    /**
     * Displays an image given by the url
     *
     * @param url url of the image
     */
    public void setImageURL(String url) {
        if (url == null) {
            return;
        }
        builder = (DrawableTypeRequest) manager.load(url).dontAnimate();
        setParams();
        builder.into(this);
    }

    private void setParams() {
        if (placeholderId > 0) {
            builder.placeholder(placeholderId);
        }
        if (failureImageId > 0) {
            builder.error(failureImageId);
        }
        if (roundedCornerRadius > 0) {
            builder.transform(mRoundTrans);
        }
        if (roundAsCircle) {
            builder.transform(mCircleTrans);
        }
        builder.animate(R.anim.slide_in_left);
    }

    //圆形
    public static class CircleTransform extends BitmapTransformation {
        public CircleTransform(Context context) {
            super(context);
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return circleCrop(pool, toTransform);
        }

        private static Bitmap circleCrop(BitmapPool pool, Bitmap source) {
            if (source == null) {
                return null;
            }

            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            // TODO this could be acquired from the pool too
            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override
        public String getId() {
            return getClass().getName();
        }
    }

    //圆角
    public class RoundTransform extends BitmapTransformation {

        private float radius = 0f;

        public RoundTransform(Context context) {
            this(context, roundedCornerRadius);
        }

        public RoundTransform(Context context, int dp) {
            super(context);
            this.radius = Resources.getSystem().getDisplayMetrics().density * dp;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            return roundCrop(pool, toTransform);
        }

        private Bitmap roundCrop(BitmapPool pool, Bitmap source) {
            if (source == null) {
                return null;
            }

            Bitmap result = pool.get(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            RectF rectF = new RectF(0f, 0f, source.getWidth(), source.getHeight());
            canvas.drawRoundRect(rectF, radius, radius, paint);
            return result;
        }

        @Override
        public String getId() {
            return getClass().getName();
        }
    }
}
