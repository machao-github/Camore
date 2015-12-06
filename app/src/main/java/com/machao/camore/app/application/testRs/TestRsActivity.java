package com.machao.camore.app.application.testRs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.machao.camore.R;
import com.machao.camore.app.computation_core.ScriptC_invert;

public class TestRsActivity extends AppCompatActivity {

    private Bitmap mBitmap;
    private ImageView mImView;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_test_rs);

        mImView = (ImageView)findViewById(R.id.image_view);
        Bitmap bitmapImmutable = BitmapFactory.decodeResource(getResources(),R.drawable.scene);
        mBitmap = bitmapImmutable.copy(bitmapImmutable.getConfig(),true);

        mTextView = (TextView)findViewById(R.id.text_count);

        mImView.setImageBitmap(mBitmap);
        CreateRs();

        mImView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int actionType = event.getAction();
                if(actionType == event.ACTION_DOWN){
                    RunRs();
                    return true;
                }

                return false;
            }
        });
    }

    private void InvertImage(){
        int rowNum = mBitmap.getHeight();
        int colNum = mBitmap.getWidth();

        for(int x = 0; x < colNum; x++){
            for(int y =0; y<rowNum; y++){
                int color = (0x00ffffff - mBitmap.getPixel(x,y)) | 0xff000000;
                mBitmap.setPixel(x,y,color);
            }
        }

        mImView.setImageBitmap(mBitmap);
    }

    private RenderScript mRs;
    private Allocation mAllocIn;
    private Allocation mAllocOut;
    private ScriptC_invert mInvertScript;

    private Allocation mTmpIm;
    Type mTmpImType;

    private void CreateRs(){

        mRs = RenderScript.create(this);
        mAllocIn = Allocation.createFromBitmap(mRs,mBitmap);
        mAllocOut = Allocation.createFromBitmap(mRs, mBitmap);
        mInvertScript = new ScriptC_invert(mRs);

        mInvertScript.set_im_col((long) mBitmap.getWidth());

        Type.Builder typeBuilder = new Type.Builder(mRs,Element.RGBA_8888(mRs));
        typeBuilder.setX(mBitmap.getWidth());
        typeBuilder.setY(mBitmap.getHeight());
        mTmpImType = typeBuilder.create();


        mTmpIm = Allocation.createTyped(mRs, mTmpImType);


        mInvertScript.bind_g_image(mTmpIm);
        mInvertScript.set_g_imB(mTmpIm);
    }

    private void RunRs(){
        mTextView.setText("start new computation");

        Script.LaunchOptions opt = new Script.LaunchOptions();
        opt.setX(2, mBitmap.getWidth() - 2);
        opt.setY(2, mBitmap.getHeight() - 2);

        mTmpIm.copyFrom(mBitmap);

        long timeStart = System.currentTimeMillis();
        mInvertScript.forEach_invert(mAllocIn, mAllocOut,opt);
        long timeEnd = System.currentTimeMillis();


        mAllocOut.copyTo(mBitmap);
        mTmpIm.copyFrom(mAllocOut);
        mImView.setImageBitmap(mBitmap);

        long timeConsumedS = (timeEnd - timeStart)/1000;
        long timeConsumedMs = (timeEnd - timeStart)%1000;
        mTextView.setText("time consumed:" + Long.toString(timeConsumedS) + " s ,"
            + Long.toString(timeConsumedMs) + " ms");
    }
}
