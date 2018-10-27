package com.zh.swipebacklib;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.FloatRange;
import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.zh.swipebacklib.tools.Util;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * Created by zh on 2018/10/21.
 */

public class SwipeBackLayout extends FrameLayout {
    private static final String TAG = "SwipeBackLayout";

    private Drawable mShadowLeft;
    private float mScrimOpacity = 1;
    private Rect mTmpRect = new Rect();
    private static final int FULL_ALPHA = 255;//左侧边缘阴影最大透明度
    private View mPreviousChild;
    private static final float FINALSCALE = 0.96f;
    private static final int ANIM_DURATION = 200;
    public boolean finishAnim = true;//Activity关闭底层动画
    private float mScale;//当前缩放比例
    private boolean mEnable = true;//是否允许滑动
    private Activity mTopActivity;//执行滑动时的最顶层Activity
    private WeakReference<Activity> mBackActivityWeakRf;//后面的Activity的弱引用
    public boolean mIsActivityTranslucent = true;//界面是否是透明的
    public boolean mIsActivitySwipeing = false;//界面是否正在滑动


    public static final int FROM_LEFT = 1 << 0;
    public static final int FROM_RIGHT = 1 << 1;
    public static final int FROM_TOP = 1 << 2;
    public static final int FROM_BOTTOM = 1 << 3;

    @IntDef({FROM_LEFT, FROM_TOP, FROM_RIGHT, FROM_BOTTOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DirectionMode {
    }

    private int mDirectionMode = FROM_LEFT;

    private final ViewDragHelper mDragHelper;
    private View mDragContentView;
    private View innerScrollView;

    private int width, height;

    private int mTouchSlop;
    private float swipeBackFactor = 0.5f;
    private float swipeBackFraction;//界面滑动进度
    private int maskAlpha = 180;//底层阴影初始透明度
    private boolean isSwipeFromEdge = false;
    private float downX, downY;

    private int leftOffset = 0;
    private int topOffset = 0;
    private float autoFinishedVelocityLimit = 2000f;

    private int touchedEdge = ViewDragHelper.INVALID_POINTER;

    public SwipeBackLayout(@NonNull Context context) {
        this(context, null);
    }

    public SwipeBackLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeBackLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mDragHelper = ViewDragHelper.create(this, 1f, new DragHelperCallback());
        mDragHelper.setEdgeTrackingEnabled(mDirectionMode);
        mTouchSlop = mDragHelper.getTouchSlop();
        setSwipeBackListener(defaultSwipeBackListener);

        init(context, attrs);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeBackLayout);
        setDirectionMode(a.getInt(R.styleable.SwipeBackLayout_directionMode, mDirectionMode));
        setSwipeBackFactor(a.getFloat(R.styleable.SwipeBackLayout_swipeBackFactor, swipeBackFactor));
        setMaskAlpha(a.getInteger(R.styleable.SwipeBackLayout_maskAlpha, maskAlpha));
        isSwipeFromEdge = a.getBoolean(R.styleable.SwipeBackLayout_isSwipeFromEdge, isSwipeFromEdge);
        setShadow(R.drawable.shadow_left);
        a.recycle();
    }

    public void attachToActivity(Activity activity) {
        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.windowBackground
        });
        int background = a.getResourceId(0, 0);
        a.recycle();
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decorView.getChildAt(0);
//        decorChild.setBackgroundColor(Color.TRANSPARENT);//这样设置的话布局必须设置颜色，不然会出现黑色
        decorChild.setBackgroundResource(background);
        decorView.removeView(decorChild);
        addView(decorChild);
        mDragContentView = decorChild;
        decorView.addView(this);

        mTopActivity = activity;
        Activity backActivity  = SlideFinishManager.getInstance().getPenultimateActivity();
        if (null != backActivity &&  backActivity instanceof BaseSwipeBackActivity) {
            mBackActivityWeakRf = new WeakReference<>(backActivity);
            SwipeBackLayout previousSlideLayout = ((BaseSwipeBackActivity) backActivity).getSwipeBackLayout();
            if(previousSlideLayout != null){
                mPreviousChild = previousSlideLayout.getChildAt(0);
            }
        }
    }

    /**
     * 绑定
     */
    public void bind() {
        Log.e("zh","bind  SlideFinishManager.mSlideFinishLayoutList.size:: " + SlideFinishManager.getInstance().mActivityStack.size());
        if(finishAnim && mPreviousChild != null){
            Log.e("zh","bind  mPreviousChild != null ");
            ObjectAnimator closeAnimatorX = ObjectAnimator.ofFloat(mPreviousChild, "scaleX", 1f, FINALSCALE);
            ObjectAnimator closeAnimatorY = ObjectAnimator.ofFloat(mPreviousChild, "scaleY", 1f, FINALSCALE);
            AnimatorSet animatorSet1 = new AnimatorSet();
            animatorSet1.playTogether(closeAnimatorX, closeAnimatorY);
            animatorSet1.setDuration(ANIM_DURATION).start();
        }
    }

    /**
     * 上层滑动关闭，底层恢复缩放动画
     */
    public void startFinishAnim() {
        Log.e("zh","startFinishAnim  SlideFinishManager.mSlideFinishLayoutList.size:: " + SlideFinishManager.getInstance().mActivityStack.size());
        if(finishAnim &&  mPreviousChild != null){
            ObjectAnimator closeAnimatorX = ObjectAnimator.ofFloat(mPreviousChild, "scaleX", FINALSCALE, 1f);
            ObjectAnimator closeAnimatorY = ObjectAnimator.ofFloat(mPreviousChild, "scaleY", FINALSCALE, 1f);
            AnimatorSet animatorSet1 = new AnimatorSet();
            animatorSet1.playTogether(closeAnimatorX, closeAnimatorY);
            animatorSet1.setDuration(ANIM_DURATION).start();
        }
    }


    public void setEnableGesture(boolean enable) {
        mEnable = enable;
    }

    public boolean getSwipeBackEnable() {
        return  mEnable;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        try {
            if (!getSwipeBackEnable()) {
                super.onLayout(changed, l, t, r, b);
                return;
            }
            int left = getPaddingLeft() + leftOffset;
            int top = getPaddingTop() + topOffset;
            int right = left + mDragContentView.getMeasuredWidth();
            int bottom = top + mDragContentView.getMeasuredHeight();
            mDragContentView.layout(left, top, right, bottom);

            if (changed) {
                width = getWidth();
                height = getHeight();
            }
            innerScrollView = Util.findAllScrollViews(this);
        } catch (Exception e) {
            super.onLayout(changed, l, t, r, b);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getSwipeBackEnable()){
            //绘制底层阴影
            canvas.drawARGB(maskAlpha - (int) (maskAlpha * swipeBackFraction), 0, 0, 0);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!getSwipeBackEnable()) {
            return false;
        }
        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getRawX();
                downY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (innerScrollView != null && Util.contains(innerScrollView, downX, downY)) {
                    float distanceX = Math.abs(ev.getRawX() - downX);
                    float distanceY = Math.abs(ev.getRawY() - downY);
                    if (mDirectionMode == FROM_LEFT || mDirectionMode == FROM_RIGHT) {
                        if (distanceY > mTouchSlop && distanceY > distanceX) {
                            return super.onInterceptTouchEvent(ev);
                        }
                    } else if (mDirectionMode == FROM_TOP || mDirectionMode == FROM_BOTTOM) {
                        if (distanceX > mTouchSlop && distanceX > distanceY) {
                            return super.onInterceptTouchEvent(ev);
                        }
                    }
                }
                break;
            default:
                break;
        }
        boolean handled = mDragHelper.shouldInterceptTouchEvent(ev);
        return handled ? handled : super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!getSwipeBackEnable()) {
            return false;
        }
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void smoothScrollToX(int finalLeft) {
        if (mDragHelper.settleCapturedViewAt(finalLeft, getPaddingTop())) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void smoothScrollToY(int finalTop) {
        if (mDragHelper.settleCapturedViewAt(getPaddingLeft(), finalTop)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            if(getSwipeBackEnable() ){
                mIsActivitySwipeing = true;
                if (null != mTopActivity && !mIsActivityTranslucent) {
                    Util.convertActivityToTranslucent(mTopActivity);
                    mIsActivityTranslucent = true;
                }
                return child == mDragContentView;
            }
            return false;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            leftOffset = getPaddingLeft();
            if (isSwipeEnabled()) {
                if (mDirectionMode == FROM_LEFT && !Util.canViewScrollRight(innerScrollView, downX, downY, false)) {
                    leftOffset = Math.min(Math.max(left, getPaddingLeft()), width);
                } else if (mDirectionMode == FROM_RIGHT && !Util.canViewScrollLeft(innerScrollView, downX, downY, false)) {
                    leftOffset = Math.min(Math.max(left, -width), getPaddingRight());
                }
            }
            return leftOffset;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            topOffset = getPaddingTop();
            if (isSwipeEnabled()) {
                if (mDirectionMode == FROM_TOP && !Util.canViewScrollUp(innerScrollView, downX, downY, false)) {
                    topOffset = Math.min(Math.max(top, getPaddingTop()), height);
                } else if (mDirectionMode == FROM_BOTTOM && !Util.canViewScrollDown(innerScrollView, downX, downY, false)) {
                    topOffset = Math.min(Math.max(top, -height), getPaddingBottom());
                }
            }
            return topOffset;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            left = Math.abs(left);
            top = Math.abs(top);
            switch (mDirectionMode) {
                case FROM_LEFT:
                case FROM_RIGHT:
                    swipeBackFraction = 1.0f * left / width;
                    break;
                case FROM_TOP:
                case FROM_BOTTOM:
                    swipeBackFraction = 1.0f * top / height;
                    break;
                default:
                    break;
            }
            if (mSwipeBackListener != null) {
                mSwipeBackListener.onViewPositionChanged(mDragContentView, swipeBackFraction, swipeBackFactor);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            leftOffset = topOffset = 0;
            if (!isSwipeEnabled()) {
                touchedEdge = ViewDragHelper.INVALID_POINTER;
                return;
            }
            touchedEdge = ViewDragHelper.INVALID_POINTER;

            boolean isBackToEnd = backJudgeBySpeed(xvel, yvel) || swipeBackFraction >= swipeBackFactor;
            if (isBackToEnd) {
                switch (mDirectionMode) {
                    case FROM_LEFT:
                        //滑动关闭
                        smoothScrollToX(width);
                        break;
                    case FROM_TOP:
                        smoothScrollToY(height);
                        break;
                    case FROM_RIGHT:
                        smoothScrollToX(-width);
                        break;
                    case FROM_BOTTOM:
                        smoothScrollToY(-height);
                        break;
                    default:
                        break;
                }
            } else {
                switch (mDirectionMode) {
                    case FROM_LEFT:
                    case FROM_RIGHT:
                        //滑动返回
                        smoothScrollToX(getPaddingLeft());
                        break;
                    case FROM_BOTTOM:
                    case FROM_TOP:
                        smoothScrollToY(getPaddingTop());
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            if (state == ViewDragHelper.STATE_IDLE) {
                if (mSwipeBackListener != null) {
                    if (swipeBackFraction == 0) {
                        mSwipeBackListener.onViewSwipeFinished(mDragContentView, false);
                    } else if (swipeBackFraction == 1) {
                        mSwipeBackListener.onViewSwipeFinished(mDragContentView, true);
                    }
                }
            }
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return width;
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return height;
        }

        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
            super.onEdgeTouched(edgeFlags, pointerId);
            //边缘Touch状态 开始滑动
            touchedEdge = edgeFlags;
        }
    }

    private boolean isSwipeEnabled() {
        if (isSwipeFromEdge) {
            switch (mDirectionMode) {
                case FROM_LEFT:
                    return touchedEdge == ViewDragHelper.EDGE_LEFT;
                case FROM_TOP:
                    return touchedEdge == ViewDragHelper.EDGE_TOP;
                case FROM_RIGHT:
                    return touchedEdge == ViewDragHelper.EDGE_RIGHT;
                case FROM_BOTTOM:
                    return touchedEdge == ViewDragHelper.EDGE_BOTTOM;
                default:
                    break;
            }
        }
        return true;
    }

    private boolean backJudgeBySpeed(float xvel, float yvel) {
        switch (mDirectionMode) {
            case FROM_LEFT:
                return xvel > autoFinishedVelocityLimit;
            case FROM_TOP:
                return yvel > autoFinishedVelocityLimit;
            case FROM_RIGHT:
                return xvel < -autoFinishedVelocityLimit;
            case FROM_BOTTOM:
                return yvel < -autoFinishedVelocityLimit;
            default:
                break;
        }
        return false;
    }

    public void setSwipeBackFactor(@FloatRange(from = 0.0f, to = 1.0f) float swipeBackFactor) {
        if (swipeBackFactor > 1) {
            swipeBackFactor = 1;
        } else if (swipeBackFactor < 0) {
            swipeBackFactor = 0;
        }
        this.swipeBackFactor = swipeBackFactor;
    }

    public float getSwipeBackFactor() {
        return swipeBackFactor;
    }

    public void setMaskAlpha(@IntRange(from = 0, to = 255) int maskAlpha) {
        if (maskAlpha > 255) {
            maskAlpha = 255;
        } else if (maskAlpha < 0) {
            maskAlpha = 0;
        }
        this.maskAlpha = maskAlpha;
    }

    public int getMaskAlpha() {
        return maskAlpha;
    }

    public void setDirectionMode(@DirectionMode int direction) {
        mDirectionMode = direction;
        mDragHelper.setEdgeTrackingEnabled(direction);
    }

    public int getDirectionMode() {
        return mDirectionMode;
    }

    public float getAutoFinishedVelocityLimit() {
        return autoFinishedVelocityLimit;
    }

    public void setAutoFinishedVelocityLimit(float autoFinishedVelocityLimit) {
        this.autoFinishedVelocityLimit = autoFinishedVelocityLimit;
    }

    public boolean isSwipeFromEdge() {
        return isSwipeFromEdge;
    }

    public void setSwipeFromEdge(boolean isSwipeFromEdge) {
        this.isSwipeFromEdge = isSwipeFromEdge;
    }

    private OnSwipeBackListener mSwipeBackListener;

    private OnSwipeBackListener defaultSwipeBackListener = new OnSwipeBackListener() {
        @Override
        public void onViewPositionChanged(View mView, float swipeBackFraction, float swipeBackFactor) {
            invalidate();
            //滑动中改变缩放度
            mScale = FINALSCALE + (1 - FINALSCALE) * swipeBackFraction;
            if (mPreviousChild != null) {
                mPreviousChild.setScaleX(mScale);
                mPreviousChild.setScaleY(mScale);
            }
        }

        @Override
        public void onViewSwipeFinished(View mView, boolean isEnd) {
            if (isEnd) {
                //上层界面滑动消失，finish掉该界面
                finish();
            }else{
                //上层界面恢复滑动
                mIsActivitySwipeing = false;
                if (null != mTopActivity && mIsActivityTranslucent) {
                    Util.convertActivityFromTranslucent(mTopActivity);
                    mIsActivityTranslucent = false;
                }
            }
        }
    };

    public void finish() {
        finishAnim = false;
        ((Activity) getContext()).finish();
        //去除默认动画，防止关闭时出现阴影
        ((Activity) getContext()).overridePendingTransition(0, 0);
    }

    public void setSwipeBackListener(OnSwipeBackListener mSwipeBackListener) {
        this.mSwipeBackListener = mSwipeBackListener;
    }

    public interface OnSwipeBackListener {

        void onViewPositionChanged(View mView, float swipeBackFraction, float swipeBackFactor);

        void onViewSwipeFinished(View mView, boolean isEnd);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        if(!getSwipeBackEnable()){
            return super.drawChild(canvas, child, drawingTime);
        }
        final boolean drawContent = child == mDragContentView;
        boolean ret = super.drawChild(canvas, child, drawingTime);
        if (mScrimOpacity > 0 && drawContent
                && mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
            drawShadow(canvas, child);
        }
        return ret;
    }

    /**
     * 设置左侧边缘阴影图片
     */
    public void setShadow(int resId) {
        Drawable shadow  = getResources().getDrawable(resId);
        if (mDirectionMode == FROM_LEFT) {
            mShadowLeft = shadow;
        }
        invalidate();
    }

    /**
     * 左侧边缘阴影
     * @param canvas
     * @param child
     */
    private void drawShadow(Canvas canvas, View child) {
        final Rect childRect = mTmpRect;
        child.getHitRect(childRect);
        if (mDirectionMode == FROM_LEFT) {
            mShadowLeft.setBounds(childRect.left - mShadowLeft.getIntrinsicWidth(), childRect.top,
                    childRect.left, childRect.bottom);
            mShadowLeft.setAlpha((int) ( (1 - swipeBackFraction) * FULL_ALPHA));
            mShadowLeft.draw(canvas);
        }
    }
}
