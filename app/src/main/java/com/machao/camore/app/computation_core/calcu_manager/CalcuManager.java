package com.machao.camore.app.computation_core.calcu_manager;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.machao.camore.app.computation_core.compu_exception.ComputException;
import com.machao.camore.app.computation_core.fusion.Fusion;
import com.machao.camore.app.computation_core.im_data.Data;
import com.machao.camore.app.computation_core.im_data.ImCollection;
import com.machao.camore.app.computation_core.im_data.BlockSplitter;
import com.machao.camore.app.computation_core.motion_estimate.MotionEstimator;
import com.machao.camore.app.computation_core.register.Registration;
import com.machao.camore.app.computation_core.rs_invokor.RsConvertToGray;
import com.machao.camore.app.computation_core.rs_invokor.RsInvokorBase;
import com.machao.camore.app.computation_core.rs_invokor.RsPartialDerivative;

import java.io.IOException;
import java.util.LinkedList;

public class CalcuManager {
    public static final String LOG_TAG = " Calculation ";
    private Data data = new Data();
    public Data getData() {
        return data;
    }



    private LinkedList<CalcuProcedure> procedures = new LinkedList<>();

    public CalcuManager(Data.ImViewProperty property){
        data.imViewSizes = property;
        CalcuProcedure.setData(data);

    }

    private void resetProcedures(){
        procedures.clear();
        procedures.add(new Registration());
        procedures.add(new MotionEstimator());
        procedures.add(new Fusion());
    }

    public void start(){
        try{
            startCalculation();

            data.copyAllocSuperToBitmap();
            String path = data.saveSuperResImToExternStorage();
            notifySuperImAvai(path);
            notifyOverallProgressChange(1f);
            notifyCurProgressChange(1f);

        }catch (ComputException e){
            listener.onCompuException(e);
            Log.e(LOG_TAG,e.getMessage());
        }
        catch (IOException e){
            listener.ioException(e);
        }
    }

    private void startCalculation()throws ComputException{
        ImCollection images = new ImCollection();
        int count = 1;
        for (Bitmap bitmap:images){
            if (!data.isInited()){
                initData(bitmap);
                GenBaseGray();
                calcBaseImPartial();
                listener.onLowResImAvai(data.getBaseIm());
                continue;
            }


            resetProcedures();
            data.setCurIm(bitmap);


            RsInvokorBase.updateCurImAlloc();
            GenCurGray();

            processCurImage();
            data.getCurIm().recycle();


            count++;
            notifyOverallProgressChange( (float)count / (float)(images.getNum()) );
        }
    }


    private void processCurImage(){
        try{
            rectifySuperRes();
        }catch (ComputException e){
            Log.e(LOG_TAG,"exception occured, current image skipped");
        }
    }

    private void initData(Bitmap bitmap){

        data.init(bitmap);

        RsInvokorBase.setData(data);
        RsInvokorBase.initAllocs();

    }

    private void notifySuperImAvai(String path){
        listener.onSuperResAvail(data.getSuperResImToShow(),path);
    }

    public interface StatusListener{
        void onOverallProgressChanged(int progress);
        void onCurProgressChanged(int progress);
        void onSuperResAvail(Bitmap superRes,String path);
        void ioException(IOException e);
        void onLowResImAvai(Bitmap subIm);
        void onCompuException(ComputException e);
    }

    private StatusListener listener;
    public void setStatusListener(StatusListener listener){
        this.listener = listener;
    }


    private void rectifySuperRes()throws ComputException{


        BlockSplitter splitter = new BlockSplitter(data.getOriImRegion());

        int count = 0;
        for (Rect rect:splitter){
            int xCurBlockIndex = splitter.getCurIndexX();
            int yCurBlockIndex = splitter.getCurIndexY();

            //Log.d(LOG_TAG, "cur block index (x,y)->(" + Integer.toString(xCurBlockIndex)
            //        + "," + Integer.toString(yCurBlockIndex) + ")"
             //       + PrimFieldToStr.toString(rect));
            data.setCurBlockRegion(rect, xCurBlockIndex, yCurBlockIndex);

            conductPRocedures();
            count++;
            notifyCurProgressChange((float)count/(float)splitter.getBlockNum());
        }
    }


    private void notifyCurProgressChange(float ratio){
        listener.onCurProgressChanged((int)(ratio*100));
    }

    private void notifyOverallProgressChange(float ratio){
        listener.onOverallProgressChanged((int) (ratio * 100));
    }

    private void conductPRocedures()throws ComputException{
        for (CalcuProcedure procedure:procedures){
            procedure.conduct();
        }
    }

    private void calcBaseImPartial(){
        RsPartialDerivative partialCalc = new RsPartialDerivative();
        RsPartialDerivative.Parameter parameter = new RsPartialDerivative.Parameter();
        parameter.im = RsInvokorBase.allocGrayBase;
        partialCalc.start(parameter);
    }

    private RsConvertToGray rsToGray = new RsConvertToGray();
    private void GenBaseGray(){
        RsConvertToGray.Parameter parameter = new RsConvertToGray.Parameter();
        parameter.in = RsInvokorBase.allocImBase;
        parameter.out = RsInvokorBase.allocGrayBase;
        rsToGray.start(parameter);
    }

    private void GenCurGray(){
        RsConvertToGray.Parameter parameter= new RsConvertToGray.Parameter();
        parameter.in = RsInvokorBase.allocImCur;
        parameter.out = RsInvokorBase.allocGrayCur;
        rsToGray.start(parameter);
    }
}
