package com.machao.camore.app.computation_core.rs_invokor;

import android.renderscript.Allocation;

import com.machao.camore.app.computation_core.ScriptC_to_gray;

public class RsConvertToGray extends RsInvokorBase {

    private Parameter parameter ;
    private ScriptC_to_gray rsConvert ;

    public RsConvertToGray(){
        init();
    }

    protected void init(){
        rsConvert =  new ScriptC_to_gray(rs);
    }

    public void start(Object obj){
        parameter = (Parameter)obj;
        config();
        rsConvert.forEach_convert(parameter.out,parameter.out);
    }

    protected void config(){
        rsConvert.set_g_im(parameter.in);
    }

    static public class Parameter{
        public Allocation in;
        public Allocation out;
    }
}
