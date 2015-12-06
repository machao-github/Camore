package com.machao.camore.app.application.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;

import com.machao.camore.app.computation_core.calcu_manager.CalcuManager;
import com.machao.camore.R;
import com.machao.camore.app.Configs.Configs;
import com.machao.camore.app.application.display_items.ImageWithTitle;
import com.machao.camore.app.application.display_items.ProgressBarWithTitle;
import com.machao.camore.app.computation_core.compu_exception.ComputException;
import com.machao.camore.app.computation_core.im_data.Data;
import com.machao.camore.app.computation_core.rs_invokor.RsInvokorBase;

import java.io.IOException;

public class CompuStatusActivity extends Activity {

    private ImageWithTitle oriImItem;
    private ImageWithTitle supImItem;
    private ProgressBarWithTitle overallProgress;
    private ProgressBarWithTitle curImProgress;
    private Button okButton;
    private static final int MAX_IMAGE_VIEW_LEN = 3072;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compu_status);
        getViews();
        initViews();
    }

    @Override
    public void onBackPressed(){
        if (calcuTask != null){
            calcuTask.cancel(true);
        }

        finishThis();
    }

    private void getViews(){
        oriImItem = new ImageWithTitle(this,R.id.im_ori_item);
        supImItem = new ImageWithTitle(this,R.id.im_cur_item);
        overallProgress = new ProgressBarWithTitle(this,R.id.overall_progress);
        curImProgress = new ProgressBarWithTitle(this,R.id.cur_im_progress);
        okButton = (Button)findViewById(R.id.compute_start_button);
    }



    public void initViews(){

        oriImItem.setTitle(Configs.ORI_IM_TITLE);
        supImItem.setTitle(Configs.CUR_IM_TITLE);

        overallProgress.setTitle(Configs.OVERALL_PROG_TITLE);
        curImProgress.setTitle(Configs.CUR_PROG_TITLE);

        overallProgress.setProgress(0);
        curImProgress.setProgress(0);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MakeStartButtonInvisible();
                runInBackground();
            }
        });
    }

    private void MakeStartButtonInvisible(){
        okButton.setVisibility(View.INVISIBLE);
    }

    private void MakeStartButtonVisible(){
        okButton.setVisibility(View.VISIBLE);
    }

    protected class calcuStatusHandler implements CalcuManager.StatusListener {

        public void onOverallProgressChanged(int progress){
            overallProgress.setProgress(progress);
        }
        public void onCurProgressChanged(int progress){
            curImProgress.setProgress(progress);
        }
        public void onSuperResAvail(Bitmap superRes,final String path){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(CompuStatusActivity.this)
                            .setTitle("super-resolution image generated")
                            .setMessage("super-resolution image saved at \"" + path + "\"")
                            .setPositiveButton("ok", null)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                }
            });
            supImItem.setImage(superRes);
            handleOkButtonWhenComplete();

        }

        public void onLowResImAvai(Bitmap lowResIm){

            oriImItem.setImage(lowResIm);
        }
        public void ioException(IOException e){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(CompuStatusActivity.this)
                            .setTitle("error")
                            .setMessage("an error occurred when operating files")
                            .setPositiveButton("ok", null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });

        }

        public void onCompuException(ComputException e){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(CompuStatusActivity.this)
                            .setTitle("error")
                            .setMessage("an error occurred when generating super-resolution image")
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finishThis();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
        }

    }

    private void handleOkButtonWhenComplete(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishThis();
                    }
                });
                okButton.setVisibility(View.VISIBLE);
                okButton.setText("Back");
                RsInvokorBase.reset();
            }
        });

    }

    private void finishThis(){
        oriImItem.setImage(null);
        supImItem.setImage(null);

        if (null != calcuManager && null != calcuManager.getData()){
            calcuManager.getData().recycleBitmaps();
        }
        RsInvokorBase.reset();
        finish();

    }

    private CalcuManager calcuManager;
    private calcuStatusHandler statusHandler = new calcuStatusHandler();
    private AsyncTask<Void,Void,Void> calcuTask;

    private void runInBackground(){
        calcuTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                Data.ImViewProperty viewProp = new Data.ImViewProperty();
                viewProp.curImHeight = oriImItem.getImHeight();
                viewProp.curImWidth = oriImItem.getImWidth();
                viewProp.superResImHeight = oriImItem.getImHeight();
                viewProp.superResImWidth = supImItem.getImWidth();
                viewProp.max_width = MAX_IMAGE_VIEW_LEN;
                viewProp.max_height = MAX_IMAGE_VIEW_LEN;

                calcuManager = new CalcuManager( viewProp);
                calcuManager.setStatusListener(statusHandler);
                calcuManager.start();

                return null;
            }
        };

        calcuTask.execute();
    }




}
