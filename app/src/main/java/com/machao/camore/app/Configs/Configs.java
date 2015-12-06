package com.machao.camore.app.Configs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.machao.camore.app.application.activities.CamoreApp;
import com.machao.camore.R;

import java.io.File;


public  class Configs {
    private static String externStorageDir="";

    public static final String ORI_IM_TITLE = " Original image ";
    public static final String CUR_IM_TITLE = " Super-Resolution image ";
    public static final String OVERALL_PROG_TITLE = " Overall progress ";
    public static final String CUR_PROG_TITLE = " Current progress ";

    public static final String SUPER_RES_IMAGS_DIR = "super_res_iamges/";


    static {
        File storageDirFile = Environment.getExternalStorageDirectory();
        externStorageDir = storageDirFile.getAbsolutePath();
    }

    public Configs(){

    }

    static public String getSuperResImagsDir(){
        return externStorageDir + "/" + SUPER_RES_IMAGS_DIR ;
    }

    static public String getExternStorageDir(){
        return externStorageDir;
    }

    static public String getWorkingDir(){
        Context appContext = CamoreApp.getAppContext();
        SharedPreferences appPref = appContext.getSharedPreferences(
                appContext.getString(R.string.g_preference_group_name),
                Context.MODE_PRIVATE);

        String workingDir =  appPref.getString(appContext.getString(
                R.string.g_preference_working_dir),externStorageDir);

        return workingDir;
    }

    static public void setWorkingDir(String absPath){
        Context appContext = CamoreApp.getAppContext();
        SharedPreferences appPref = appContext.getSharedPreferences(
                appContext.getString(R.string.g_preference_group_name),
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = appPref.edit();
        String key = appContext.getString(R.string.g_preference_working_dir);
        editor.putString(key,absPath);
        editor.commit();

    }
}
