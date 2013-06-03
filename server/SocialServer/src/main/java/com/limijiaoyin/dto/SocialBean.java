package com.limijiaoyin.dto;

import java.io.Serializable;
import java.util.Date;

import com.limijiaoyin.entity.Social;

public class SocialBean implements Serializable{
    private static final long serialVersionUID = 4746497706466499810L;

    private long id;

    private Platform platform;

    private String fromUid;

    private Date bindTime;

    private String data;

    private String accessToken;

    private String deviceId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Platform getPlatform() {
        return platform;
    }

    public void setPlatform(Platform platform) {
        this.platform = platform;
    }

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public Date getBindTime() {
        return bindTime;
    }

    public void setBindTime(Date bindTime) {
        this.bindTime = bindTime;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    public void updateSocialBean(Social social){
        
    }
}
