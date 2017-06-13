/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.g2mobility.xbee.recycler;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.g2mobility.xbee.R;

/**
 * A {@link android.view.View.OnTouchListener} that makes the list items in a {@link
 * android.widget.ListView} dismissable. {@link android.widget.ListView} is given special treatment
 * because by default it handles touches for its list items... i.e. it's in charge of drawing the
 * pressed state (the list selector), handling list item clicks, etc. <p/> <p>Example usage:</p>
 * <p/>
 * <pre>
 * SwipeDismissRecyclerViewTouchListener touchListener =
 *         new SwipeDismissRecyclerViewTouchListener(
 *                 listView,
 *                 new SwipeDismissRecyclerViewTouchListener.OnDismissCallback() {
 *                     public void onDismiss(ListView listView, int[] reverseSortedPositions) {
 *                         for (int position : reverseSortedPositions) {
 *                             adapter.remove(adapter.getItem(position));
 *                         }
 *                         adapter.notifyDataSetChanged();
 *                     }
 *                 });
 * listView.setOnTouchListener(touchListener);
 * listView.setOnScrollListener(touchListener.makeScrollListener());
 * </pre>
 * <p/> <p>This class Requires API level 12 or later due to use of {@link
 * android.view.ViewPropertyAnimator}.</p>
 */
public class SwipeDeleteTouchListener implements View.OnTouchListener {
    // Cached ViewConfiguration and system-wide constant values
    private int mSlop;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;
    private long mAnimationTime;

    // Fixed properties
    private View mItemView;
    private DismissCallbacks mCallbacks;
    private View.OnClickListener mOnClickListener;
    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

    // Transient properties
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private int mSwipingSlop;
    private VelocityTracker mVelocityTracker;
    private View mDownView;
    private boolean mPaused;

    /**
     * The callback interface used by {@link com.g2mobility.xbee.recycler.SwipeDeleteTouchListener}
     * to inform its client about a successful dismissal of one or more list item positions.
     */
    public interface DismissCallbacks {
        /**
         * Called to determine whether the given position can be dismissed.
         */
        boolean canDismiss();

        /**
         * Called when the user has indicated they she would like to dismiss one or more list item
         * positions.
         */
        void onDismiss(View view);
    }

    /**
     * Constructs a new swipe-to-dismiss touch listener for the given list view.
     *
     * @param itemView  The list view whose items should be dismissable.
     * @param callbacks The callback to trigger when the user has indicated that she would like to
     *                  dismiss one or more list items.
     */
    public SwipeDeleteTouchListener(View itemView, DismissCallbacks callbacks,
            View.OnClickListener onClickListener) {
        ViewConfiguration vc = ViewConfiguration.get(itemView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = itemView.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        mItemView = itemView;
        mCallbacks = callbacks;
        mOnClickListener = onClickListener;
    }

    /**
     * Enables or disables (pauses or resumes) watching for swipe-to-dismiss gestures.
     *
     * @param enabled Whether or not to watch for gestures.
     */
    public void setEnabled(boolean enabled) {
        mPaused = !enabled;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (mViewWidth < 2) {
            mViewWidth = mItemView.getWidth();
        }

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                if (mPaused) {
                    return false;
                }

                // TODO: ensure this is a finger, and set a flag

                mDownView = (View) view.getParent();
                mDownX = motionEvent.getRawX();
                mDownY = motionEvent.getRawY();
                if (mCallbacks.canDismiss()) {
                    mVelocityTracker = VelocityTracker.obtain();
                    mVelocityTracker.addMovement(motionEvent);
                } else {
                    mDownView = null;
                }
                return false;
            }

            case MotionEvent.ACTION_CANCEL: {
                if (mVelocityTracker == null) {
                    break;
                }

                if (mDownView != null && mSwiping) {
                    // cancel
                    mDownView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mVelocityTracker == null) {
                    break;
                }

                float deltaX = motionEvent.getRawX() - mDownX;
                mVelocityTracker.addMovement(motionEvent);
                mVelocityTracker.computeCurrentVelocity(1000);
                float velocityX = mVelocityTracker.getXVelocity();
                final float absVelocityX = Math.abs(velocityX);
                float absVelocityY = Math.abs(mVelocityTracker.getYVelocity());
                boolean dismiss = false;
                boolean dismissRight = false;
                if (Math.abs(deltaX) > mViewWidth / 2 && mSwiping) {
                    dismiss = true;
                    dismissRight = deltaX > 0;
                } else if (mMinFlingVelocity <= absVelocityX && absVelocityX <= mMaxFlingVelocity
                        && absVelocityY < absVelocityX && mSwiping) {
                    // dismiss only if flinging in the same direction as dragging
                    dismiss = (velocityX < 0) == (deltaX < 0);
                    dismissRight = mVelocityTracker.getXVelocity() > 0;
                }
                if (dismiss) {
                    // dismiss
                    final View downView = mDownView;
                    downView.animate()
                            .translationX(dismissRight ? mViewWidth : -mViewWidth)
                            .alpha(0)
                            .setDuration(mAnimationTime)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    performDismiss(downView);
                                }
                            });
                } else {
                    // cancel
                    final View downView = mDownView;
                    downView.animate()
                            .translationX(0)
                            .alpha(1)
                            .setDuration(mAnimationTime)
                            .setListener(null);
                    ValueAnimator animator = ObjectAnimator.ofInt(((ColorDrawable) downView
                            .getBackground()).getColor(), mDownView.getResources().getColor(R
                            .color.background));
                    animator.setEvaluator(new ArgbEvaluator());
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            downView.setBackgroundColor((Integer) animation.getAnimatedValue());
                        }
                    });
                    animator.setDuration(mAnimationTime);
                    animator.start();
                }
                if (!mSwiping) {
                    mOnClickListener.onClick(view);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mDownX = 0;
                mDownY = 0;
                mDownView = null;
                mSwiping = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (mVelocityTracker == null || mPaused) {
                    break;
                }

                mVelocityTracker.addMovement(motionEvent);
                float deltaX = motionEvent.getRawX() - mDownX;
                float deltaY = motionEvent.getRawY() - mDownY;
                if (Math.abs(deltaX) > mSlop && Math.abs(deltaY) < Math.abs(deltaX) / 2) {
                    mSwiping = true;
                    mSwipingSlop = (deltaX > 0 ? mSlop : -mSlop);
                    ((ViewGroup) mItemView).requestDisallowInterceptTouchEvent(true);

                    // Cancel ListView's touch (un-highlighting the item)
                    MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL |
                            (motionEvent.getActionIndex()
                                    << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                    mItemView.onTouchEvent(cancelEvent);
                    cancelEvent.recycle();
                }

                if (mSwiping) {
                    mDownView.setTranslationX(deltaX - mSwipingSlop);
                    mDownView.setBackgroundColor((int) new ArgbEvaluator().evaluate(Math.max(0f,
                                    Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)),
                            mDownView.getResources().getColor(R.color.grey_300),
                            mDownView.getResources().getColor(R.color.background)));

                    boolean dismissRight = deltaX > 0;
                    mItemView.findViewById(R.id.deleteLeft).setVisibility(dismissRight ?
                            View.VISIBLE : View.INVISIBLE);
                    mItemView.findViewById(R.id.deleteRight).setVisibility(dismissRight ?
                            View.INVISIBLE : View.VISIBLE);
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private void performDismiss(View downView) {
        // No active animations, process all pending dismisses.
        // Sort by descending position

        mCallbacks.onDismiss(downView);

        // Send a cancel event
        long time = SystemClock.uptimeMillis();
        MotionEvent cancelEvent = MotionEvent.obtain(time, time,
                MotionEvent.ACTION_CANCEL, 0, 0, 0);
        mItemView.dispatchTouchEvent(cancelEvent);
    }

}