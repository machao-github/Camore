package com.machao.camore.app.computation_core.rs_invokor;

import android.graphics.Rect;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Script;
import android.renderscript.Type;

import com.machao.camore.app.computation_core.register.Analyser;
import com.machao.camore.app.computation_core.register.Step;
import com.machao.camore.app.computation_core.ScriptC_sum_substract;

import java.util.LinkedList;


public class RsRegisErrEstimator extends RsInvokorBase {
    private ScriptC_sum_substract substRs;
    private Script.LaunchOptions opt;
    private Allocation outAlloc;

    Step step = new Step();
    double err;

    public RsRegisErrEstimator(){

        init();
    }


    private void init(){

        substRs = new ScriptC_sum_substract(rs);
    }

    private Rect rect;
    private void config(Rect rect){
        //x coordinate refers to index in row , index in column is not used
        opt = new Script.LaunchOptions();

        substRs.set_g_offsetX(step.offsetX);
        substRs.set_g_offsetY(step.offsetY);
        substRs.set_g_x_ind_max(data.getOriWidth() - 1);
        substRs.set_g_y_ind_max(data.getOriHeight() - 1);

        substRs.set_g_imBase(allocGrayBase);
        substRs.set_g_imCur(allocGrayCur);
        this.rect = rect;
        setIndexes();
        createOutAlloc();
    }

    private void setIndexes(){
        //the upper end parameter passed to opt is not reached (
        opt.setX(0, rect.height()+1);

        substRs.set_g_colIndexStart(rect.left);
        substRs.set_g_colIndexEnd(rect.right);
        substRs.set_g_rowIndexStart(rect.top);
        substRs.set_g_rowIndexEnd(rect.bottom);
    }

    @Override
    public void start(Object obj){
        Analyser.StepInfo stepInfo = (Analyser.StepInfo)obj;
        this.step.set(stepInfo.step);

        LinkedList<Rect> subRects = step.getSubRects(data);
        stepInfo.err = 0;
        for (Rect subRect:subRects){
            config(subRect);
            runRs();
            handleOutAlloc();
            stepInfo.err += err;
        }

        stepInfo.err = stepInfo.err/subRects.size();
    }


    private void createOutAlloc(){
        outAlloc = Allocation.createTyped(rs,getTypeOutAlloc());
    }

    private Type getTypeOutAlloc(){
        Type.Builder allocConfig = new Type.Builder(rs,
                Element.F32(rs));
        allocConfig.setX(rect.height()+1);
        return allocConfig.create();
    }

    private void runRs(){
        substRs.forEach_sum_substract_row(outAlloc, outAlloc,opt);

    }

    private void handleOutAlloc(){
        int length = rect.height()+1;
        err = getErr(outAlloc, length);
    }

    private double getErr(Allocation alloc,int length){
        float[] errArr = new float[length];
        alloc.copyTo(errArr);

        double errSum = 0;
        for (double err:errArr){
            errSum += err;
        }

        return  errSum/length;
    }
}