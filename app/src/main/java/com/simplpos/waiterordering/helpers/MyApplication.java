package com.simplpos.waiterordering.helpers;

import android.content.Context;
import android.support.multidex.MultiDexApplication;



public class MyApplication extends MultiDexApplication {

    private static Context context;
    public static String pinBlock;

    private boolean isAidl;

    public boolean isAidl() {
        return isAidl;
    }

    public void setAidl(boolean aidl) {
        isAidl = aidl;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        MyApplication.context = getApplicationContext();
        // DatabaseManager.initializeInstance(new DatabaseForDemo(this));
//        DatabaseForDemo.init(getApplicationContext());

        //;
//        new LogExceptionHandle(this);

        //this is for sunmi printer sdk initialization

        /*isAidl = true;
        AidlUtil.getInstance().connectPrinterService(this);*/

    }

    public static Context getAppContext() {
        return MyApplication.context;
    }


}