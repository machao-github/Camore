package com.machao.camore.app.computation_core.motion_estimate;

import com.machao.camore.app.computation_core.im_data.Data;
import com.machao.camore.app.computation_core.rs_invokor.RsMotionErr;
import com.machao.camore.app.computation_core.calcu_manager.CalcuProcedure;
import com.machao.camore.app.computation_core.compu_exception.ComputException;

public class MotionEstimator extends CalcuProcedure {
    StepSchedule stepSchedule;
    RsMotionErr motionErrEstimator;

    public MotionEstimator() {
        init();
    }

    private void init() {
        stepSchedule = new StepSchedule(data);
        motionErrEstimator = new RsMotionErr();
    }

    @Override
    public void conduct() throws ComputException {
        try{
            start();
        }catch (ComputException e){
            Data.Block block = data.blocks.getCurBlock();
            block.subPixMotionX = 0;
            block.subPixMotionY = 0;
        }
    }

    private void start()throws ComputException{
        Step curStep = stepSchedule.getInitStep();

        do {

            motionErrEstimator.start(curStep);

            curStep = stepSchedule.getNext();

        } while (!stepSchedule.shouldStop());

        Data.Block block = data.blocks.getCurBlock();
        block.subPixMotionX = curStep.vXBase;
        block.subPixMotionY = curStep.vYBase;
    }

    @Override
    public void reset(){
        stepSchedule.reset();
    }


    static public class Step {
        public float vXBase;
        public float vYBase;

        public float stepSizeX;
        public float stepSizeY;

        public float thresholdEstimate;
        public float thresholdRegulation;

        public double errVx;
        public double errVy;
        public double errBase;

        public void set(Step step){
            vXBase = step.vXBase;
            vYBase = step.vYBase;
            stepSizeX = step.stepSizeX;
            stepSizeY = step.stepSizeY;
            thresholdEstimate = step.thresholdEstimate;
            thresholdRegulation = step.thresholdRegulation;
            errVx = step.errVx;
            errVy = step.errVy;
            errBase = step.errBase;
        }

        public void reset(){
            vXBase = 0;
            vYBase =0;
            stepSizeX = 0;
            stepSizeY =0;
            thresholdEstimate =0;
            thresholdRegulation = 0;
            errVx = 0;
            errVy = 0;
            errBase = 0;
        }
    }

}

