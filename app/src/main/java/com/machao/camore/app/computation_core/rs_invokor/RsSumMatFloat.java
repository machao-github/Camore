package com.machao.camore.app.computation_core.rs_invokor;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Script;
import android.renderscript.Type;

import com.machao.camore.app.computation_core.ScriptC_sum_row_mat_float;


public class RsSumMatFloat extends RsInvokorBase{
    ScriptC_sum_row_mat_float rsSum;
    Script.LaunchOptions opt = new Script.LaunchOptions();
    Allocation out;
    Parameter parameter;
    public RsSumMatFloat(){
        init();
    }


    protected void init(){
        rsSum = new ScriptC_sum_row_mat_float(rs);
    }

    public void start(Object obj){
        /*
        Allocation alloc = parameter.allocIn;
        float[] arr = new float[alloc.getType().getX()*alloc.getType().getY()];
        alloc.copyTo(arr);
        parameter.sum = 0;
        for (float f :arr){
            parameter.sum += f;
        }

        return;*/

        parameter = (Parameter)obj;
        width = parameter.allocIn.getType().getX();
        height = parameter.allocIn.getType().getY();
        config();
        runRs();

    }

    int width;
    int height;
    protected void config(){

        opt.setX(0,height);

        Type.Builder builder = new Type.Builder(rs, Element.F32(rs));
        builder.setX(height);
        out = Allocation.createTyped(rs,builder.create());

        rsSum.set_g_x_start(0);
        rsSum.set_g_x_end(width);
        rsSum.set_g_src(parameter.allocIn);
    }

    private void runRs(){
        rsSum.forEach_sum_row_float(out, out, opt);

        parameter.sum = handleOutAlloc();
    }

    private float handleOutAlloc(){
        float[] sumRow = new float[height];
        out.copyTo(sumRow);
        float sum = 0;
        for (float f:sumRow){
            //Log.d("sum row ",Float.toString(f));
            sum += f;
        }

        return sum;
    }


    public static class Parameter{
        Allocation allocIn;
        float sum;
    }
}
