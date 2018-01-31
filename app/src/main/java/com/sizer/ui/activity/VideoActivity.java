package com.sizer.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.devdoo.rxpermissions.RxPermission;
import com.sizer.App;
import com.sizer.R;
import com.sizer.data.ILocalRepository;
import com.sizer.model.ScanData;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;

public class VideoActivity extends BaseActivity {

    @BindView(R.id.camera)
    CameraView cameraView;
    List<SavePhotoTask> tasks = new ArrayList<>();;
    private static int frameCnt;
    Subscription subscription;
    private final int width = 480;
    private final int height = 640;

    private static ILocalRepository localRepository;

    @Override
    int getContentViewLayoutResource() {
        return R.layout.activity_video;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        localRepository = App.getAppComponent().localDataRepository();
        frameCnt = 0;
        ButterKnife.bind(this);
        subscription =
                RxPermission.with(getFragmentManager())
                        .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) {
                                if (!cameraView.isStarted()) {
                                    cameraView.setVideoQuality(CameraKit.Constants.VIDEO_QUALITY_480P);
                                    cameraView.setJpegQuality(20);
                                    cameraView.postDelayed(capturePreview, 1000);
                                    //TODO: or capture captureVideo() call to capture video
                                }
                            }
                        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        subscription.unsubscribe();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        for (SavePhotoTask task : tasks) {
            if (!task.isCancelled()) {
                task.cancel(false);
            }
        }
        super.onDestroy();
    }


    private Runnable capturePreview = new Runnable() {
        @Override
        public void run() {
            cameraView.captureImage(new CameraKitEventCallback<CameraKitImage>() {

                @Override
                public void callback(CameraKitImage cameraKitImage) {
                    Bitmap bmp = cameraKitImage.getBitmap().copy(Bitmap.Config.RGB_565, true);
                    bmp.setWidth(width);
                    bmp.setHeight(height);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 20, stream);
                    byte[] byteArray = stream.toByteArray();
                    SavePhotoTask task = new SavePhotoTask();
                    tasks.add(task);
                    frameCnt++;
                    task.doInBackground(byteArray);

                }
            });
            if (frameCnt > 9) {
                cameraView.stopVideo();
                startActivity(new Intent(VideoActivity.this, RegisterActivity.class));
                finish();
            } else {
                // Run again after approximately 1 second.
                cameraView.postDelayed(this, 1000);
            }
        }
    };

    private void captureVideo() {
        String str = localRepository.getUniqueDeviceId() + File.separator + localRepository.setScanData(new ScanData()).getScanId() + File.separator;
        File scanPath = new File(Environment.getExternalStorageDirectory(), str);
        File photo = new File(scanPath, "capture.mpeg");

        scanPath.mkdirs();
        cameraView.captureVideo(photo, new CameraKitEventCallback<CameraKitVideo>() {
            @Override
            public void callback(CameraKitVideo cameraKitVideo) {
                startActivity(new Intent(VideoActivity.this, RegisterActivity.class));
                finish();
            }
        });

//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                cameraView.stopVideo();
//            }
//        }, 60000);

    }

    static class SavePhotoTask extends AsyncTask<byte[], String, String> {
        @Override
        protected String doInBackground(byte[]... jpeg) {

            String str = localRepository.getUniqueDeviceId() + File.separator + localRepository.setScanData(new ScanData()).getScanId() + File.separator;
            File scanPath = new File(Environment.getExternalStorageDirectory(), str);
            File photo = new File(scanPath, String.format("%06d", frameCnt) + ".jpg");

            scanPath.mkdirs();

            try {
                FileOutputStream fos = new FileOutputStream(photo);

                fos.write(jpeg[0]);
                fos.close();
            } catch (IOException e) {
                Log.e("Sizer", "Exception in photoCallback", e);
            }

            return (null);
        }
    }
}
