package com.appvisor_event.master.model;

import java.util.List;

/**
 * Created by WangLin on 2016/11/1.
 */

public class FrameBean {
    private List<ItemsBean> ja;
    private List<ItemsBean> en;

    public List<ItemsBean> getJa() {
        return ja;
    }

    public void setJa(List<ItemsBean> ja) {
        this.ja = ja;
    }

    public List<ItemsBean> getEn() {
        return en;
    }

    public void setEn(List<ItemsBean> en) {
        this.en = en;
    }
}
