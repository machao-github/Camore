package com.machao.camore.app.computation_core.rs_invokor;

import android.graphics.Rect;
import android.renderscript.Script;

import com.machao.camore.app.computation_core.im_data.Data;
import com.machao.camore.app.computation_core.ScriptC_fusion;

public class RsFusion extends RsInvokorBase {
    private static final String LOG_TAG = "rs fusion";
    ScriptC_fusion rsFusion;
    Rect superImRegion;
    Script.LaunchOptions opt;
    Data.Block curBlock;

    public RsFusion(){
        init();
    }


    public void init(){
        rsFusion = new ScriptC_fusion(rs);
    }

    public void start(Object obj){
        config();
        rsFusion.forEach_fusion(allocSuperRes,allocSuperRes,opt);
    }

    public void config(){
        curBlock = data.blocks.getCurBlock();
        superImRegion = curBlock.getCorrespBlockSuper();
        opt = new Script.LaunchOptions();
        opt.setX(superImRegion.left,superImRegion.right );
        opt.setY(superImRegion.top, superImRegion.bottom );

        rsFusion.set_g_xl_ind_max(data.getOriWidth());
        rsFusion.set_g_yl_ind_max(data.getOriHeight());

        rsFusion.set_low_res(allocImCur);
        rsFusion.set_super_res(data.getAllocSuper());
        rsFusion.set_g_xl_pix_offset(curBlock.xTranslation);
        rsFusion.set_g_yl_pix_offset(curBlock.yTranslation);
        rsFusion.set_g_xl_subpix_offset(curBlock.subPixMotionX);
        rsFusion.set_g_yl_subpix_offset(curBlock.subPixMotionY);

    }



}
