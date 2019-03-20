package com.example.mjesto.Fragments.Maps;

import android.content.Context;
import android.support.animation.DynamicAnimation;
import android.support.animation.FlingAnimation;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.example.mjesto.R;

public class ParkedGestureListener extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {

    private static final String TAG = ParkedGestureListener.class.getSimpleName();
    private static final float CLICK_FLEXIBILITY = 100;

    private FrameLayout mMapsFragmentView;

    private float mWindowSize;
    private float mStartY;
    private float mDifference;
    private VelocityTracker mVelocityTracker;

    ParkedGestureListener(View view) {
        mMapsFragmentView = (FrameLayout) view;
        mVelocityTracker = null;
        mWindowSize = 0;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int index = event.getActionIndex();
        int pointerID = event.getPointerId(index);

        int action = event.getAction();

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                Log.d(TAG,"Action was DOWN");
                mStartY = v.getY();
                mDifference = event.getRawY() - v.getY();
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }

                return true;
            case (MotionEvent.ACTION_MOVE) :

                Log.d(TAG,"Action was MOVE, coords: current: " + v.getY() + ", raw: " + event.getRawY());

                v.setY(event.getRawY() - mDifference);

                LinearLayout.LayoutParams curP = (LinearLayout.LayoutParams) mMapsFragmentView.getLayoutParams();
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(curP.width, (int) (v.getY() + mDifference));
                mMapsFragmentView.setLayoutParams(lp);

                event.setLocation(event.getX(), v.getY());
                mVelocityTracker.addMovement(event);
                mVelocityTracker.computeCurrentVelocity(1000);
                Log.d(TAG, "Velocity: " + mVelocityTracker.getYVelocity(pointerID));

                return true;
            case (MotionEvent.ACTION_UP) :
                Log.d(TAG,"Action was UP");
                if (Math.abs(mStartY - (event.getRawY() - mDifference)) < CLICK_FLEXIBILITY) {
                    return false;
                }
                FlingAnimation fling = new FlingAnimation(v, DynamicAnimation.TRANSLATION_Y);
                Log.d(TAG, "Velocity: " + mVelocityTracker.getYVelocity(pointerID));
                fling.setStartVelocity(mVelocityTracker.getYVelocity(pointerID))
                        .setFriction(1.1f)
                        .start();


                return true;
            case (MotionEvent.ACTION_CANCEL) :
                v.setY(mStartY);
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                Log.d(TAG,"Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            default:
                return false;
        }
    }
}
