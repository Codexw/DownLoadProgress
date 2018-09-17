package me.jessyan.progressmanager.demo;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import me.jessyan.progressmanager.ProgressListener;
import me.jessyan.progressmanager.ProgressManager;
import me.jessyan.progressmanager.body.ProgressInfo;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * Author: xu
 * Date:2018/9/17
 * Description:
 */
public class DownWaveActivity extends AppCompatActivity implements View.OnClickListener {

    public String mDownloadUrl = new String("http://of2rh8u96.bkt.clouddn.com/setup_11.4.0.2002s.exe");

    private Button mDownBtn;
    private WaveView mWaveView;
    private WaveHelper mWaveHelper;

    private Retrofit mRetrofit;
    private Api mApi;
    private ProgressInfo mLastDownloadingInfo;

    private int mBorderColor = Color.parseColor("#44FFFFFF");
    private int mBorderWidth = 0;
    private AnimatorSet mAnimatorSet;
    private float mDefaultPercent = 0f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down);

        mWaveView = findViewById(R.id.down_wave);
        findViewById(R.id.down_btn).setOnClickListener(this);
        findViewById(R.id.resume_btn).setOnClickListener(this);

        mRetrofit = ((BaseApplication) getApplicationContext()).getRetrofit();
        mApi = mRetrofit.create(Api.class);


        mWaveView.setWaveColor(
                Color.parseColor("#28f16d7a"),
                Color.parseColor("#3cf16d7a"));
        mBorderColor = Color.parseColor("#44f16d7a");
        mWaveView.setBorder(mBorderWidth, mBorderColor);
        mWaveView.setShapeType(WaveView.ShapeType.CIRCLE);
//        mWaveView.setWaveColor(
//                WaveView.DEFAULT_BEHIND_WAVE_COLOR,
//                WaveView.DEFAULT_FRONT_WAVE_COLOR);
        mWaveHelper = new WaveHelper(mWaveView);

        ProgressManager.getInstance().addResponseListener(mDownloadUrl, getDownloadListener());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.down_btn:
                downloadStart();
//                mWaveHelper.stopAnimation();
                break;
            case R.id.resume_btn:
//                mWaveHelper.startAnimation();
                break;
        }
    }

    private void downloadStart() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File directory = new File("/mnt/sdcard/Test/");
                    File file = new File("/mnt/sdcard/Test/test");
                    if (!directory.exists()) {
                        directory.mkdir();
                    }

//                    Request request = new Request.Builder()
//                            .url(mDownloadUrl)
//                            .build();

//                    Response response = mOkHttpClient.newCall(request).execute();

                    Call<ResponseBody> call = mApi.getCall();
                    retrofit2.Response<ResponseBody> response = call.execute();


                    InputStream is = response.body().byteStream();
                    //为了方便就不动态申请权限了,直接将文件放到CacheDir()中
//                    File file = new File(getCacheDir(), "download");
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = bis.read(buffer)) != -1) {
                        fos.write(buffer, 0, len);
                    }
                    fos.flush();
                    fos.close();
                    bis.close();
                    is.close();


                } catch (IOException e) {
                    e.printStackTrace();
                    //当外部发生错误时,使用此方法可以通知所有监听器的 onError 方法
                    ProgressManager.getInstance().notifyOnErorr(mDownloadUrl, e);
                }
            }
        }).start();
    }

    private ProgressListener getDownloadListener() {
        return new ProgressListener() {
            @Override
            public void onProgress(ProgressInfo progressInfo) {
                if (mLastDownloadingInfo == null) {
                    mLastDownloadingInfo = progressInfo;
                }

                //因为是以请求开始时的时间作为 Id ,所以值越大,说明该请求越新
                if (progressInfo.getId() < mLastDownloadingInfo.getId()) {
                    return;
                } else if (progressInfo.getId() > mLastDownloadingInfo.getId()) {
                    mLastDownloadingInfo = progressInfo;
                }

                float progress = mLastDownloadingInfo.getPercent() / 100.0f;
//                synchronized (ObjectAnimator.class){
//                    if (mDefaultPercent != progress){
//                        ObjectAnimator waterLevelAnim = ObjectAnimator.ofFloat(
//                                mWaveView, "waterLevelRatio", 0f, 1f);
//                        waterLevelAnim.setDuration(10000);
//                        waterLevelAnim.setInterpolator(new DecelerateInterpolator());
//                        mDefaultPercent = progress;
//                        waterLevelAnim.start();
//                    }
//                }
                if (progress >= mDefaultPercent + 0.1f || progress == 1.0f) {
                    int time = progress == 1.0f ? 0 : 1000;
                    mWaveHelper.setCurrentPrecent(progress, time);
                    mWaveHelper.startAnimation();
                    mDefaultPercent = progress;
                }

                Toast.makeText(DownWaveActivity.this, "进度" + progress, Toast.LENGTH_SHORT).show();
                if (mLastDownloadingInfo.isFinish()) {
                    Toast.makeText(DownWaveActivity.this, "完成", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(long id, Exception e) {
                Toast.makeText(DownWaveActivity.this, "error", Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDownloadUrl = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWaveHelper.cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWaveHelper.start();
    }
}
