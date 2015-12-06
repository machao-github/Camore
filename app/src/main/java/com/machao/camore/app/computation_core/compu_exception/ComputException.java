package com.machao.camore.app.computation_core.compu_exception;

public class ComputException extends Exception{
    String msg;
    public ComputException(String msg){
        super(msg);
        this.msg = msg;
    }

    public String getMsg(){
        return msg;
    }
}
