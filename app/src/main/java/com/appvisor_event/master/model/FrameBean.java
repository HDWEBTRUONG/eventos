package com.appvisor_event.master.model;

import java.util.List;

/**
 * Created by WangLin on 2016/11/1.
 */

public class FrameBean {
    private List<ItemsBean> jp;
    private List<ItemsBean> en;

    public List<ItemsBean> getJp() {
        return jp;
    }

    public void setJp(List<ItemsBean> jp) {
        this.jp = jp;
    }

    public List<ItemsBean> getEn() {
        return en;
    }

    public void setEn(List<ItemsBean> en) {
        this.en = en;
    }
}
