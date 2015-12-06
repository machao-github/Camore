package com.machao.camore.app.computation_core.register;

import com.machao.camore.app.computation_core.compu_exception.ComputException;


public  class StepScale{
    public static final String LOG_TAG = StepScale.class.getName();
    public static final double MAX_SCALE_FACTOR =  1d/16d;
    private double curScaleFactor = MAX_SCALE_FACTOR;//means minimal step size is 1/8 of block width
    private int    curMinStepSize;
    private int    maxStepSize;
    private int    blockEdgeLen;//we use square blocks

    public StepScale(int blockEdgeLen){
        this.blockEdgeLen = blockEdgeLen;
        reset();
    }

    public void reset(){
        int stepSize =  (int)(blockEdgeLen * curScaleFactor);
        curMinStepSize = stepSize > 1 ?stepSize:1;
        maxStepSize = curMinStepSize;
    }

    public void increase()throws ComputException {
        int newMinStepSize = curMinStepSize*2;
        if (newMinStepSize > MAX_SCALE_FACTOR*blockEdgeLen){
            throw  new ComputException("can't increase scale any more");
        }

        curMinStepSize = newMinStepSize;

    }

    public void decrease()throws ComputException{
        int newMinStepSize = (int)(curMinStepSize*0.5);
        if (newMinStepSize < 1){
            if( curMinStepSize > 1){
                curMinStepSize = 1;
            }else {
                throw new ComputException("step scale can no longer be decreased");
            }
        }

        curMinStepSize = newMinStepSize;

    }

    public void minimize(){
        curMinStepSize = 1;
    }

    public int getMinStepSize(){
        return curMinStepSize;
    }

    public boolean couldScaleDecr(){
        return curMinStepSize > 1;
    }

    public boolean couldScaleIncr(){
        return curMinStepSize <  maxStepSize;
    }
    public int getStepRadius(){
        return curMinStepSize;
    }
}