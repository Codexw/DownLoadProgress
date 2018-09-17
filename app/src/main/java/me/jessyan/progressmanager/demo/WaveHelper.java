package me.jessyan.progressmanager.demo;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

public class WaveHelper {
    private WaveView mWaveView;

    private AnimatorSet mAnimatorSet;
    private ObjectAnimator mWaterLevelAnim;
    private long mCurrentPlayTime = 0L;
    private float mCurrentPrecent = 0f;

    public WaveHelper(WaveView waveView) {
        mWaveView = waveView;
        initAnimation();
    }

    public void start() {
        mWaveView.setShowWave(true);
        if (mAnimatorSet != null) {
            mAnimatorSet.start();
        }
    }

    private void initAnimation() {
        List<Animator> animators = new ArrayList<>();

        // horizontal animation.
        // wave waves infinitely.
        ObjectAnimator waveShiftAnim = ObjectAnimator.ofFloat(
                mWaveView, "waveShiftRatio", 0f, 1f);
        waveShiftAnim.setRepeatCount(ValueAnimator.INFINITE);
        waveShiftAnim.setDuration(1000);
        waveShiftAnim.setInterpolator(new LinearInterpolator());
        animators.add(waveShiftAnim);

        // vertical animation.
        // water level increases from 0 to center of WaveView
//        ObjectAnimator waterLevelAnim = ObjectAnimator.ofFloat(
//                mWaveView, "waterLevelRatio", 0f, 1f);
//        waterLevelAnim.setRepeatCount(ValueAnimator.INFINITE);
//        waterLevelAnim.setRepeatMode(ValueAnimator.REVERSE);
//        waterLevelAnim.setDuration(10000);
//        waterLevelAnim.setInterpolator(new DecelerateInterpolator());
//        animators.add(waterLevelAnim);

        // amplitude animation.
        // wave grows big then grows small, repeatedly
        ObjectAnimator amplitudeAnim = ObjectAnimator.ofFloat(
                mWaveView, "amplitudeRatio", 0.0001f, 0.05f);
        amplitudeAnim.setRepeatCount(ValueAnimator.INFINITE);
        amplitudeAnim.setRepeatMode(ValueAnimator.REVERSE);
        amplitudeAnim.setDuration(5000);
        amplitudeAnim.setInterpolator(new LinearInterpolator());
        animators.add(amplitudeAnim);

        mWaterLevelAnim = ObjectAnimator.ofFloat(
                mWaveView, "waterLevelRatio", 0f, 1f);
        mWaterLevelAnim.setDuration(3000);
        mWaterLevelAnim.setInterpolator(new DecelerateInterpolator());
        mWaterLevelAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                if(value >= mCurrentPrecent){
                    stopAnimation();
                }
            }
        });
        mWaterLevelAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        mAnimatorSet = new AnimatorSet();
//        mAnimatorSet.playTogether(animators);
        mAnimatorSet.playTogether(waveShiftAnim, amplitudeAnim, mWaterLevelAnim);

    }

    public void setCurrentPrecent(float precent, int time){
        mCurrentPrecent = precent;
        if(time != 0)
            mWaterLevelAnim.setDuration(time);
    }

    public void cancel() {
        if (mAnimatorSet != null) {
//            mAnimatorSet.cancel();
            mAnimatorSet.end();
        }
    }

    //17701528
    public void stopAnimation(){
        mCurrentPlayTime = mWaterLevelAnim.getCurrentPlayTime();
        if(mCurrentPlayTime > 1000*1000)
            mCurrentPlayTime = 0L;
        mWaterLevelAnim.cancel();
    }

    public void startAnimation() {
        mWaterLevelAnim.start();
        mWaterLevelAnim.setCurrentPlayTime(mCurrentPlayTime);
    }
}
