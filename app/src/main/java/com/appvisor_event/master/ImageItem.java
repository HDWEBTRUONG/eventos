package com.appvisor_event.master;

/**
 * Created by BraveSoft on 16/10/25.
 */
public class ImageItem {
    private String name;
    private int id;
    private float width_position;
    private float height_position;
    private float scale;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getWidth_position() {
        return width_position;
    }

    public void setWidth_position(float width_position) {
        this.width_position = width_position;
    }

    public float getHeight_position() {
        return height_position;
    }

    public void setHeight_position(float height_position) {
        this.height_position = height_position;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }
}
