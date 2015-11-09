package com.example.truebelief.anglesurveyor;
/**
 * Created by truebelief on 2015/6/23.
 */
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class ExistingJobView extends TextView{
    private Paint marginPaint;
    private Paint linePaint;
    private int paperColor;
    private float margin;


//    @Override
//    public boolean onTouchEvent(MotionEvent event){
//        Log.v("FK","MLGB");
//        final boolean superResult = super.onTouchEvent(event);
//        return superResult;
//    }
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        int action = event.getAction() & MotionEvent.ACTION_MASK;
//        Log.d("CV", "Action ["+action+"]");
//        switch(action) {
//            case MotionEvent.ACTION_DOWN : {
//
//
//                break;
//            }
//            case MotionEvent.ACTION_MOVE : {
////                path.lineTo(event.getX(), event.getY());
//
//                break;
//            }
//
//        }
//
//        invalidate();
//        return true;
//    }


    public ExistingJobView (Context context,AttributeSet ats, int ds){
        super(context,ats,ds);
        init();
    }
    public ExistingJobView (Context context){
        super(context);
        init();
    }
    public ExistingJobView (Context context,AttributeSet ats){
        super(context,ats);
        init();
    }
    private void init(){
//        Log.v("t", "cnm!!1");
        Resources myResources=getResources();
        marginPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        marginPaint.setStrokeWidth(2);
        marginPaint.setColor(myResources.getColor(R.color.notepad_margin));
        linePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(myResources.getColor(R.color.notepad_lines));
        linePaint.setStrokeWidth(2);
        paperColor=myResources.getColor(R.color.notepad_paper);
        margin=myResources.getDimension(R.dimen.notepad_margin);
    }
    @Override
    public void onDraw(Canvas canvas){
//        Log.v("t", "cnm!!2");
        canvas.drawColor(paperColor);
        canvas.drawLine(0,0,0,getMeasuredHeight(),linePaint);
        canvas.drawLine(0,getMeasuredHeight(),getMeasuredWidth(),getMeasuredHeight(),linePaint);

        canvas.drawLine(margin,0,margin,getMeasuredHeight(),marginPaint);
        canvas.save();
        canvas.translate(margin,0);
        super.onDraw(canvas);
        canvas.restore();
    }
}
