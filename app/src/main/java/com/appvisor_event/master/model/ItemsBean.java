package com.appvisor_event.master.model;

import java.util.List;

/**
 * Created by WangLin on 2016/11/1.
 */

public class ItemsBean {

    /**
     * message : Share2
     * items : [{"name":"f2_image01_en","x":0.75,"y":0.25,"width":0.3}]
     */

    private String message;
    /**
     * name : f2_image01_en
     * x : 0.75
     * y : 0.25
     * width : 0.3
     */

    private List<ItemBean> items;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ItemBean> getItems() {
        return items;
    }

    public void setItems(List<ItemBean> items) {
        this.items = items;
    }
}
