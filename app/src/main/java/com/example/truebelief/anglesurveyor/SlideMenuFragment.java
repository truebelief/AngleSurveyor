package com.example.truebelief.anglesurveyor;


import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Date;


public class SlideMenuFragment extends Fragment {
    private MyTouchListener myTouchListenerInstance;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.slide_menu, null);
        LinearLayout slideMenuLayout = (LinearLayout) view.findViewById(R.id.slideMenu);
//        Log.v("fk", "mlgb!");
        int layoutCount = slideMenuLayout.getChildCount();
//        Log.v("fk", String.valueOf(layoutCount));
        for (int i = 0; i < layoutCount; i++) {
            LinearLayout ly=(LinearLayout)slideMenuLayout.getChildAt(i);
            myTouchListenerInstance = new MyTouchListener(getResources().getResourceName(ly.getId()));
            ly.setOnTouchListener(myTouchListenerInstance);
        }
        return view;
    }

    public interface FragmentSwitchListener {
        public void FragmentSwitch(String fragmentName);
    }

    private FragmentSwitchListener fragmentSwitch;
    public class MyTouchListener implements View.OnTouchListener {
        private String _slideItemName;
        public MyTouchListener(String slide_item_name) {
            _slideItemName = slide_item_name;
        }
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    LinearLayout view = (LinearLayout) v;
                    ImageView iv = (ImageView) view.getChildAt(0);
                    iv.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.MULTIPLY);
                    TextView tv = (TextView) view.getChildAt(1);
                    tv.setTextColor(Color.LTGRAY);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:
                    // Your action here on button click
//                    Toast.makeText((getActivity()),"Slide name is :"+_slideItemName,Toast.LENGTH_SHORT).show();
                    String slide_name=_slideItemName.substring(_slideItemName.indexOf("_")+1);
                    fragmentSwitch.FragmentSwitch(slide_name);

                case MotionEvent.ACTION_CANCEL: {
                    LinearLayout view = (LinearLayout) v;
                    ImageView iv = (ImageView) view.getChildAt(0);
                    iv.getDrawable().clearColorFilter();
                    TextView tv = (TextView) view.getChildAt(1);
                    tv.setTextColor(Color.BLACK);
                    view.invalidate();
                    break;
                }
            }
            return true;
        }
    }
    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try {
            fragmentSwitch=(FragmentSwitchListener) activity;

        }catch (ClassCastException e){
            throw new ClassCastException(activity.toString()+"must implement FragmentSwitchListener");
        }
    }

}
