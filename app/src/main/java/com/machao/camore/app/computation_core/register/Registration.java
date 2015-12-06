package com.machao.camore.app.computation_core.register;

import com.machao.camore.app.computation_core.calcu_manager.CalcuProcedure;
import com.machao.camore.app.computation_core.compu_exception.ComputException;
import com.machao.camore.app.computation_core.im_data.Data;
import com.machao.camore.app.computation_core.rs_invokor.RsRegisErrEstimator;

public class Registration extends CalcuProcedure{
    public static final String LOG_TAG = "Registration ";
    private RsRegisErrEstimator rsRegisErrEstimator;
    private Analyser analyser;

    public Registration(){
        rsRegisErrEstimator = new RsRegisErrEstimator();
        analyser = new Analyser(data, rsRegisErrEstimator);
    }

    @Override
    public void conduct() throws ComputException{

        analyser.start();
        Step estimate = analyser.getEstimate();
        Data.Block block = data.blocks.getCurBlock();
        block.xTranslation = estimate.offsetX;
        block.yTranslation = estimate.offsetY;
    }

    @Override
    public void reset(){
        analyser.reset();
    }


}
