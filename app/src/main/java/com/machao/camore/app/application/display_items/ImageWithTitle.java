package com.machao.camore.app.application.display_items;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.machao.camore.R;


public class ImageWithTitle {
    ImageView im;
    TextView imTitle;
    View parentView;
    Activity curActivity;


    public ImageWithTitle(Activity activity,int viewId){
        curActivity = activity;
        parentView = activity.findViewById(viewId);
        im = (ImageView)parentView.findViewById(R.id.image_item);
        imTitle = (TextView)parentView.findViewById(R.id.image_title);

        im.setOnTouchListener(new View.OnTouchListener() {
            private float prevX,prevY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                float curX, curY;

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        prevX = event.getX();
                        prevY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        curX = event.getX();
                        curY = event.getY();
                        im.scrollBy((int) (prevX - curX), (int) (prevY - curY));
                        prevX = curX;
                        prevY = curY;
                        break;
                    case MotionEvent.ACTION_UP:
                        curX = event.getX();
                        curY = event.getY();
                        im.scrollBy((int) (prevX - curX), (int) (prevY - curY));
                        break;
                }

                return true;
            }
        });
    }

    public int getImWidth(){
        return im.getWidth();
    }

    public int getImHeight(){
        return im.getHeight();
    }

    public int getMaxWidth(){
        return im.getMaxWidth();
    }

    public int getMaxHeight(){
        return im.getMaxHeight();
    }

    public void setImage(final Bitmap src){
        curActivity.runOnUiThread(new Runnable(){
            @Override
            public void run() {
                im.setVisibility(View.VISIBLE);
                im.setImageBitmap(src);
            }
        });
    }


    public void setTitle(final String str){
        curActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imTitle.setText(str);
            }
        });
    }


}
