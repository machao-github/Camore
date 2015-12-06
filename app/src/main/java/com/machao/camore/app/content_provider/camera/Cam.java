package com.machao.camore.app.content_provider.camera;

import android.hardware.Camera;


public class Cam{

    int maxWidth;
    int maxHeight;
    private Camera camera;

    public Cam(){
        camera = Camera.open();
        configure();
    }

    public float getRatio(){
        int bigger = maxWidth > maxHeight ? maxWidth:maxHeight;
        int smaller = maxWidth > maxHeight ? maxHeight:maxWidth;
        return (float)(bigger)/(float)(smaller);
    }

    private void configure(){
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        
        for( Camera.Size size : parameters.getSupportedPictureSizes()){
            if (size.width >= maxWidth && size.height >= maxHeight) {
                maxWidth = size.width;
                maxHeight = size.height;
            }
        }
        parameters.setPictureSize(maxWidth, maxHeight);
        camera.setParameters(parameters);
    }



    public Camera getCam(){
        return camera;
    }

}
