package com.machao.camore.app.computation_core.motion_estimate;

import android.util.Log;

import com.machao.camore.app.computation_core.im_data.Data;
import com.machao.camore.app.computation_core.compu_exception.ComputException;
import com.machao.camore.app.computation_core.im_data.BlockSplitter;
import com.machao.camore.app.computation_core.im_data.PrimFieldToStr;

class StepSchedule {
    private static final int ESTIMATE_THRESHOLD_INIT_VAL = 750*750;
    private static final float lamdaEstimate = 1;
    private static final float lamdaRegulation = 10;
    private static final float MIN_STEP_SIZE = 0.05f;
    private static final float INIT_STEP_SIZE = 0.1F;
    private static final float THRESH_REGU_INIT = 0.01f;
    private static final float THRESH_SHRINK_FACTOR = 0.95F;



    Data data;
    MotionEstimator.Step curStep = new MotionEstimator.Step();
    MotionEstimator.Step prevStep = new MotionEstimator.Step();
    int numSetpsTaken =1;


    public StepSchedule(Data data){
        this.data = data;
    }

    public void reset(){
        curStep.reset();
        prevStep.reset();
        numSetpsTaken = 1;
    }

    public MotionEstimator.Step getInitStep(){
        curStep.reset();
        prevStep.reset();
        numSetpsTaken = 1;
        curStep.stepSizeX = INIT_STEP_SIZE;
        curStep.stepSizeY = INIT_STEP_SIZE;
        curStep.thresholdEstimate = ESTIMATE_THRESHOLD_INIT_VAL;
        curStep.thresholdRegulation = THRESH_REGU_INIT;
        setCurStepInitMotion();
        return curStep;
    }

    private void setCurStepInitMotion(){
        Data.Block neighbour = data.blocks.getCurNeighbour();
        if (neighbour == null){
            return;
        }

        curStep.vXBase = neighbour.subPixMotionX;
        curStep.vYBase = neighbour.subPixMotionY;
    }

    public MotionEstimator.Step getNext()throws ComputException{

        prevStep.set(curStep);
        curStep.reset();

       // GenCurStepThreshold();
        curStep.thresholdEstimate = prevStep.thresholdEstimate;
        curStep.thresholdRegulation = prevStep.thresholdRegulation;


        double mju = Math.cos(Math.PI /(numSetpsTaken + 1.5d));
        double mjuSquare = Math.pow(mju, 2);
        double omiga = 2*(1- Math.sqrt(1 - Math.pow(mjuSquare, 2)))/mjuSquare;

        double factorT = ESTIMATE_THRESHOLD_INIT_VAL *lamdaEstimate/Math.pow(prevStep.thresholdEstimate,2)
                + lamdaRegulation/Math.pow(prevStep.thresholdRegulation,2);

        double stepFactor = omiga/factorT;

        double deltax = - (stepFactor)*(prevStep.errVx - prevStep.errBase);
        double deltay = - (stepFactor)*(prevStep.errVy - prevStep.errBase);

        curStep.vXBase = (float)(prevStep.vXBase + deltax);
        curStep.vYBase = (float)(prevStep.vYBase + deltay);

        curStep.stepSizeX = (float)deltax/4;
        curStep.stepSizeY = (float)deltay/4;


        if (Float.isNaN(curStep.stepSizeX)
                ||Float.isNaN(curStep.stepSizeY)
                ||Float.isNaN(curStep.vXBase)
                ||Float.isNaN(curStep.vYBase)
                ||Double.isNaN(curStep.errBase)
                ||Double.isNaN(curStep.errVx)
                ||Double.isNaN(curStep.errVy)
                ||Math.abs(curStep.stepSizeX + curStep.vXBase) > 1
                || Math.abs(curStep.stepSizeY + curStep.vYBase) > 1){
            Log.e("invalid threshold","prev step:"+ PrimFieldToStr.toString(prevStep) +
                    "cur step:" + PrimFieldToStr.toString(curStep));
            throw new ComputException("motion estimate does not converge");
        }

        numSetpsTaken++;
        return  curStep;
    }


    private void GenCurStepThreshold()throws ComputException{

        /*
        curStep.thresholdRegulation = THRESH_SHRINK_FACTOR*prevStep.thresholdRegulation;
        curStep.thresholdEstimate = 2*ESTIMATE_THRESHOLD_INIT_VAL*(
                Math.abs(prevStep.vXBase) + Math.abs(prevStep.stepSizeX)
                        + Math.abs(prevStep.vYBase) + Math.abs(prevStep.stepSizeY) );

        if (curStep.thresholdEstimate == 0 || curStep.thresholdRegulation == 0
                ||Float.isNaN(curStep.thresholdEstimate) || Float.isNaN(curStep
                .thresholdRegulation) ){
            Log.e("invalid threshold", PrimFieldToStr.toString(prevStep) + PrimFieldToStr
                    .toString(curStep));
            throw  new ComputException("failed to generate valid threshold");
        }
        */
    }

    public boolean shouldStop(){
        if ( numSetpsTaken > BlockSplitter.BLOCK_WIDTH
                || isStepSizeTooSmall() ){
            return true;
        }
        return false;
    }

    private boolean isStepSizeTooSmall(){
        return curStep.stepSizeX < MIN_STEP_SIZE
                && curStep.stepSizeY < MIN_STEP_SIZE;
    }
}