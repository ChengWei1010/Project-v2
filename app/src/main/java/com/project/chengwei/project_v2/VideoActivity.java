package com.project.chengwei.project_v2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
//import android.support.annotation.Size;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.camera2.*;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.util.Size;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

// https://www.youtube.com/watch?v=LskL8OipkKE&t=103s 上次看到的最後位置

public class VideoActivity extends AppCompatActivity {

    private File mVideoFolder;
    private String mVideoFileName;

    private ImageButton mRecordImageButton;
    private boolean mIsRecording = false;

    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
    private TextureView mTextureView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
            setupCamera(width,height);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };


    private CameraDevice mCameraDevice;

    //根據camera回傳的狀態進行調整
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;

            //會加入下列判斷是因為在第一次安裝時會遇到runtime permission，如果同意寫入硬碟會造成app pause then resume，此時會造成所有運作中的元件都消失。
            if(mIsRecording){
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
            }else {
                startPreview();
            }
            //Toast.makeText(getApplicationContext(),
            //      "Camera connection made!",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    private String mCameraId;
    private Size mPreviewSize;
    private Size mVideoSize;

    private MediaRecorder mMediaRecorder; //What does it do?

    private int mTotalRotation;

    private CaptureRequest.Builder mCaptureRequestBuilder;
    private static SparseIntArray ORIENTAIONS = new SparseIntArray();
    static{
        ORIENTAIONS.append(Surface.ROTATION_0,0);
        ORIENTAIONS.append(Surface.ROTATION_90,90);
        ORIENTAIONS.append(Surface.ROTATION_180,180);
        ORIENTAIONS.append(Surface.ROTATION_270,270);
    }


    //當camera存在時關閉camera
    private void closeCamera(){
        if(mCameraDevice != null){
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        startBackgroundThread();

        if(mTextureView.isAvailable()){
            setupCamera(mTextureView.getWidth(),mTextureView.getHeight());
            connectCamera();
        }else{
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    //當使用者切換到其他application時，可以暫時釋放資源。
    @Override
    protected void onPause(){
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    //設置相機資源
    private void setupCamera(int width, int height){
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE); //CameraManager用來管理所有的設備
        try {
            for(String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if(cameraCharacteristics.get(cameraCharacteristics.LENS_FACING) ==   //LENS_FACING是用來取得當前相機的位置
                        cameraCharacteristics.LENS_FACING_FRONT){
                    continue;
                }
                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceRotation = getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = sensorToDeviceRotation(cameraCharacteristics,deviceRotation);
                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                if(swapRotation){
                    rotatedWidth = height;
                    rotatedHeight = width;
                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),rotatedWidth,rotatedHeight);
                mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class),rotatedWidth,rotatedHeight);
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void connectCamera(){
        CameraManager cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED){
                    cameraManager.openCamera(mCameraId,mCameraDeviceStateCallback,mBackgroundHandler);
                }else{
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ) {
                        Toast.makeText(this,"Video app required to camera", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA},REQUEST_CAMERA_PERMISSION_RESULT);
                }
            }else {
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview(){
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture(); //What's different?
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight()); //預設Surface的欄位大小
        Surface previewSurface = new Surface(surfaceTexture); // What's different with others?

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW); //將camera的輸出綁定到mCaptureRequest Builder
            mCaptureRequestBuilder.addTarget(previewSurface); //將輸出畫面綁定到previewSurface

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                null,mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(getApplicationContext(),"Unable to setup camera preview.",Toast.LENGTH_SHORT).show();
                }
            },null );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    private void startBackgroundThread(){
        mBackgroundHandlerThread = new HandlerThread("Camera2Video");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread(){
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation){
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTAIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation + 360) % 360;
    }

    private static class compareSizeByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs){
            return Long.signum((long)lhs.getWidth() * rhs.getHeight() /
                    (long)rhs.getWidth() * lhs.getHeight());
        }
    }

    private static Size chooseOptimalSize(Size[] Choices, int width ,int height){
        List<Size> bigEnough = new ArrayList<Size>();
        for(Size option : Choices){
            if(option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height){
                bigEnough.add(option);
            }
        }
        if(bigEnough.size()>0){
            return Collections.min(bigEnough,new compareSizeByArea());
        }else{
            return Choices[0];
        }
    }

    //根據不同permission回傳的結果所做的不同動作。
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult){
        super.onRequestPermissionsResult(requestCode,permissions,grantResult);
        if(requestCode == REQUEST_CAMERA_PERMISSION_RESULT){
            if(grantResult[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(),
                        "Application will not run without camera services",Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT){
            if(grantResult[0] == PackageManager.PERMISSION_GRANTED){
                mIsRecording = true;
                mRecordImageButton.setImageResource(R.mipmap.video_on);
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this,"Permission successfully granted. ",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"App needs to save to run",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createVideoFolder(){
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);//取得裝置預設的影片存放位置。
        mVideoFolder = new File(movieFile,"camera2Video");//在影片資料夾下再創一個專屬於此app的資料夾。
        if(!mVideoFolder.exists()){ //如果影片資料夾中沒有這個資料夾則自己重創一個。
            mVideoFolder.mkdirs();
        }
    }

    private File createVideoFileName() throws IOException{
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "Video_"+timestamp+"_";
        File videoFile = File.createTempFile(prepend,".mp4",mVideoFolder); //創造mp4格式的檔案;
        mVideoFileName = videoFile.getAbsolutePath();
        return videoFile;

    }

    private void checkWriteStoragePermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ //因為6.0以上版本需要特別認證
            Log.d("Version","Upper 6.0");
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED){
                Log.d("Permission","Permission granted!");
                mIsRecording = true;
                mRecordImageButton.setImageResource(R.mipmap.video_on);
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
            }else{
                Log.d("Permission","Permission denied!");
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    Toast.makeText(this,"app needs to be able to save video.",Toast.LENGTH_SHORT).show();
                    requestPermissions(new  String []{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT );
                }
            }
        }else{
            Log.d("Version","Below 6.0");
            mIsRecording = true;
            mRecordImageButton.setImageResource(R.mipmap.video_on);
            try {
                createVideoFileName();
            } catch (IOException e) {
                e.printStackTrace();
            }
            startRecord();
            mMediaRecorder.start();
        }
    }

    //用來設定影片的格式（如果要限制時間長短好像在這裡調整）
    private void setupMediaRecorder()throws IOException{
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(1000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(),mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();
    }

    private void startRecord(){

        try {
            setupMediaRecorder();
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture(); //What's different?
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight()); //預設Surface的欄位大小
            Surface previewSurface = new Surface(surfaceTexture); // What's different with others?

            //下面是重要觀念 要把它弄懂！
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest( CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            try {
                                cameraCaptureSession.setRepeatingRequest(
                                        mCaptureRequestBuilder.build(),null,null
                                );
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                        }
                    },null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        mTextureView = (TextureView)findViewById(R.id.textureView);

        createVideoFolder();

        mMediaRecorder = new MediaRecorder();

        mRecordImageButton = (ImageButton) findViewById(R.id.recordButton);
        mRecordImageButton.setImageResource(R.mipmap.video_off );
        mRecordImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsRecording){
                    mIsRecording = false;
                    mRecordImageButton.setImageResource(R.mipmap.video_off );
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    startPreview();
                }else{
                    Log.d("start_check_permission","Start check permission");
                    checkWriteStoragePermission();
                }
            }
        });
    }
}

