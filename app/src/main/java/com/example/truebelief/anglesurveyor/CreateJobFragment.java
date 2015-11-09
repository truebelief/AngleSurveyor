package com.example.truebelief.anglesurveyor;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.PorterDuff;
import android.graphics.Xfermode;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.view.KeyEvent;
import android.widget.ImageButton;

/**
 * Created by truebelief on 2015/6/23.
 */
public class CreateJobFragment extends Fragment {
    private static boolean _editable;
//    private View _view;
    EditText myEditText;
    ImageButton myJobEdit;
    ImageButton myJobAdd;
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        _editable=false;
        View view=inflater.inflate(R.layout.new_job_fragment,container,false);

        myEditText=(EditText) view.findViewById(R.id.myEditText);
        myJobEdit=(ImageButton) view.findViewById(R.id.myJobEdit);
        myJobAdd=(ImageButton) view.findViewById(R.id.myJobAdd);
        final ImageButton myButtonCall=(ImageButton) view.findViewById(R.id.myButtonCall);

        myEditText.setOnKeyListener(new View.OnKeyListener(){
        public boolean onKey(View v,int keyCode, KeyEvent event){
        if (event.getAction()==KeyEvent.ACTION_DOWN){
            if ((keyCode==KeyEvent.KEYCODE_DPAD_CENTER) || (keyCode==KeyEvent.KEYCODE_ENTER)){
                myEditText.setEnabled(false);
                myJobEdit.setImageResource(R.mipmap.job_edit);
                _editable=true;
//                myEditText.setText("");
                return  true;
            }
        }
            return  false;
        }
        });


        myJobEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageButton view = (ImageButton) v;
                        view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.MULTIPLY);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        if (_editable) {
                            myEditText.setEnabled(true);
                            myJobEdit.setImageResource(R.mipmap.job_yes);
                            _editable = false;
                        } else {
                            myEditText.setEnabled(false);
                            myJobEdit.setImageResource(R.mipmap.job_edit);
                            _editable = true;
                        }

                    case MotionEvent.ACTION_CANCEL: {
                        ImageButton view = (ImageButton) v;
                        view.getDrawable().clearColorFilter();
                        view.invalidate();
                        break;
                    }
                }
                return true;
            }
        });



        myJobAdd.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageButton view = (ImageButton) v;
                        view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.MULTIPLY);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        String newJob = myEditText.getText().toString();
                        if (newJob.isEmpty()) {
                            return true;
                        }
                        ;
//                        onNewJobAddedListenerInstance.onNewJobAdded(newJob);
                        onCreateJobFragment.onNewJobAdded(newJob);
                    case MotionEvent.ACTION_CANCEL: {
                        ImageButton view = (ImageButton) v;
                        view.getDrawable().clearColorFilter();
                        view.invalidate();
                        break;
                    }
                }
                return true;
            }
        });


        myButtonCall.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageButton view = (ImageButton) v;
//                        view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                        view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.MULTIPLY);
                        view.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        // Your action here on button click
//                        onSlideListenerInstance.onSlide();
                        onCreateJobFragment.onSlide();
                    case MotionEvent.ACTION_CANCEL: {
                        ImageButton view = (ImageButton) v;
                        view.getDrawable().clearColorFilter();
                        view.invalidate();
                        break;
                    }
                }
                return true;
            }
        });

//        onCreateJobFragment.onEnableJobEdit(myEditText,myJobEdit,myJobAdd);

//        _view=view;
        return view;
    }

    public interface onCreateJobFragmentListener{
        public void onSlide();
        public void onNewJobAdded(String newItem);
//        public void onEnableJobEdit(EditText ed,ImageButton b1,ImageButton b2);
    };
//    public interface onNewJobAddedListener {
//        public void onNewJobAdded(String newItem);
//    }

//    private onNewJobAddedListener onNewJobAddedListenerInstance;
//    private onSlideListener onSlideListenerInstance;
    private onCreateJobFragmentListener onCreateJobFragment;


    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try {
//            onNewJobAddedListenerInstance = (onNewJobAddedListener)activity;
//            onSlideListenerInstance=(onSlideListener)activity;
            onCreateJobFragment=(onCreateJobFragmentListener) activity;

        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString()+"must implement onCreateJobFragmentListener");
        }
    }


}
