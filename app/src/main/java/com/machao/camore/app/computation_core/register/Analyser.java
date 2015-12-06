package com.machao.camore.app.computation_core.register;

import android.os.SystemClock;

import com.machao.camore.app.computation_core.compu_exception.ComputException;
import com.machao.camore.app.computation_core.im_data.Data;
import com.machao.camore.app.computation_core.im_data.BlockSplitter;
import com.machao.camore.app.computation_core.rs_invokor.RsRegisErrEstimator;

import java.util.LinkedList;
import java.util.Random;

public class Analyser {
    public static final String LOG_TAG = Analyser.class.getName();
    public static final int MAX_NUM_STEPS = (BlockSplitter.BLOCK_HEIGHT + BlockSplitter
            .BLOCK_WIDTH) /2;

    static public class StepInfo {
        public double gradienX;
        public double gradientY;
        public double xErr;
        public double yErr;
        public double err;
        public Step step = new Step();
        public void set(StepInfo src){
            this.gradientY = src.gradientY;
            this.gradienX = src.gradienX;
            this.xErr = src.xErr;
            this.yErr = src.yErr;
            this.err = src.err;
            step.set(src.step);
        }

        public void reset(){
            step.reset();
            this.gradientY = 0;
            this.gradienX = 0;
            this.xErr = 0;
            this.yErr = 0;
        }
    }

    public static class Records {
        private double minErr = Double.MAX_VALUE;
        private int indexMinErr;
        private LinkedList<StepInfo> stepInfos = new LinkedList<StepInfo>();

        public boolean isMinimalFound(){
            if (isMinLastOrFirst()){
                return false;
            }

            return true;
        }

        public boolean isMinLastOrFirst(){
            return  (indexMinErr == stepInfos.size() -1)
                    ||(indexMinErr == 0);
        }

        public boolean isStepTaken(Step src){
            for (StepInfo stepInfo : stepInfos){
                if (stepInfo.step.equals(src)){
                    return true;
                }
            }

            return false;
        }

        public StepInfo getStepinfo(Step src){
            for (StepInfo stepinfo:stepInfos){
                if (stepinfo.step.equals(src)){
                    return stepinfo;
                }
            }

            return null;
        }

        public void add(StepInfo newStepInfo){
            StepInfo tmp = new StepInfo();
            tmp.set(newStepInfo);
            stepInfos.add(tmp);
            if (tmp.err <= minErr){
                indexMinErr = stepInfos.size() -1;
                minErr = tmp.err;
            }
        }

        public Step getMinimal(){
            return stepInfos.get(indexMinErr).step;
        }

        public void reset(){
            stepInfos.clear();
            minErr = Double.MAX_VALUE;
            indexMinErr = 0;
        }
    }


    private int nextStepSizeX;
    private int nextStepSizeY;
    private Data data;

    private int numStepsTaken;
    private StepScale  stepScale;
    private Records records;
    private StepInfo curStepInfo = new StepInfo();
    private StepInfo prevStepInfo = new StepInfo();
    private RsRegisErrEstimator estimator;

    public Analyser(Data data,RsRegisErrEstimator rsRegisErrEstimator){
        this.data = data;
        this.estimator = rsRegisErrEstimator;
        records = new Records();
        stepScale = new StepScale(Math.min(BlockSplitter.BLOCK_HEIGHT, BlockSplitter.BLOCK_WIDTH));
    }

    public void reset(){
        nextStepSizeX = 0;
        nextStepSizeY = 0;
        numStepsTaken = 0;
        stepScale.reset();
        records.reset();
        curStepInfo.reset();
        prevStepInfo.reset();
    }

    public void start()throws ComputException{
        reset();
        useIteration();
        //useGraduation();
    }

    private void useIteration()throws ComputException{
        int basex = 0;int basey = 0;
        int scope = 8;//2;
        int scale = 4;//64;


        for (;scale > 0 ;scale = scale/2){

            for (int i = -(scope -1); i<scope; i++){
                for (int j = -(scope-1); j<scope; j++){

                    int offsetx = basex + i*scale;
                    int offsety = basey + j*scale;

                    StepInfo stepInfo = new StepInfo();
                    stepInfo.step.set(offsetx, offsety);
                    if (records.isStepTaken(stepInfo.step)){
                        continue;
                    }

                    stepInfo.err = getErr(stepInfo.step);
                    records.add(stepInfo);

                }

                //scope -= scale;
            }


            Step min = records.getMinimal();
            basex = min.offsetX;
            basey = min.offsetY;
        }
    }


private void useGraduation()throws ComputException{
        genInitStep();


        while ((!needStop())
                && numStepsTaken < MAX_NUM_STEPS){



            Step xPartial = curStepInfo.step.getPartialXStep(data, stepScale.getMinStepSize());
            curStepInfo.xErr = getErr(xPartial);

            Step yPartial = curStepInfo.step.getPartialYStep(data, stepScale.getMinStepSize());
            curStepInfo.yErr = getErr(yPartial);

            curStepInfo.err = getErr(curStepInfo.step);


            records.add(curStepInfo);

            genNextStep();

            chooseStippestRoute();

            setSwapStepCurPrev();

            adjustScale();

            addCurResultToDetector();

            numStepsTaken++;
        }

    }

    private void genInitStep()throws ComputException{
        Data.Block neighbour = data.blocks.getCurNeighbour();
        if (neighbour != null){
            stepScale.minimize();
            curStepInfo.step.set(prevStepInfo.step, neighbour.xTranslation,
                    neighbour.yTranslation);

        }
        curStepInfo.step.set(prevStepInfo.step, 0, 0);
    }

    private double getErr(Step step){
        StepInfo stepInfo = new StepInfo();
        stepInfo.step.set(step);

        StepInfo stored = records.getStepinfo(stepInfo.step);
        if (stored != null){
            return stored.err;
        }

        estimator.start(stepInfo);
        return stepInfo.err;
    }

    private double getNeighbFourErr(Step step)throws ComputException{
        double error = 0;
        LinkedList<Step> neighbours = step.getStepNeighb4(data, step);
        neighbours.add(step);
        for (Step stepInfo :neighbours){
            error += getErr(stepInfo)/neighbours.size();
        }

        return error;
    }

    private void adjustScale()throws ComputException{
        if ( (curStepInfo.err > prevStepInfo.err
                || records.isStepTaken(curStepInfo.step))
                && stepScale.couldScaleDecr()){

            if (stepScale.couldScaleDecr()){
                decreaseScale();
            }else {
                markedCurStepAsStop();
                return;
            }

        }

        if ( records.isMinimalFound() ) {
            if (!stepScale.couldScaleDecr()){
                markedCurStepAsStop();
                return;
            }else {
                decreaseScale();
            }
        }
    }



    /*
      when a step is taken already and the step is not minimal of
      that scale ,random jump should be added to avoid meaninglessly
      estimating of previously taken steps
    */
    private void randomJumpCurStep(){
        Random random = new Random();
        random.setSeed(SystemClock.currentThreadTimeMillis());
        int xJumpFactor = random.nextInt()%(2*stepScale.getMinStepSize());
        int yJumpFactor = random.nextInt()%(2*stepScale.getMinStepSize());
        curStepInfo.step.offsetX += xJumpFactor;
        curStepInfo.step.offsetY += yJumpFactor;
    }

    private Step markedCurStepAsStop(){
        curStepInfo.step.setAsNeedStop();
        return curStepInfo.step;
    }

    private void decreaseScale()throws ComputException{

        records.reset();
        stepScale.decrease();

    }

    private void increaseScale()throws ComputException{
        if (!stepScale.couldScaleIncr()){
            throw new ComputException("step scale could no longer be increased");
        }
        records.reset();
        stepScale.increase();
    }


    private void addCurResultToDetector(){
        StepInfo stepInfoToAdd = new StepInfo();
        stepInfoToAdd.set(curStepInfo);
        records.add(stepInfoToAdd);
    }

    private void setSwapStepCurPrev()throws ComputException{
        prevStepInfo.set(curStepInfo);
        curStepInfo.step.set(prevStepInfo.step, nextStepSizeX, nextStepSizeY);
        //Log.d(LOG_TAG, PrimFieldToStr.toString(curStepInfo.step));
    }

    private void genNextStep()throws ComputException{
        if (curStepInfo.step.getPartialYStepSize() != 0){
            curStepInfo.gradienX =(curStepInfo.xErr - curStepInfo.err) /
                    Math.abs(curStepInfo.step.getPartialXStepSize());
        }else {
            curStepInfo.gradienX = 0;
        }

        if (curStepInfo.step.getPartialYStepSize() != 0){
            curStepInfo.gradientY = (curStepInfo.yErr - curStepInfo.err) /
                    Math.abs(curStepInfo.step.getPartialYStepSize());
        }
        else {
            curStepInfo.gradientY = 0;
        }


        int xSign = (int) (Math.signum((float) curStepInfo.step.getPartialXStepSize())
                *(-1)*Math.signum(curStepInfo.gradienX) );

        int ySign = (int) (Math.signum((float) curStepInfo.step.getPartialYStepSize())
                * (-1)*Math.signum(curStepInfo.gradientY) );

        double base = stepScale.getMinStepSize()/Math.sqrt(Math.pow(curStepInfo.gradienX,2d)
                + Math.pow(curStepInfo.gradientY,2));

        double xVal = base * Math.abs(curStepInfo.gradienX) ;
        double yVal = base * Math.abs(curStepInfo.gradientY);

        nextStepSizeX = (xVal < 1 && xVal > 0)?1:(int)xVal;
        nextStepSizeY = (yVal < 1 && yVal > 0)?1:(int)yVal;

        nextStepSizeX *= xSign;
        nextStepSizeY *= ySign;
    }

    /*it happens that  when partial derivative in both x and y direction are non-zero
       values ,the value of f(x,y) does not change most rapidly in a direction that lies
       between x and y due to properties of discrete signals . it has to be made clear
       whether the scene value occurs on x axis or maybe y axis .
    */
    private void chooseStippestRoute()throws ComputException{
        Step newStep = new Step();
        newStep.set(curStepInfo.step, nextStepSizeX, nextStepSizeY);
        StepInfo newStepInfo= new StepInfo();
        newStepInfo.step.set(newStep);

        estimator.start(newStepInfo);
        if ( newStepInfo.err > curStepInfo.xErr || newStepInfo.err > curStepInfo.yErr){
            if (curStepInfo.xErr < curStepInfo.yErr){
                nextStepSizeY = 0;
            }else {
                nextStepSizeX = 0;
            }
        }
    }

    private boolean needStop(){
        if (records.isMinimalFound()
                && (!stepScale.couldScaleDecr())
                ){
            return true;
        }
        return false;
    }

    public Step getEstimate(){
        return records.getMinimal();
    }

}
