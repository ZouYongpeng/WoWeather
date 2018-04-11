package com.example.woweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by 邹永鹏 on 2018/4/8.
 */

public class Collection extends DataSupport {
    private String collectName;
    private String collectId;

    public Collection(){}
    public void setCollectName(String collectName) {
        this.collectName = collectName;
    }

    public void setCollectId(String collectId) {
        this.collectId = collectId;
    }

    public String getCollectName() {
        return collectName;
    }

    public String getCollectId() {
        return collectId;
    }
}
