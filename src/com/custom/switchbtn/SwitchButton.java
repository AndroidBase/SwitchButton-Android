package com.custom.switchbtn;

import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.CheckBox;

/**
 * Created by Leone on 2014/7/9.
 *
 * @author Leone
 * @version 1.0.0
 * @date 2014/7/9
 */
public class SwitchButton extends CheckBox {

    /** View的背景图片 **/
    private Bitmap mBtnBgImg;
    /** View的遮罩图片 **/
    private Bitmap mBtnMaskImg;
    /** View的框架图片 **/
    private Bitmap mBtnFrameImg;
    /** View的按钮按下图片 **/
    private Bitmap mBtnPressedImg;
    /** View的按钮非按下图片 **/
    private Bitmap mBtnUnPressedImg;
    /** 按钮当前图片 **/
    private Bitmap mBtnCurImg;
    /** 最大透明度 **/
    private final static int MAX_ALPHA = 255;
    /** 按钮的移动速率 **/
    private final static int VELOCITY_DPI = 350;
    /** y轴方向的偏移距离 **/
    private final static int EXTEND_OFFSET_Y_DPI = 15;
    /** 移动速率 **/
    private int mVelocity;
    /** y轴方向偏移距离 **/
    private int mExtendOffsetY;
    /** 按钮开启状态和关闭状态的位置以及真实位置 **/
    private float mBtnOnPos = 0, mBtnOffPos = 0, mBtnRealPos = 0;
    /** 按钮放置区域 **/
    private RectF mSaveLayerRectF;
    /**
     * 按钮的画笔，注意请在初始化View时将此画笔初始化，
     * 假如在onDraw中初始化由于onDraw方法被不断调用使得JVM不断GC，影响效率
     */
    private Paint mPaint;
    /** 设置两张图片相交时的模式 **/
    private PorterDuffXfermode mXferMode;
    /** 判断点击事件还是滑动事件的条件值 **/
    private int mTouchSlop, mClickTimeOut;
    /** 第一次点击位置 **/
    private float mFirstDownX, mFirstDownY;
    /** 是否开启开关 **/
    private boolean mTurningOn = false;
    /** 动画的开启和停止状态 **/
    private boolean mAnimationStatus = false;
    /** 动画执行的位置 **/
    private float mAnimationPostion = 0;
    /** 用于处理循环的Handler **/
    private Handler mLoopHandler = new Handler();

    /**
     * SwitchButton
     * @param context context
     */
    public SwitchButton(Context context) {
        super(context);
        initViews();
    }

    /**
     * SwitchButton
     * @param context context
     * @param attrs attrs
     */
    public SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    /**
     * SwitchButton
     * @param context context
     * @param attrs attrs
     * @param defStyle defStyle
     */
    public SwitchButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    private void initViews() {
        mBtnMaskImg = BitmapFactory.decodeResource(getResources(), R.drawable.mask);
        mBtnFrameImg = BitmapFactory.decodeResource(getResources(), R.drawable.frame);
        mBtnBgImg = BitmapFactory.decodeResource(getResources(), R.drawable.bottom_bg);
        mBtnPressedImg = BitmapFactory.decodeResource(getResources(), R.drawable.btn_pressed);
        mBtnUnPressedImg = BitmapFactory.decodeResource(getResources(), R.drawable.btn_unpressed);
        mBtnCurImg = mBtnUnPressedImg;

        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);

        mXferMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mClickTimeOut = ViewConfiguration.getLongPressTimeout() + ViewConfiguration.getTapTimeout();

        mBtnOffPos = mBtnPressedImg.getWidth() / 2;
        mBtnOnPos = mBtnMaskImg.getWidth() - mBtnOffPos;

        final float density = getResources().getDisplayMetrics().density;
        mVelocity = (int) (VELOCITY_DPI * density + 0.5f);
        mExtendOffsetY = (int) (EXTEND_OFFSET_Y_DPI * density + 0.5f);

        mSaveLayerRectF = new RectF(0, mExtendOffsetY, mBtnMaskImg.getWidth(), mBtnMaskImg.getHeight() + mExtendOffsetY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mBtnMaskImg.getWidth(), mBtnMaskImg.getHeight() + 2 * mExtendOffsetY);
    }

    /**
     * onDraw
     * @param canvas canvas
     */
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.saveLayerAlpha(mSaveLayerRectF, isEnabled() ? MAX_ALPHA : MAX_ALPHA / 2, Canvas.MATRIX_SAVE_FLAG
                | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
                | Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);

        // 绘制蒙板图片
        canvas.drawBitmap(mBtnMaskImg, 0, mExtendOffsetY, mPaint);
        mPaint.setXfermode(mXferMode);

        // 绘制背景图片
        canvas.drawBitmap(mBtnBgImg, mBtnRealPos, mExtendOffsetY, mPaint);
        mPaint.setXfermode(null);

        // 绘制框架图片
        canvas.drawBitmap(mBtnFrameImg, 0, mExtendOffsetY, mPaint);

        // 绘制按钮图片
        canvas.drawBitmap(mBtnCurImg, mBtnRealPos, mExtendOffsetY, mPaint);
        canvas.restore();
    }

    /**
     * onTouchEvent
     * @param event event
     * @return isEnable()
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                mFirstDownX = event.getX();
                mFirstDownY = event.getY();
                mBtnCurImg = mBtnPressedImg;
                break;
            case MotionEvent.ACTION_MOVE:
                float btnPos = (isChecked() ? mBtnOnPos : mBtnOffPos) + event.getX() - mFirstDownX;
                btnPos = btnPos <= mBtnOnPos ? mBtnOnPos : btnPos;
                btnPos = btnPos >= mBtnOffPos ? mBtnOffPos : btnPos;
                mTurningOn = btnPos > mBtnOffPos / 2;
                mBtnRealPos = btnPos - mBtnOffPos;
                break;
            case MotionEvent.ACTION_UP:
                mBtnCurImg = mBtnUnPressedImg;
                long timeOut = event.getEventTime() - event.getDownTime();
                if (Math.abs(event.getX() - mFirstDownX) < mTouchSlop && Math.abs(event.getY() - mFirstDownY) < mTouchSlop
                            && timeOut < mClickTimeOut) {
                    performClick();
                } else {
                    startSlideAnimation(!mTurningOn);
                }
                break;
            default:
                break;
        }

        invalidate();
        return isEnabled();
    }

    public void startSlideAnimation(boolean turningOn) {
        mAnimationStatus = true;
        mVelocity = turningOn ? -Math.abs(mVelocity) : Math.abs(mVelocity);
        mAnimationPostion = mBtnRealPos + mBtnOffPos;
        mLoopHandler.post(new SlidingAnimation());
    }

    public void stopSlideAnimation() {
        mAnimationStatus = false;
    }

    @Override
    public boolean performClick() {
        startSlideAnimation(!isChecked());
        return true;
    }

    private static final int SET_CHECKED_DELAY_TIMEMILLS = 10;
    public void setCheckedDelay(final boolean isChecked) {
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                setChecked(isChecked);
            }
        }, SET_CHECKED_DELAY_TIMEMILLS);
    }

    private class SlidingAnimation implements Runnable {
        final int ANIMATION_FRAME_DURATION = 1000 / 60;
        final int THOUSAND_VALUE = 1000;

        @Override
        public void run() {
            if (!mAnimationStatus) {
                mLoopHandler.removeCallbacks(this);
                return;
            }

            mAnimationPostion += mVelocity * ANIMATION_FRAME_DURATION / THOUSAND_VALUE;
            if (mAnimationPostion >= mBtnOffPos) {
                stopSlideAnimation();
                mAnimationPostion = mBtnOffPos;
                setCheckedDelay(false);
            } else if (mAnimationPostion <=  mBtnOnPos) {
                stopSlideAnimation();
                mAnimationPostion = mBtnOnPos;
                setCheckedDelay(true);
            }
            mBtnRealPos = mAnimationPostion - mBtnOffPos;
            invalidate();
            mLoopHandler.postDelayed(this, ANIMATION_FRAME_DURATION);
        }
    }

}
