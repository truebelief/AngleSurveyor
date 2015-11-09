package com.example.truebelief.anglesurveyor;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Created by truebelief on 2015/6/26.
 */
public class MainPanelView extends ViewGroup {
    private LinearLayout _mainPanelLayout;
    private Scroller _mainPanelScroller;

    private int _touchSlop;
    private float _lastMotionX;
    private float _lastMotionY;

    private VelocityTracker _velocityTracker;

    private static final int SNAP_VELOCITY = 1000;

    private View _slideMenuView;
    private View _recordingView;
    private View _centerPanelView;

    public MainPanelView(Context context) {
        super(context);
        init();
    }

    public MainPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MainPanelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this._mainPanelLayout.measure(widthMeasureSpec, heightMeasureSpec);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;
        this._mainPanelLayout.layout(0, 0, width, height);
    }

    private void init() {

        this._mainPanelLayout=new LinearLayout(getContext());
//        this._mainPanelLayout.setBackgroundColor(0xff000000);
        this._mainPanelScroller = new Scroller(getContext());
        this._touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        super.addView(this._mainPanelLayout);
    }
    public void setView(View v) {
        if (this._mainPanelLayout.getChildCount() > 0) {
            this._mainPanelLayout.removeAllViews();
        }
        this._mainPanelLayout.addView(v);
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        postInvalidate();
    }
    @Override
    public void computeScroll() {
        if (!this._mainPanelScroller.isFinished()) {
            if (this._mainPanelScroller.computeScrollOffset()) {
                int oldX = getScrollX();
                int oldY = getScrollY();
                int x = this._mainPanelScroller.getCurrX();
                int y = this._mainPanelScroller.getCurrY();
                if (oldX != x || oldY != y) {
                    scrollTo(x, y);
                }
                // Keep on drawing until the animation has finished.
                invalidate();
            } else {
                clearChildrenCache();
            }
        } else {
            clearChildrenCache();
        }
    }

    private boolean mIsBeingDragged;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                this._lastMotionX = x;
                this._lastMotionY = y;
                this.mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE:
                final float dx = x - this._lastMotionX;
                final float xDiff = Math.abs(dx);
                final float yDiff = Math.abs(y - this._lastMotionY);
                if (xDiff > this._touchSlop && xDiff > yDiff) {
                    this.mIsBeingDragged = true;
                    this._lastMotionX = x;
                }
                break;

        }
        return this.mIsBeingDragged;
    }

//    @Override
//    public boolean (View v) {
//        Log.v("CMN","CSNM");
//        return false;
//    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
//        Log.d("SB","SBSB");
        if (this._velocityTracker == null) {
            this._velocityTracker = VelocityTracker.obtain();
        }
        this._velocityTracker.addMovement(ev);

        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!this._mainPanelScroller.isFinished()) {
                    this._mainPanelScroller.abortAnimation();
                }
                this._lastMotionX = x;
                this._lastMotionY = y;
                if (getScrollX() == -getSlideMenuWidth() && this._lastMotionX < getSlideMenuWidth()) {
                    return false;
                }
                if (getScrollX() == getCenterPanelWidth() && this._lastMotionX > getSlideMenuWidth()) {
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsBeingDragged) {
                    enableChildrenCache();
                    final float deltaX = this._lastMotionX - x;
                    this._lastMotionX = x;
                    float oldScrollX = getScrollX();
                    float scrollX = oldScrollX + deltaX;

                    if (deltaX < 0 && oldScrollX < 0) { // left view
                        final float leftBound = 0;
                        final float rightBound = -getSlideMenuWidth();
                        if (scrollX > leftBound) {
                            scrollX = leftBound;
                        } else if (scrollX < rightBound) {
                            scrollX = rightBound;
                        }
                    }
                    else if (deltaX > 0 && oldScrollX > 0) { // right view
                        final float rightBound = getCenterPanelWidth();
                        final float leftBound = 0;
                        if (scrollX < leftBound) {
                            scrollX = leftBound;
                        } else if (scrollX > rightBound) {
                            scrollX = rightBound;
                        }
                    }

                    scrollTo((int) scrollX, getScrollY());
                }
                break;
            case MotionEvent.ACTION_CANCEL:

            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
//                    final VelocityTracker velocityTracker = this._velocityTracker;
//                    velocityTracker.computeCurrentVelocity(10000);
//                    int velocityX = (int) velocityTracker.getXVelocity();
////                    velocityX = 0;
//                    Log.e("ad", "velocityX == " + velocityX);
                    int oldScrollX = getScrollX();
                    int dx = 0;
                    if (oldScrollX < 0) {
                        if (oldScrollX < -getSlideMenuWidth() / 2) {
                            dx = -getSlideMenuWidth() - oldScrollX;
                        } else if (oldScrollX >= -getSlideMenuWidth() / 2) {
                            dx = -oldScrollX;
                        }
                    }else{
                        dx = - oldScrollX;
                    }
                    smoothScrollTo(dx);
                    clearChildrenCache();
                }
                break;
        }
        if (this._velocityTracker != null) {
            this._velocityTracker.recycle();
            this._velocityTracker = null;
        }

        return false;
    }

//    public boolean onClick(){
//
//}

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void showSlideMenuView() {
        int menuWidth = this._slideMenuView.getWidth();
        int oldScrollX = getScrollX();
        if (oldScrollX >= 0) {
            smoothScrollTo(-menuWidth-oldScrollX);
        } else {
            smoothScrollTo(-oldScrollX);
        }
    }
//    public void shutSlideMenuView() {
//        int menuWidth = this._slideMenuView.getWidth();
//        int oldScrollX = getScrollX();
//        if (oldScrollX == 0) {
//            smoothScrollTo(menuWidth);
//        } else if (oldScrollX == -menuWidth) {
//            smoothScrollTo(menuWidth);
//        }
//    }
    void smoothScrollTo(int dx) {
        int duration = 500;
        int oldScrollX = getScrollX();
        this._mainPanelScroller.startScroll(oldScrollX, getScrollY(), dx, getScrollY(),
                duration);
        invalidate();
    }
    public View getSlideMenuView() {
        return this._slideMenuView;
    }
    public View get_recordingView() {
        return this._recordingView;
    }


    public void setSlideMenuView(View SlideMenuView) {
        this._slideMenuView=SlideMenuView;
    }
    public void setRecordingView(View RecordingView) {
        this._recordingView=RecordingView;
    }


    public void setHeadView(View headView){

    }
    public View getCenterPanelView() {
        return this._centerPanelView;
    }
    public void setCenterPanelView(View CenterPanelView) {
        this._centerPanelView=CenterPanelView;
    }

    private int getSlideMenuWidth() {
        if (this._slideMenuView == null) {
            return 0;
        }
        return this._slideMenuView.getWidth();
    }
    private int getRecordingViewWidth() {
        if (this._recordingView == null) {
            return 0;
        }
        return this._recordingView.getWidth();
    }
    private int getCenterPanelWidth() {
        if (this._centerPanelView == null) {
            return 0;
        }
        return this._centerPanelView.getWidth();
    }

    void enableChildrenCache() {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View layout = (View) getChildAt(i);
            layout.setDrawingCacheEnabled(true);
        }
    }

    void clearChildrenCache() {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View layout = (View) getChildAt(i);
            layout.setDrawingCacheEnabled(false);
        }
    }

}
