package com.machao.camore.app.application.activities;

import android.app.Application;
import android.content.Context;


public class CamoreApp extends Application{
    private static Context appContext;

    public CamoreApp(){
        appContext = this;
    }

    static public Context getAppContext(){
        return  appContext;
    }
}
