package com.machao.camore.app.computation_core.register;

import android.graphics.Rect;

import com.machao.camore.app.computation_core.im_data.BlockSplitter;
import com.machao.camore.app.computation_core.im_data.Data;
import com.machao.camore.app.computation_core.compu_exception.ComputException;

import java.util.LinkedList;


public class Step {

    public static final int MAX_OFFSETX_ABS = BlockSplitter.BLOCK_WIDTH *2/3;
    public static final int MAX_OFFSETY_ABS = BlockSplitter.BLOCK_HEIGHT*2/3;
    /*
    the assumed corresponding pixel index in the base is (offsetX + indexCurImageX,
    offsetY + indexCurImageY)
     */
    public int offsetX;
    public int offsetY;


    public int stepSizeY;
    public int stepSizeX;




    public void set(Step dest){
        offsetX =  dest.offsetX;
        offsetY = dest.offsetY;
        stepSizeY = dest.stepSizeY;
        stepSizeX = dest.stepSizeX;

    }

    public void reset(){
        offsetX =  0;
        offsetY = 0;
        stepSizeY = 0;
        stepSizeX = 0;
    }

    public boolean equals(Step src){
        if (src.offsetX == this.offsetX
                && src.offsetY == this.offsetY){
            return true;
        }

        return false;
    }

    public void set(Step prev,int stepSizeX, int stepSizeY) throws ComputException {

        this.stepSizeY = stepSizeY;
        this.stepSizeX = stepSizeX;

        offsetX = prev.offsetX + stepSizeX;
        offsetY = prev.offsetY + stepSizeY;

        validateStepSize(this);

    }

    public void set(int stepSizeX, int stepSizeY){
        this.offsetX = stepSizeX;
        this.offsetY = stepSizeY;
    }

    private int partialXStepSize;
    private int partialYStepSize;
    public Step getPartialXStep(Data data,int stepSize)throws ComputException{
        Step partialX = new Step();
        this.partialXStepSize = stepSize;
        partialX.stepSizeY = stepSize;
        partialX.stepSizeX = 0;

        partialX.offsetX = this.offsetX + stepSize;
        partialX.offsetY = this.offsetY;

        validateStepSize(partialX);

        return partialX;

    }

    public Step getPartialYStep(Data data,int stepSize)throws ComputException{
        Step partialY = new Step();
        partialYStepSize = stepSize;
        partialY.stepSizeX = stepSize;
        partialY.stepSizeY = 0;

        partialY.offsetY = this.offsetY + stepSize;
        partialY.offsetX = this.offsetX;

        validateStepSize(partialY);

        return partialY;

    }

    public LinkedList<Step> getStepNeighb4(Data data,Step step)throws ComputException{
        LinkedList<Step> neighbours = new LinkedList<>();
        for (int i = -1 ;i<2;i++){
            for (int j = -1; j< 2 ;j++){
                if (i == 0 || j== 0){
                    continue;
                }
                Step neighbour = new Step();

                neighbour.offsetX = neighbour.offsetX + i;
                neighbour.offsetY = neighbour.offsetY + j;
                validateStepSize(neighbour);
                neighbours.add(neighbour);
            }
        }

        return neighbours;
    }


    public int getPartialXStepSize(){
        return partialXStepSize;
    }

    public int getPartialYStepSize(){
        return partialYStepSize;
    }



    private static float subRectRatios5[][] = {
        //ratio xStart,yStart,xEnd,yEnd
        {0.125f     ,0.125f     ,0.375f  ,0.375f },
        {0.625f ,0.125f     ,0.875f     ,0.375f },
        {0.375f,0.375f,0.625f ,0.625f},
        {0.125f     ,0.625f ,0.375f  ,0.875f    },
        {0.625f ,0.625f ,0.875f     ,0.875f    } };

    private static float subRectRatios5Smaller[][]= {
            //ratio xStart,yStart,xEnd,yEnd
            {0     ,0     ,0.1f  ,0.1f },
            {0.9f  ,0     ,1f    ,0.1f },
            {0.45f ,0.45f ,0.55f ,0.55f},
            {0     ,0.9f  ,0.1f  ,1f    },
            {0.9f  ,0.9f  ,1f    ,1f    } };

    private static float subRectRatios1[][] = {
            {0.3f     ,0.3f     ,0.7f  ,0.7f }
    };

    private static float subRectRatios1Smaller[][] = {
            {0.4f     ,0.4f     ,0.6f  ,0.6f }
    };

    private float[][] subRectRatios = subRectRatios5;


    /*
   offset here is used to get corresponding index in base image, indexCur + offset
   equals indexBase for offset > 0, cur image is shifted toward left or top side
   compared with base image
    */
    public LinkedList<Rect> getSubRects( Data data){
        LinkedList<Rect> rects = new LinkedList<>();
        Rect curBlockRegion = data.blocks.getCurBlock().region;
        Rect oriValidRegion = data.getOriValidRegion();

        for (int i = 0;i<subRectRatios.length;i++){
            try{
                float [] ratios = subRectRatios[i];
                Rect rectForShift = new Rect();
                rectForShift.left = (int)(ratios[0]*curBlockRegion.left);
                rectForShift.right = (int)(ratios[2]*curBlockRegion.right);
                rectForShift.top = (int)(ratios[1]*curBlockRegion.top);
                rectForShift.bottom = (int)(ratios[3]*curBlockRegion.bottom);
                makeIndValid(rectForShift,curBlockRegion,oriValidRegion,offsetX,offsetY);

                if (rectForShift.width() > 1 && rectForShift.height() > 1){
                    rects.add(rectForShift);
                }
            }catch (ComputException e){

            }

        }
        return rects;
    }

    private void makeIndValid(Rect rectForShift,Rect curBlockRegion,Rect oriValidRegion
                            ,int offsetX,int offsetY)throws ComputException{
        if (offsetX >= 0){
            if(curBlockRegion.right + offsetX > oriValidRegion.right) {
                rectForShift.right = oriValidRegion.right - offsetX;
            }

        }else {
            if (curBlockRegion.left + offsetX < oriValidRegion.left){
                rectForShift.left = oriValidRegion.left - offsetX;
            }
        }

        if (offsetY >= 0){
            if (curBlockRegion.bottom + offsetY > oriValidRegion.bottom){
                rectForShift.bottom = oriValidRegion.bottom - offsetY;
            }
        }else{
            if (curBlockRegion.top + offsetY < oriValidRegion.top) {
                rectForShift.top = oriValidRegion.top - offsetY;
            }
        }

        if (!oriValidRegion.contains(rectForShift)
                || rectForShift.left >= rectForShift.right
                ||rectForShift.top >= rectForShift.bottom){
            throw new ComputException("invalid index");
        }
    }

    static private void validateStepSize(Step config)throws ComputException{
        if (Math.abs(config.offsetX) > MAX_OFFSETX_ABS
                || Math.abs(config.offsetY) > MAX_OFFSETY_ABS){
            throw new ComputException("step size way to big ");
        }
    }

    public void setAsNeedStop(){
        stepSizeY = 0;
        stepSizeX = 0;
    }



    public boolean shouldStop(){
        return (stepSizeY == 0) && (stepSizeX == 0);
    }

}
