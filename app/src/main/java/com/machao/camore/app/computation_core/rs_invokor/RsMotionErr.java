package com.machao.camore.app.computation_core.rs_invokor;

import android.graphics.Rect;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Script;
import android.renderscript.Type;

import com.machao.camore.app.computation_core.im_data.Data;
import com.machao.camore.app.computation_core.motion_estimate.MotionEstimator;
import com.machao.camore.app.computation_core.ScriptC_estimate_motion_err;


public class RsMotionErr extends RsInvokorBase{
    private static final String LOG_TAG = "estimated motion";
    private MotionEstimator.Step step;
    private ScriptC_estimate_motion_err rsMotionErr;
    private Data.Block block;
    private Rect curRect;
    private Script.LaunchOptions opt = new Script.LaunchOptions();
    private Allocation outErr;

    private RsSumMatFloat sumMatFloat;
    RsSumMatFloat.Parameter parameterSum = new RsSumMatFloat.Parameter();
    public RsMotionErr(){
        init();
    }


    protected void init(){
        rsMotionErr = new ScriptC_estimate_motion_err(rs);
        sumMatFloat = new RsSumMatFloat();
    }

    @Override
    public void start(Object obj){
        step = (MotionEstimator.Step)obj;
        config();

        estimateBase();
        estimatePartialX();
        estimatePartialY();

        //Log.d(LOG_TAG, PrimFieldToStr.toString(step));
    }

    private void estimatePartialX(){
        rsMotionErr.set_g_vx(step.vXBase + step.stepSizeX);
        rsMotionErr.set_g_vy(step.vYBase);

        runRs();

        step.errVx = parameterSum.sum;
    }

    private void estimatePartialY(){
        rsMotionErr.set_g_vx(step.vXBase);
        rsMotionErr.set_g_vy(step.vYBase + step.stepSizeY);
        runRs();
        step.errVy = parameterSum.sum;
    }

    private void estimateBase(){
        rsMotionErr.set_g_vx(step.vXBase + step.stepSizeX);
        rsMotionErr.set_g_vy(step.vYBase + step.stepSizeY);
        runRs();
        step.errBase = parameterSum.sum;
    }

    private void runRs(){
        rsMotionErr.forEach_estimate_motion_err(outErr, outErr, opt);

        parameterSum.allocIn = outErr;

        sumMatFloat.start(parameterSum);
    }


    protected void config(){
        block = data.blocks.getCurBlock();
        curRect = block.region;

        createAllocOutEstimateErr();

        opt.setX(0, curRect.width() + 1);
        opt.setY(0, curRect.height() + 1);

        rsMotionErr.set_g_estimate_threshold(step.thresholdEstimate);
        rsMotionErr.set_g_regulation_threshold(step.thresholdRegulation);
        rsMotionErr.set_g_base_im(allocGrayBase);
        rsMotionErr.set_g_cur_im(allocGrayCur);
        rsMotionErr.set_g_baseim_partial_x(allocBasePartialX);
        rsMotionErr.set_g_baseim_partial_y(allocBasePartialY);
        rsMotionErr.set_g_baseim_sec_partial_xx(allocBaseSecPartialX);
        rsMotionErr.set_g_baseim_sec_partial_yy(allocBaseSecPartialY);
        rsMotionErr.set_g_block_ind_left(curRect.left);
        rsMotionErr.set_g_block_ind_right(curRect.right);
        rsMotionErr.set_g_block_ind_top(curRect.top);
        rsMotionErr.set_g_block_ind_bot(curRect.bottom);

        rsMotionErr.set_g_x_ind_max(data.getOriWidth() -1);
        rsMotionErr.set_g_y_ind_max(data.getOriHeight() -1);
        rsMotionErr.set_g_offset_x(block.xTranslation);
        rsMotionErr.set_g_offset_y(block.yTranslation);

    }

    private void createAllocOutEstimateErr(){
        Type.Builder builder = new Type.Builder(rs, Element.F32(rs));
        builder.setX(curRect.width() + 1);
        builder.setY(curRect.height()+1);
        Type type = builder.create();
        outErr = Allocation.createTyped(rs,type);
    }



}