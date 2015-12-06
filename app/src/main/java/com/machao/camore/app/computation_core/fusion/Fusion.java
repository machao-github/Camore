package com.machao.camore.app.computation_core.fusion;


import com.machao.camore.app.computation_core.calcu_manager.CalcuProcedure;
import com.machao.camore.app.computation_core.compu_exception.ComputException;
import com.machao.camore.app.computation_core.rs_invokor.RsFusion;

public class Fusion extends CalcuProcedure{
    RsFusion rsFusion = new RsFusion();
    public Fusion(){

    }

    @Override
    public void conduct() throws ComputException {
        rsFusion.start(null);
    }

    @Override
    public void reset(){

    }
}
