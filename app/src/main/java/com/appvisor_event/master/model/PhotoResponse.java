package com.appvisor_event.master.model;


/**
 * Created by WangLin on 2016/11/1.
 */

public class PhotoResponse {

    /**
     * status : 200
     * version : 1
     * url : https://stg-api.appvisor-event.com/common/zip/photoframes/omotesando2016/images.zip
     * frame : {"jp":[{"message":"シェアMSG","items":[{"name":"f1_image01_jp","x":0.8,"y":0.8,"width":0.05},{"name":"f1_image02_jp","x":0.5,"y":0.5,"width":0.1},{"name":"f1_image03_jp","x":0.1,"y":0.3,"width":0.25}]},{"message":"シェア2","items":[{"name":"f2_image01_jp","x":0.75,"y":0.25,"width":0.3}]}],"en":[{"message":"Share MSG","items":[{"name":"f1_image01_en","x":0.8,"y":0.8,"width":0.05},{"name":"f1_image02_en","x":0.5,"y":0.5,"width":0.1},{"name":"f1_image03_en","x":0.1,"y":0.3,"width":0.25}]},{"message":"Share2","items":[{"name":"f2_image01_en","x":0.75,"y":0.25,"width":0.3}]}]}
     */

    private int status;
    private int version;
    private String url;
    private FrameBean frame;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public FrameBean getFrame() {
        return frame;
    }

    public void setFrame(FrameBean frame) {
        this.frame = frame;
    }

}
