package com.appvisor_event.master.model;

/**
 * Created by WangLin on 2016/11/1.
 */

public class ItemBean {
    /**
     * {
     * "name": "f2_image01_en",
     * "x": 0.75,
     * "y": 0.25,
     * "width": 0.3
     }
     */
    private String name;
    private double x;
    private double y;
    private double width;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }
}
