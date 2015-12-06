package com.machao.camore.app.content_provider.camera;

import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PicTaker {
    private Camera camera;
    private static final int MAX_NUM_TO_TAKE = 4;
    private int			 mPicturesTaken;

    public PicTaker(Camera camera){
        this.camera = camera;
    }


    private String dirToSave;
    private void createDirToSave()throws IOException{
       String path =  Environment.getExternalStorageDirectory()
                +"/pics/";
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()){
            file.mkdirs();
        }

        int index = 1;
        String newPath;
        while (true){
            newPath = path + Integer.toString(index)+"/";
            index ++;
            file = new File(newPath);
            if (!file.exists()) {
                file.mkdirs();
                break;
            }
        }

        dirToSave = newPath;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                String externDir = dirToSave;
                File pictureDir = new File(externDir);
                pictureDir.mkdir();

                SimpleDateFormat format = new SimpleDateFormat("yyyyddMMhhmmss");
                String timeStamp = format.format(new Date());
                String fileName = externDir	+ timeStamp + "_" + System.currentTimeMillis()+".jpg";

                File imageFile = new File(fileName);
                FileOutputStream fos = new FileOutputStream(imageFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d("error", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d("error", "Error accessing file: " + e.getMessage());
            }
            camera.startPreview();

            if (mPicturesTaken > 0) {
                mPicturesTaken--;
                camera.takePicture(null, null, null,this);
            }
            else {
               onAllPicsTaken();
            }
        }
    };


    private onAllTakenListener listener;
    public void onAllPicsTaken(){
        listener.onAllPicsTaken(dirToSave);
    }

    public void setAllPicsTakenListener(onAllTakenListener listener){
        this.listener = listener;
    }

    public interface onAllTakenListener{
        void onAllPicsTaken(String path);
    }


    private Camera.AutoFocusCallback mOnFocusDone = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                camera.takePicture(null, null, null, mPicture);
            }
        }
    };


    public void takePicture()throws IOException{
        createDirToSave();
        mPicturesTaken = MAX_NUM_TO_TAKE;
        camera.autoFocus(mOnFocusDone);
    }
}
