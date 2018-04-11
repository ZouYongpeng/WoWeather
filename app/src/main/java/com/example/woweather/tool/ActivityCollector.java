package com.example.woweather.tool;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 邹永鹏 on 2018/4/10.
 */

public class ActivityCollector {

    public static List<Activity> sActivities=new ArrayList<>();

    public static void addActivity(Activity activity){
        sActivities.add(activity);
    }

    public static void removeActivity(Activity activity){
        sActivities.remove(activity);
    }

    public static void finishAll(){
        for (Activity activity:sActivities){
            if (!activity.isFinishing()){
                activity.finish();
            }
        }
    }
}
