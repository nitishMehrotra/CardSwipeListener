package com.paajinpaaji.swipeviewsample;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by nitishmehrotra from Paaji N' Paaji.
 */

public class CardSwipeListener implements View.OnTouchListener {
    // Cached ViewConfiguration and system-wide constant values
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;

    // Fixed properties
    private ViewGroup mViewGroup;
    private int mViewHeight = 1; // 1 and not 0 to prevent dividing by zero

    // Transient properties
    private float mDownY;
    private boolean mSwiping;
    private int mSwipingSlop;
    private VelocityTracker mVelocityTracker;
    private int mDownPosition;
    private static int INVALID_POSITION;
    private ArrayList<View> mSwipeView;

    public CardSwipeListener(ViewGroup viewGroup) {
        ViewConfiguration vc = ViewConfiguration.get(viewGroup.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        INVALID_POSITION = viewGroup.getChildCount();
        mViewGroup = viewGroup;
        mSwipeView = new ArrayList<>();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mViewHeight < 2) {
            mViewHeight = mViewGroup.getHeight();
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                // TODO: ensure this is a finger, and set a flag

                // Find the child view that was touched (perform a hit test)
                Rect rect = new Rect();
                int childCount = mViewGroup.getChildCount();
                int[] listViewCoords = new int[2];
                mViewGroup.getLocationOnScreen(listViewCoords);
                int x = (int) motionEvent.getRawX() - listViewCoords[0];
                int y = (int) motionEvent.getRawY() - listViewCoords[1];
                View child;
                for (int i = 0; i < childCount; i++) {
                    child = mViewGroup.getChildAt(i);
                    child.getHitRect(rect);
                    if (rect.contains(x, y)) {
                        mSwipeView.add(child);
                        mDownPosition = i;
                    }
                }

                if (mSwipeView != null) {
                    mDownY = motionEvent.getRawY();
                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(motionEvent);
                }
                return true;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (mVelocityTracker == null) {
                    break;
                }

                if (mSwipeView != null && mSwiping) {
                    // cancel
                    mSwipeView.get(mDownPosition)
                            .animate()
                            .translationY(0)
                            .setListener(null);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownY = 0;
                mDownPosition = ListView.INVALID_POSITION;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    break;
                }

                float deltaY = motionEvent.getRawY() - mDownY;
                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityY = mVelocityTracker.getYVelocity();
                float absVelocityX = Math.abs(mVelocityTracker.getYVelocity());
                float absVelocityY = Math.abs(velocityY);
                boolean dismiss = false;
                boolean swipeDown = false;
                if (Math.abs(deltaY) > mViewHeight / 3 && mSwiping) {
                    dismiss = true;
                    swipeDown = deltaY > 0;
                } else if (mMinFlingVelocity <= absVelocityY && absVelocityY <= mMaxFlingVelocity
                        && absVelocityX < absVelocityY && mSwiping) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = (velocityY < 0) == (deltaY < 0);
                    swipeDown = mVelocityTracker.getYVelocity() > 0;
                }
                final View downView;
                if (deltaY > 0) {
                    if ((mDownPosition + 1) < INVALID_POSITION) {
                        downView = mSwipeView.get(mDownPosition + 1);
                    } else {
                        return false;
                    }
                } else {
                    if (mDownPosition == 0) {
                        return false;
                    } else {
                        downView = mSwipeView.get(mDownPosition);
                    }
                }
                if (dismiss) {
                    // dismiss
                    downView.animate()
                            .translationY(swipeDown ? 0 : -mViewHeight)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                }
                            });
                } else {
                    // cancel
                    final int translate = swipeDown ? 0 : -mViewHeight;
                    downView.animate()
                            .translationY(translate)
                            .setListener(null);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownY = 0;
                mDownPosition = INVALID_POSITION;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null) {
                    break;
                }
                mVelocityTracker.addMovement(motionEvent);
                float deltaY = motionEvent.getRawY() - mDownY;
                if (Math.abs(deltaY) > mSlop) {
                    mSwiping = true;
                    mSwipingSlop = (deltaY > 0 ? mSlop : -mSlop);

                    mViewGroup.requestDisallowInterceptTouchEvent(true);

                    // Cancel ListView's touch (un-highlighting the item)
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex()
                                    << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mViewGroup.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }

                final View downView;
                if (deltaY > 0) {
                    if ((mDownPosition + 1) < INVALID_POSITION) {
                        downView = mSwipeView.get(mDownPosition + 1);
                    } else {
                        return false;
                    }
                } else {
                    if (mDownPosition == 0) {
                        return false;
                    } else {
                        downView = mSwipeView.get(mDownPosition);
                    }
                }
                if (mSwiping && deltaY > 0) {
                    downView.setTranslationY(-mViewHeight + deltaY - mSwipingSlop);
                    return true;
                } else if (mSwiping && deltaY < 0) {
                    downView.setTranslationY(deltaY - mSwipingSlop);
                    return true;
                }
                break;
            }
        }
        return false;
    }

}