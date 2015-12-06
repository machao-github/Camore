package com.machao.camore.app.application.display_items;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.machao.camore.R;

public class ProgressBarWithTitle {
    private static final String LOG_TAG = "progressBar";
    private View parentView;
    private ProgressBar progressBar;
    private TextView title;
    private Activity curActivity;

    public ProgressBarWithTitle(Activity activity,int viewId){
        curActivity = activity;

        parentView = curActivity.findViewById(viewId);
        title = (TextView)parentView.findViewById(R.id.progress_bar_title);
        progressBar = (ProgressBar)parentView.findViewById(R.id.progress_bar);

    }

    public void setProgress(final int progress){
        if (progress < 0 || progress > 100){
            Log.e(LOG_TAG,"invalid progress" + Double.toString(progress));
            System.exit(-1);
        }

        curActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(progress);
            }
        });
    }

    public void setTitle(final String str){
        curActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                title.setText(str);
            }
        });
    }
}
