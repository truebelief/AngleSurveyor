package com.example.truebelief.anglesurveyor;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by truebelief on 2015/6/26.
 */
public class MainPanel extends RelativeLayout {

    private MainPanelView _mainPanelView;
    private View _slideMenuVw;
    private View _recordingMenuVw;
    private View _titleView;

    public MainPanel(Context context) {
        super(context);
    }

    public MainPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainPanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void addViews(View left, View center) {
        setSlideMenuView(left);
        setCenterView(center);
    }


    @SuppressWarnings("deprecation")
    public void setSlideMenuView(View view) {
        LayoutParams behindParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
        behindParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        addView(view, behindParams);
        this._slideMenuVw = view;
    }

    public void setRecordingMenuView(View view) {
        LayoutParams bottomParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//        bottomParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        bottomParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(view, bottomParams);
        this._recordingMenuVw = view;
    }

    /**
     *
     * @param view
     */
    @SuppressWarnings("deprecation")
    public void setCenterView(View view) {

        LayoutParams aboveParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        this._mainPanelView = new MainPanelView(getContext());
        this._mainPanelView.setView(view);
        addView(this._mainPanelView, aboveParams);
//        this._mainPanelView.setCenterPanelView(view);
//        this._mainPanelView.setRecordingView(this._recordingMenuVw);
        this._mainPanelView.setSlideMenuView(this._slideMenuVw);
        this._mainPanelView.invalidate();
    }
//    @SuppressWarnings("deprecation")
//    public void setHeadView(View view){
//        LayoutParams headParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//
//        headParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//
//        addView(view, headParams);
//        this._titleView=view;
////        this._mainPanelView.setCenterPanelView(view);
//
//
//    }


    public void showSlideMenuView() {
        this._mainPanelView.showSlideMenuView();
    }


}
