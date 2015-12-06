package com.machao.camore.app.computation_core.rs_invokor;

import android.graphics.Rect;
import android.renderscript.Allocation;
import android.renderscript.Script;

import com.machao.camore.app.computation_core.ScriptC_partial_deriv_sec_x;
import com.machao.camore.app.computation_core.ScriptC_partial_deriv_sec_y;
import com.machao.camore.app.computation_core.ScriptC_partial_derivative_x;
import com.machao.camore.app.computation_core.ScriptC_partial_derivative_y;

public class RsPartialDerivative extends RsInvokorBase{
    private Parameter para;


    private ScriptC_partial_derivative_x rsPartialX;
    private ScriptC_partial_derivative_y rsPartialY;
    private ScriptC_partial_deriv_sec_x rsSecPartialX;
    private ScriptC_partial_deriv_sec_y rsSecPartialY;

    public RsPartialDerivative(){
        init();
    }


    Rect validRegion;

    protected void init(){
        validRegion = data.getOriValidRegion();
        rsPartialX = new ScriptC_partial_derivative_x(rs);
        rsPartialY = new ScriptC_partial_derivative_y(rs);
        rsSecPartialX = new ScriptC_partial_deriv_sec_x(rs);
        rsSecPartialY = new ScriptC_partial_deriv_sec_y(rs);
    }

    @Override
    public void start(Object obj){
        this.para = (Parameter)obj;
        config();

        calcX();
        calcY();

        calcSecX();
        calcSecY();
    }


    protected void config(){
        configX();
        configY();
        configSecX();
        configSecY();
    }



    private void calcX(){
        rsPartialX.forEach_partial_x(allocBasePartialX, allocBasePartialX, createOpt());

    }

    private void calcY(){
        rsPartialY.forEach_partial_y(allocBasePartialY, allocBasePartialY, createOpt());
    }

    private void calcSecX(){
        rsSecPartialX.forEach_sec_partial_x(allocBaseSecPartialX, allocBaseSecPartialX, createOpt());
    }

    private void calcSecY(){
        rsSecPartialY.forEach_sec_partial_y(allocBaseSecPartialY, allocBaseSecPartialY, createOpt());
    }

    private void configSecX(){
        rsSecPartialX.set_g_partial_x(allocBasePartialX);
        rsSecPartialX.set_g_x_max(data.getOriWidth()-1);
        rsSecPartialX.set_g_y_max(data.getOriHeight()-1);
    }

    private void configSecY(){
        rsSecPartialY.set_g_partial_y(allocBasePartialY);
        rsSecPartialY.set_g_x_max(data.getOriWidth()-1);
        rsSecPartialY.set_g_y_max(data.getOriHeight()-1);
    }

    private Script.LaunchOptions createOpt(){
        Script.LaunchOptions options = new Script.LaunchOptions();
        options.setX(0,data.getOriWidth() );
        options.setY(0,data.getOriHeight() );
        return options;
    }

    private void configX(){
        rsPartialX.set_g_im_gray(para.im);
        rsPartialX.set_g_x_max(data.getOriWidth()-1);
        rsPartialX.set_g_y_max(data.getOriHeight()-1);
    }

    private void configY(){
        rsPartialY.set_g_im_gray(para.im);
        rsPartialY.set_g_x_max(data.getOriWidth()-1);
        rsPartialY.set_g_y_max(data.getOriHeight()-1);
    }


    static public class Parameter{
        public Allocation im;
    }
}

