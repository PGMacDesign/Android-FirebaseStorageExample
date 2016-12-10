package com.pgmacdesign.firebasestorageexample;

import android.app.Application;
import android.content.Context;

/**
 * Created by pmacdowell on 2016-12-05.
 */

public class MyApplication extends Application {

    private static MyApplication myApplication = null;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = getInstance();
        MyApplication.context = getApplicationContext();
        //FirebaseApp.initializeApp(this);
    }

    public static MyApplication getInstance(){
        if(myApplication == null)
            myApplication = new MyApplication();

        return myApplication;
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }
}
