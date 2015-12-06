package com.machao.camore.app.content_provider.camera;

import android.app.Activity;
import android.content.Intent;
import android.widget.FrameLayout;

import com.machao.camore.app.Configs.Configs;
import com.machao.camore.app.application.activities.CompuStatusActivity;
import com.machao.camore.app.application.activities.MainActivity;

import java.io.IOException;


public class Preview implements MainActivity.MainActFram {
    private SurfacePreview preview;
    private Cam cam;
    private PicTaker picTaker;
    private FrameLayout frameLayout;
    private Activity activity;

    public Preview(Activity activity,FrameLayout camPreviewLayout){
        this.activity = activity;
        this.frameLayout = camPreviewLayout;
        cam = new Cam();
        preview = new SurfacePreview(activity,cam);
        camPreviewLayout.addView(preview);
        picTaker = new PicTaker(cam.getCam());
    }


    public void start()throws IOException{
        startTakingPics();
    }

    public void stop(){

        preview.stop();
        frameLayout.removeAllViews();
    }

    private PicTaker.onAllTakenListener allPicsTakenHandler = new PicTaker.onAllTakenListener() {
        @Override
        public void onAllPicsTaken(String path){
            stop();
            startCompuation(path);
        }
    };

    private void startCompuation(String path){
        Configs.setWorkingDir(path);
        Intent intent = new Intent(activity,CompuStatusActivity.class);
        activity.startActivity(intent);

    }

    private void startTakingPics()throws IOException{
        picTaker.takePicture();
        picTaker.setAllPicsTakenListener(allPicsTakenHandler);
    }
}
