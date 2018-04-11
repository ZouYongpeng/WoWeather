package com.example.woweather;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.woweather.tool.ActivityCollector;
import com.example.woweather.tool.AppStatusManager;

/**
 * Created by 邹永鹏 on 2018/4/10.
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        AppStatusManager.getInstance().setAppStatus(AppStatusManager.AppStatusConstant.APP_NORMAL);
        super.onCreate(savedInstanceState);
//        checkAppStatus();
//        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        ActivityCollector.removeActivity(this);
    }
//
//    private void checkAppStatus() {
//        if(AppStatusManager.getInstance().getAppStatus()==AppStatusManager.AppStatusConstant.APP_FORCE_KILLED) {
//            //应用启动入口SplashActivity，走重启流程
//            Intent intent = new Intent(this, MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//        }
//    }
}
