package com.example.woweather.tool;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 邹永鹏 on 2018/4/10.
 * 单例模式，记录App状态
 */

public class AppStatusManager {

    private static AppStatusManager mInstance = null;

    private int appStatus = AppStatusConstant.APP_FORCE_KILLED;//0

    private AppStatusManager() {

    }

    public static AppStatusManager getInstance() {
        if(mInstance==null) {
            synchronized (AppStatusManager.class) {
                if(mInstance==null)
                    mInstance = new AppStatusManager();
            }
        }
        return mInstance;
    }

    public void setAppStatus(int appStatus) {
        this.appStatus = appStatus;
    }

    public int getAppStatus() {
        return appStatus;
    }


    public static class AppStatusConstant {

        /**
         * App被回收，初始状态
         */
        public static final int APP_FORCE_KILLED = 0;

        /**
         * 正常运行
         */
        public static final int APP_NORMAL = 1;
    }
}
