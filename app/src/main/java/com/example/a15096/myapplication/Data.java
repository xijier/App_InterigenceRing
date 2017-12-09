package com.example.a15096.myapplication;

/**
 * Created by 15096 on 2017/12/3.
 */

public class Data {
    private int imgId;
    private String content;
    private int switchId;
    public Data() {}

    public Data(int imgId, String content) {
        this.imgId = imgId;
        this.content = content;
    }

    public Data(int imgId,int switchId, String content) {
        this.imgId = imgId;
        this.content = content;
        this.switchId = switchId;
    }

    public int getswitchId() {
        return switchId;
    }
    public int getImgId() {
        return imgId;
    }

    public String getContent() {
        return content;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }
    public void setswitchId(int switchId) {
        this.switchId = switchId;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
