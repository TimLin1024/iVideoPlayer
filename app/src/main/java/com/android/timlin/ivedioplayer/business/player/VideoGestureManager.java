package com.android.timlin.ivedioplayer.business.player;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.timlin.ivedioplayer.R;
import com.android.timlin.ivedioplayer.business.player.widget.media.IjkVideoView;
import com.android.timlin.ivedioplayer.common.utils.ScreenUtil;

/**
 * Created by linjintian on 2019/4/27.
 */
public class VideoGestureManager {
    private static final String TAG = "VideoGestureManager";
    private static final int DISMISS_DELAY_TIME_IN_MILL = 500;

    private final Activity mActivity;
    private IjkVideoView mVideoView;
    private AudioManager mAudioManager;
    public GestureDetector mGestureDetector;
    private ProgressBar mPbVolume;
    private ProgressBar mPbBrightness;
    private LinearLayout mLlValueIcContainer;
    private ImageView mIvIcon;
    private TextView mTvValue;
    private LinearLayout mLlPositionText;
    private TextView mTvPosition;
    private TextView mTvPositionOffset;

    private WindowManager.LayoutParams mLayoutParams;

    private int mMaxVolume;
    private int mVolumeVal = -1;
    private float mBrightness = -1;
    private float mNewProgress = 0f;
    private float mOldProgress = 0f;
    private MediaController mMediaController;

    private Runnable mDismissVolumeRunnable = new Runnable() {
        @Override
        public void run() {
            mPbVolume.setVisibility(View.INVISIBLE);
        }
    };
    private Runnable mDismissIcValueContainerRunnable = new Runnable() {
        @Override
        public void run() {
            mLlValueIcContainer.setVisibility(View.INVISIBLE);
        }
    };
    private Runnable mDismissProgressTextContainerRunnable = new Runnable() {
        @Override
        public void run() {
            mLlPositionText.setVisibility(View.INVISIBLE);
        }
    };
    private Runnable mDismissBrightnessBarRunnable = new Runnable() {
        @Override
        public void run() {
            mPbBrightness.setVisibility(View.INVISIBLE);
        }
    };

    public VideoGestureManager(final Activity activity, MediaController mediaController) {
        mActivity = activity;
        mMediaController = mediaController;
        initView(activity);
        initAudioManager(activity);

        mGestureDetector = new GestureDetector(activity, new PlayerGestureListener());
        mLayoutParams = activity.getWindow().getAttributes();
    }


    private void initAudioManager(Activity activity) {
        mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    private void initView(Activity activity) {
        initVideoView(activity);
        mPbBrightness = activity.findViewById(R.id.pb_light_bar);
        mPbVolume = activity.findViewById(R.id.pb_volume_bar);
        mLlValueIcContainer = activity.findViewById(R.id.ll_value_ic_container);
        mIvIcon = activity.findViewById(R.id.iv_icon);
        mTvValue = activity.findViewById(R.id.tv_value);
        mLlPositionText = activity.findViewById(R.id.ll_position_text);
        mTvPosition = activity.findViewById(R.id.tv_position);
        mTvPositionOffset = activity.findViewById(R.id.tv_position_offset);
    }

    private void initVideoView(Activity activity) {
        mVideoView = activity.findViewById(R.id.video_view);
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (mVolumeVal == -1) {
            mVolumeVal = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolumeVal < 0)
                mVolumeVal = 0;
        }
        int currentVolume = (int) (percent * mMaxVolume) + mVolumeVal;
        if (currentVolume > mMaxVolume) {
            currentVolume = mMaxVolume;
        } else if (currentVolume < 0) {
            currentVolume = 0;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
        int volumeProgress = (int) (currentVolume * 1.0 / mMaxVolume * 100);
        updateVolumeBar(volumeProgress);
    }

    private void updateVolumeBar(int volume) {
        mPbVolume.setVisibility(View.VISIBLE);
        mPbVolume.removeCallbacks(mDismissVolumeRunnable);
        mPbVolume.setProgress(volume);
        mPbVolume.postDelayed(mDismissVolumeRunnable, DISMISS_DELAY_TIME_IN_MILL);

        mLlValueIcContainer.removeCallbacks(mDismissIcValueContainerRunnable);
        mLlValueIcContainer.postDelayed(mDismissIcValueContainerRunnable, DISMISS_DELAY_TIME_IN_MILL);
        mLlValueIcContainer.setVisibility(View.VISIBLE);
        mIvIcon.setImageResource(R.drawable.ic_volume);
        mTvValue.setText(String.valueOf(volume));
    }

    private void onProgressSlide(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float scrollX = e2.getX() - e1.getX();
        final int duration = mVideoView.getDuration();
        final int videoViewWidth = mVideoView.getWidth();
        long offsetTime = calculateOffsetTime(scrollX, duration, videoViewWidth);
        calAndLimitProgressWithinBound(scrollX, videoViewWidth);
        mTvPosition.setText(DateUtils.formatElapsedTime((long) (mNewProgress * duration / 1000)));
        mTvPositionOffset.setText(String.format("[%s%s]", scrollX > 0 ? "+" : "-", DateUtils.formatElapsedTime(offsetTime)));
        showProgressTextContainer();
        mMediaController.show();//显示进度条所在的容器
        mVideoView.seekTo((int) (mNewProgress * duration));
    }

    private void calAndLimitProgressWithinBound(float scrollX, int videoViewWidth) {
        mNewProgress = mOldProgress + scrollX / videoViewWidth;
        if (scrollX > 0) {
            if (mNewProgress > 1.f) {
                mNewProgress = 1.f;
            }
        } else {
            if (mNewProgress < 0) {
                mNewProgress = 0;
            }
        }
    }

    private long calculateOffsetTime(float scrollX, int duration, int videoViewWidth) {
        long offsetTime = (long) (Math.abs(scrollX) / videoViewWidth * duration / 1000);
        final int currentPosition = mVideoView.getCurrentPosition();
        if (scrollX > 0) {
            //如果是快进，则时间差不能大于剩下的 time
            offsetTime = Math.min(offsetTime, duration - currentPosition);
        } else {
            //如果是后退，则时间差不能大于已经经过的 time
            offsetTime = Math.min(offsetTime, currentPosition);
        }
        return offsetTime;
    }

    private void showValueIcContainer() {
        mLlValueIcContainer.removeCallbacks(mDismissIcValueContainerRunnable);
        mLlValueIcContainer.setVisibility(View.VISIBLE);
        mLlValueIcContainer.postDelayed(mDismissIcValueContainerRunnable, DISMISS_DELAY_TIME_IN_MILL);
    }

    private void showProgressTextContainer() {
        mLlPositionText.removeCallbacks(mDismissProgressTextContainerRunnable);
        mLlPositionText.setVisibility(View.VISIBLE);
        mLlPositionText.postDelayed(mDismissProgressTextContainerRunnable, DISMISS_DELAY_TIME_IN_MILL);
    }


    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = mLayoutParams.screenBrightness;
            if (mBrightness <= 0.00f) {
                mBrightness = 0.50f;
            } else if (mBrightness < 0.01f) {
                mBrightness = 0.01f;
            }
        }
        Log.d(TAG, "onBrightnessSlide mBrightness=" + mBrightness + ",percent=" + percent);
        float currentVal = calCurrentBrightnessVal(percent);
        applyBrightness(currentVal);
        updateBrightnessBar(currentVal);
    }

    private void applyBrightness(float currentVal) {
        mLayoutParams.screenBrightness = currentVal;
        mActivity.getWindow().setAttributes(mLayoutParams);
    }

    private float calCurrentBrightnessVal(float percent) {
        float currentVal = mBrightness + percent;
        if (currentVal < 0) {
            currentVal = 0;
        } else if (currentVal > 1.0f) {
            currentVal = 1.0f;
        }
        return currentVal;
    }

    private void updateBrightnessBar(float currentVal) {
        mPbBrightness.setVisibility(View.VISIBLE);
        mPbBrightness.removeCallbacks(mDismissBrightnessBarRunnable);
        final int displayVal = (int) (currentVal * 100);
        mPbBrightness.setProgress(displayVal);
        mPbBrightness.postDelayed(mDismissBrightnessBarRunnable, DISMISS_DELAY_TIME_IN_MILL);

        showValueIcContainer();
        mIvIcon.setImageResource(R.drawable.ic_brightness);
        mTvValue.setText(String.valueOf(displayVal));
    }

    public class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean mFirstTouch;
        private boolean mIsAdjustingProgress;
        private boolean mIsVolumeControl;

        @Override
        public boolean onDoubleTap(MotionEvent e) {
//            mVideoView.toggleAspectRatio();
            return super.onDoubleTap(e);
        }

        @Override
        public boolean onDown(MotionEvent e) {
            mFirstTouch = true;
            //每次按下的时候更新当前亮度和音量，还有进度
            mOldProgress = mNewProgress;
            updateBrightness();
            updateVolume();
            return super.onDown(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final float lastX = e1.getX();
            final float lastY = e1.getY();
            final float deltaX = lastX - e2.getX();
            if (mFirstTouch) {
                mFirstTouch = false;
                mIsVolumeControl = lastX > ScreenUtil.INSTANCE.getScreenWidthInPixel() * 0.5f;//右半屏上下滑动调节音量
                mIsAdjustingProgress = Math.abs(distanceX) >= Math.abs(distanceY);
            }

            if (!mIsAdjustingProgress) {
                final float deltaY = lastY - e2.getY();
                final float percent = deltaY / mVideoView.getHeight();
                if (mIsVolumeControl) {
                    onVolumeSlide(percent);
                } else {
                    onBrightnessSlide(percent);
                }
            } else {
//                onProgressSlide(-deltaX / mVideoView.getWidth());
                onProgressSlide(e1, e2, distanceX, distanceY);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        private void updateVolume() {
            mVolumeVal = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        }

        private void updateBrightness() {
            mBrightness = mLayoutParams.screenBrightness;
            if (mBrightness == -1) {
                //一开始是默认亮度的时候，获取系统亮度，计算比例值
                mBrightness = getSystemBrightness() / 255f;
            }
        }

        /**
         * 获取系统亮度
         */
        private int getSystemBrightness() {
            return Settings.System.getInt(mActivity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 255);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }
    }

}