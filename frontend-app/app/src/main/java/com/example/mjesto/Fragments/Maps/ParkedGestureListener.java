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

public class ParkedGestureListener extends GestureDetector.SimpleOnGestureListener implements
        View.OnTouchListener,
        DynamicAnimation.OnAnimationUpdateListener {

    private static final String TAG = ParkedGestureListener.class.getSimpleName();
    private static final float CLICK_FLEXIBILITY = 100;

    private FrameLayout mMapsFragmentView;
    private View mCurView;

    private float mWindowSize;
    private float mStartY;
    private float mDifference;
    private VelocityTracker mVelocityTracker;

    ParkedGestureListener(View view) {
        mMapsFragmentView = (FrameLayout) view;
        mVelocityTracker = null;
        mWindowSize = mMapsFragmentView.getHeight();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        mCurView = v;

        int index = event.getActionIndex();
        int pointerID = event.getPointerId(index);

        int action = event.getAction();

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                Log.d(TAG,"Action was DOWN");
                mStartY = mCurView.getY();
                mDifference = event.getRawY() - mCurView.getY();
                if (mWindowSize <= 0) {
                    mWindowSize = mMapsFragmentView.getHeight();

                }
                if (mVelocityTracker == null) {
                    mVelocityTracker = VelocityTracker.obtain();
                } else {
                    mVelocityTracker.clear();
                }

                return true;
            case (MotionEvent.ACTION_MOVE) :

                Log.d(TAG,"Action was MOVE, coords: current: " + mCurView.getY() + ", raw: " + event.getRawY() + ", window: " + mMapsFragmentView.getHeight());

                if (event.getRawY() > 1400 && event.getRawY() <= mWindowSize) {
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(mMapsFragmentView.getWidth(), (int) (event.getRawY()));
                    mMapsFragmentView.setLayoutParams(lp);

                    event.setLocation(event.getX(), event.getRawY());
                    mVelocityTracker.addMovement(event);
                    mVelocityTracker.computeCurrentVelocity(1000);
                    Log.d(TAG, "Velocity: " + mVelocityTracker.getYVelocity(pointerID));
                }

                return true;
            case (MotionEvent.ACTION_UP) :
                Log.d(TAG,"Action was UP");
                if (Math.abs(mStartY - (event.getRawY() - mDifference)) < CLICK_FLEXIBILITY) {
                    return false;
                }

                FlingAnimation fling = new FlingAnimation(mCurView, DynamicAnimation.Y);
                Log.d(TAG, "Velocity: " + mVelocityTracker.getYVelocity(pointerID));
                Log.d(TAG, "Window Size: " + mWindowSize);
                fling.setStartVelocity(mVelocityTracker.getYVelocity(pointerID))
                        .setFriction(1.1f)
                        .setMinValue(1400 - mDifference)
                        .setMaxValue(mWindowSize - mDifference)
                        .addUpdateListener(this)
                        .start();

                return true;
            case (MotionEvent.ACTION_CANCEL) :
//                v.setY(mStartY);
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                Log.d(TAG,"Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onAnimationUpdate(DynamicAnimation dynamicAnimation, float v, float v1) {
        Log.d(TAG, "animation update: " + v + " " + v1);
        LinearLayout.LayoutParams curP = (LinearLayout.LayoutParams) mMapsFragmentView.getLayoutParams();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(curP.width, (int) (mCurView.getY() + mDifference));
        mMapsFragmentView.setLayoutParams(lp);

    }
}
