/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.aip.face.camera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.baidu.aip.face.PreviewView;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

/**
 * 5.0以下相机API的封装。
 */
@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class Camera1Control implements ICameraControl {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private static final int MAX_PREVIEW_SIZE = 2048;

    static {

        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }


    private int displayOrientation = 0;
    private int cameraId = 0;
    private int flashMode;
    private AtomicBoolean takingPicture = new AtomicBoolean(false);

    private Context context;
    private Camera camera;
    private HandlerThread cameraHandlerThread = null;
    private Handler cameraHandler = null;
    private Handler uiHandler = null;

    private Camera.Parameters parameters;
    private PermissionCallback permissionCallback;
    private Rect previewFrame = new Rect();

    private int preferredWidth = 1280;
    private int preferredHeight = 720;

    @CameraFacing
    private int cameraFacing = CAMERA_FACING_FRONT;

    @Override
    public void setDisplayOrientation(@CameraView.Orientation int displayOrientation) {
        this.displayOrientation = displayOrientation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshPermission() {
        startPreview(true);
    }

    /**
     * {@inheritDoc}
     */
//    @Override
//    public void setFlashMode(@FlashMode int flashMode) {
//        if (this.flashMode == flashMode) {
//            return;
//        }
//        this.flashMode = flashMode;
//        updateFlashMode(flashMode);
//    }

    @Override
    public int getFlashMode() {
        return flashMode;
    }

    @Override
    public void setCameraFacing(@CameraFacing int cameraFacing) {
        this.cameraFacing = cameraFacing;
    }


    @Override
    public void start() {
        postStartCamera();
    }

    private SurfaceTexture surfaceTexture;

    private void postStartCamera() {
        if (cameraHandlerThread == null || !cameraHandlerThread.isAlive()) {
            cameraHandlerThread = new HandlerThread("camera");
            cameraHandlerThread.start();
            cameraHandler = new Handler(cameraHandlerThread.getLooper());
            uiHandler = new Handler(Looper.getMainLooper());
        }

        if (cameraHandler == null) {
            return;
        }

        cameraHandler.post(new Runnable() {
            @Override
            public void run() {
                startCamera();
            }
        });
    }

    private void startCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (permissionCallback != null) {
                permissionCallback.onRequestPermission();
            }
            return;
        }
        if (camera == null) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == cameraFacing) {
                    cameraId = i;
                }
            }
            // 部分相机会出错
            try {
                camera = Camera.open(cameraId);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            if (camera == null) {
                return;
            }
        }
//        if (parameters == null) {
//            parameters = camera.getParameters();
//            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
//        }



        int detectRotation = 0;
        if (cameraFacing == ICameraControl.CAMERA_FACING_FRONT) {
            int rotation = ORIENTATIONS.get(displayOrientation);
            rotation = getCameraDisplayOrientation(rotation, cameraId, camera);
            camera.setDisplayOrientation(rotation);
            detectRotation = rotation;
            if (displayOrientation == CameraView.ORIENTATION_PORTRAIT) {
                if (detectRotation == 90 || detectRotation == 270) {
                    detectRotation = (detectRotation + 180) % 360;
                }
            }
        } else if (cameraFacing == ICameraControl.CAMERA_FACING_BACK){
            int rotation = ORIENTATIONS.get(displayOrientation);
            rotation = getCameraDisplayOrientation(rotation, cameraId, camera);
            camera.setDisplayOrientation(rotation);
            detectRotation = rotation;
        } else if (cameraFacing == ICameraControl.CAMERA_USB){
            camera.setDisplayOrientation(0);
            detectRotation = 0;
        }

        opPreviewSize(preferredWidth, preferredHeight);
        final Camera.Size size = camera.getParameters().getPreviewSize();
        if (detectRotation % 180 == 90) {
            previewView.setPreviewSize(size.height, size.width);
        } else {
            previewView.setPreviewSize(size.width, size.height);
        }
        final int temp = detectRotation;
        try {
            if (cameraFacing == ICameraControl.CAMERA_USB) {
                camera.setPreviewTexture(textureView.getSurfaceTexture());
            } else {
                // 适配安卓8.0系统
                if (Build.VERSION.SDK_INT >= 26) {
                    camera.setPreviewTexture(textureView.getSurfaceTexture());
                } else {
                    surfaceTexture = new SurfaceTexture(11);
                    camera.setPreviewTexture(surfaceTexture);
                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            if (textureView != null) {
                                surfaceTexture.detachFromGLContext();
                                textureView.setSurfaceTexture(surfaceTexture);
                            }
                        }
                    });
                }
            }
//            camera.addCallbackBuffer(new byte[size.width * size.height * 3 / 2]);
//            camera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
//
//                @Override
//                public void onPreviewFrame(byte[] data, Camera camera) {
//                    Log.i("wtf", "onPreviewFrame-->");
//                    onFrameListener.onPreviewFrame(data, temp, size.width, size.height);
//                    camera.addCallbackBuffer(data);
//           ad     }
//            });
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    onFrameListener.onPreviewFrame(data, temp, size.width, size.height);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    private TextureView textureView;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setTextureView(TextureView textureView) {
        this.textureView = textureView;
        if (surfaceTexture != null) {
            surfaceTexture.detachFromGLContext();
            textureView.setSurfaceTexture(surfaceTexture);
        }
    }

    private int getCameraDisplayOrientation(int degrees, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation + degrees) % 360;
            rotation = (360 - rotation) % 360; // compensate the mirror
        } else { // back-facing
            rotation = (info.orientation - degrees + 360) % 360;
        }

        return rotation;
    }

    @Override
    public void stop() {
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }

        if (cameraHandlerThread != null) {
            cameraHandlerThread.quit();
            cameraHandlerThread = null;
        }
    }

    @Override
    public void pause() {
        if (camera != null) {
            camera.stopPreview();
        }
//        setFlashMode(FLASH_MODE_OFF);
    }

    @Override
    public void resume() {
        takingPicture.set(false);
        if (camera == null) {
            postStartCamera();
        }
    }

    private OnFrameListener onFrameListener;

    @Override
    public void setOnFrameListener(OnFrameListener listener) {
        this.onFrameListener = listener;
    }

    @Override
    public void setPreferredPreviewSize(int width, int height) {
        this.preferredWidth = Math.max(width, height);
        this.preferredHeight = Math.min(width, height);
    }

    @Override
    public View getDisplayView() {
        return null;
    }


    private PreviewView previewView;

    @Override
    public void setPreviewView(PreviewView previewView) {
        this.previewView = previewView;
        setTextureView(previewView.getTextureView());
    }

    @Override
    public PreviewView getPreviewView() {
        return previewView;
    }

    //    @Override
    //    public void takePicture(final OnTakePictureCallback onTakePictureCallback) {
    //        if (takingPicture.get()) {
    //            return;
    //        }
    //
    //        switch (displayOrientation) {
    //            case CameraView.ORIENTATION_PORTRAIT:
    //                parameters.setRotation(90);
    //                break;
    //            case CameraView.ORIENTATION_HORIZONTAL:
    //                parameters.setRotation(0);
    //                break;
    //            case CameraView.ORIENTATION_INVERT:
    //                parameters.setRotation(180);
    //                break;
    //            default:
    //                break;
    //        }
    //        Camera.Size picSize =
    //                getOptimalSize(preferredWidth, preferredHeight, camera.getParameters().getSupportedPictureSizes());
    //        parameters.setPictureSize(picSize.width, picSize.height);
    //        camera.setParameters(parameters);
    //        takingPicture.set(true);
    //        camera.autoFocus(new Camera.AutoFocusCallback() {
    //            @Override
    //            public void onAutoFocus(boolean success, Camera camera) {
    //                camera.cancelAutoFocus();
    //                try {
    //                    camera.takePicture(null, null, new Camera.PictureCallback() {
    //                        @Override
    //                        public void onPictureTaken(byte[] data, Camera camera) {
    //                            camera.startPreview();
    //                            takingPicture.set(false);
    //                            if (onTakePictureCallback != null) {
    //                                onTakePictureCallback.onPictureTaken(data);
    //                            }
    //                        }
    //                    });
    //                } catch (RuntimeException e) {
    //                    e.printStackTrace();
    //                    camera.startPreview();
    //                    takingPicture.set(false);
    //                }
    //            }
    //        });
    //    }

    @Override
    public void setPermissionCallback(PermissionCallback callback) {
        this.permissionCallback = callback;
    }

    public Camera1Control(Context context) {
        this.context = context;
    }

    // 开启预览
    private void startPreview(boolean checkPermission) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (checkPermission && permissionCallback != null) {
                permissionCallback.onRequestPermission();
            }
            return;
        }
        camera.startPreview();
    }

    private void opPreviewSize(int width, @SuppressWarnings("unused") int height) {

        if (camera != null && width > 0) {
            try {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size optSize = getOptimalSize(width, height, camera.getParameters().getSupportedPreviewSizes());
                Log.i("wtf", "opPreviewSize-> " + optSize.width + " " +  optSize.height);
                parameters.setPreviewSize(optSize.width, optSize.height);
                // parameters.setPreviewFpsRange(15, 25);
                camera.setParameters(parameters);
                camera.startPreview();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private Camera.Size getOptimalSize(int width, int height, List<Camera.Size> sizes) {

        Camera.Size pictureSize = sizes.get(0);

        List<Camera.Size> candidates = new ArrayList<>();

        for (Camera.Size size : sizes) {
            if (size.width >= width && size.height >= height && size.width * height == size.height * width) {
                // 比例相同
                candidates.add(size);
            } else if (size.height >= width && size.width >= height && size.width * width == size.height * height) {
                // 反比例
                candidates.add(size);
            }
        }
        if (!candidates.isEmpty()) {
            return Collections.min(candidates, sizeComparator);
        }

        for (Camera.Size size : sizes) {
            if (size.width >= width && size.height >= height) {
                return size;
            }
        }

        return pictureSize;
    }

    private Comparator<Camera.Size> sizeComparator = new Comparator<Camera.Size>() {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            return Long.signum((long) lhs.width * lhs.height - (long) rhs.width * rhs.height);
        }
    };

    private void updateFlashMode(int flashMode) {
        switch (flashMode) {
            case FLASH_MODE_TORCH:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                break;
            case FLASH_MODE_OFF:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
            case ICameraControl.FLASH_MODE_AUTO:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
            default:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
        }
        //        camera.setParameters(parameters);
    }

    private int getSurfaceOrientation() {
        @CameraView.Orientation
        int orientation = displayOrientation;
        switch (orientation) {
            case CameraView.ORIENTATION_PORTRAIT:
                return 90;
            case CameraView.ORIENTATION_HORIZONTAL:
                return 0;
            case CameraView.ORIENTATION_INVERT:
                return 180;
            default:
                return 90;
        }
    }

    @Override
    public Rect getPreviewFrame() {
        return previewFrame;
    }
}
