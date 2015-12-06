package com.machao.camore.app.computation_core.calcu_manager;

import com.machao.camore.app.computation_core.compu_exception.ComputException;
import com.machao.camore.app.computation_core.im_data.Data;

abstract public class CalcuProcedure {
    static protected Data data;
    static public void setData(Data data){
        CalcuProcedure.data = data;
    }

    abstract protected void conduct() throws ComputException;
    abstract protected void reset();
}
