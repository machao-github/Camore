package com.machao.camore.app.computation_core.rs_invokor;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;

import com.machao.camore.app.computation_core.im_data.Data;
import com.machao.camore.app.application.activities.CamoreApp;


abstract public class RsInvokorBase {

    static protected RenderScript rs;
    static protected Data data;
    static public Allocation allocImBase;
    static public Allocation allocImCur;
    static public Allocation allocBasePartialX;
    static public Allocation allocBasePartialY;
    static public Allocation allocBaseSecPartialX;
    static public Allocation allocBaseSecPartialY;
    static public Allocation allocSuperRes;
    static public Allocation allocGrayBase;
    static public Allocation allocGrayCur;

    static {
        rs = RenderScript.create(CamoreApp.getAppContext());
    }

    static public void reset(){
         data = null;
         allocImBase = null;
         allocImCur = null;
         allocBasePartialX = null;
         allocBasePartialY = null;
         allocBaseSecPartialX = null;
         allocBaseSecPartialY = null;
         allocGrayCur = null;
         allocGrayBase = null;
         allocSuperRes = null;
    }



    static  public void updateCurImAlloc(){
        allocImCur = Allocation.createFromBitmap(rs,data.getCurIm());
    }

    static  public void initAllocs(){
        allocImBase = Allocation.createFromBitmap(rs, data.getBaseIm());

        allocGray();
        allocPartial();
        allocSecPartial();
        allocSuperRes();
    }

    static  private void allocSuperRes(){
        allocSuperRes = Allocation.createFromBitmap(rs,data.getSuperResIm());
        /*
        Element element= allocImBase.getElement();
        Type.Builder builder = new Type.Builder(rs,element);
        builder.setX(data.getSupImWidth());
        builder.setY(data.getSupImHeight());
        Type type = builder.create();
        allocSuperRes = Allocation.createTyped(rs,type);
        */
    }

    static  private void allocGray(){
        Type.Builder builder = new Type.Builder(rs,Element.I32(rs));
        builder.setX(data.getOriWidth());
        builder.setY(data.getOriHeight());

        allocGrayBase = Allocation.createTyped(rs,builder.create());
        allocGrayCur = Allocation.createTyped(rs,builder.create());
    }

    static  private void  allocPartial(){
        Type.Builder builder = new Type.Builder(rs,Element.I32(rs));
        builder.setX(data.getOriWidth());
        builder.setY(data.getOriHeight());
        allocBasePartialX = Allocation.createTyped(rs,builder.create());
        allocBasePartialY = Allocation.createTyped(rs,builder.create());
    }

    static  private void allocSecPartial(){
        Type.Builder builder = new Type.Builder(rs,Element.F32(rs));
        builder.setX(data.getOriWidth());
        builder.setY(data.getOriHeight());
        allocBaseSecPartialX = Allocation.createTyped(rs,builder.create());
        allocBaseSecPartialY = Allocation.createTyped(rs,builder.create());
    }

    static public void setData(Data src){
        RsInvokorBase.data = src;
    }

    abstract public void start(Object obj);


}
