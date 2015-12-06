package com.machao.camore.app.content_provider.camera;

import android.app.Activity;
import android.content.Context;

import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class SurfacePreview extends SurfaceView implements SurfaceHolder.Callback{
    Camera camera;
    Cam cam;
    Context context;
    SurfaceHolder surfHolder;


    public SurfacePreview(Context context, Cam cam) {
        super(context);
        this.context = context;
        this.camera = cam.getCam();
        this.cam = cam;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfHolder = getHolder();
        surfHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            camera.setPreviewDisplay(holder);
            //camera.setDisplayOrientation(rotateDegrees);
            camera.startPreview();
        } catch (Exception e) {
            Log.d("Error", "Error setting camera preview: " + e.getMessage());
        }
    }


    public void surfaceDestroyed(SurfaceHolder holder) {
        releaseCam();
    }

    public void stop(){
        releaseCam();
    }

    private boolean isCamReleased = false;
    public void releaseCam(){
        if (camera != null){
            camera.release();
            camera = null;
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        if (surfHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes

        try {
            camera.stopPreview();
        } catch (Exception e){
            Log.d("error","fail to stop preview");
        }

        Camera.CameraInfo camInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, camInfo);
        int curRotation = ((Activity)context).getWindowManager().getDefaultDisplay().getRotation();
        int rotateDegrees;
        switch (curRotation) {
            case Surface.ROTATION_0:  rotateDegrees = 0;break;
            case Surface.ROTATION_90: rotateDegrees = 90;break;
            case Surface.ROTATION_180:rotateDegrees = 180;break;
            case Surface.ROTATION_270:rotateDegrees = 270;break;

            default: rotateDegrees = 0;break;
        }

        int rotate;
        if(camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
             rotate = (camInfo.orientation + rotateDegrees) % 360;
             rotate = (360 - rotate) % 360;
        }
        else{
            rotate = (camInfo.orientation - rotateDegrees + 360) % 360;
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setRotation(rotate);
        camera.setParameters(parameters);

        Rect rect = surfHolder.getSurfaceFrame();
        int width  = rect.width();
        int height = rect.height();
        if (rotateDegrees == 0 || rotateDegrees == 180){
            height = (int)(width*cam.getRatio());

        }
        else {
            width = (int)(height*cam.getRatio());

        }
        surfHolder.setFixedSize(width,height);



        try {
            camera.setPreviewDisplay(surfHolder);
            camera.setDisplayOrientation(rotate);
            camera.startPreview();
        } catch (Exception e){
            Log.d("Error", "Error starting camera preview: " + e.getMessage());
        }
    }
}
