package com.limijiaoyin.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.limijiaoyin.dto.Platform;
import com.limijiaoyin.dto.SocialBean;

@Entity
public class Social implements Serializable {
    private static final long serialVersionUID = 4517940177300623691L;

    private Long id;

    private Platform platform;

    private String fromUid;

    private Date bindTime;

    private String data;
    
    private String accessToken;
    
    private String deviceId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    @Temporal(TemporalType.TIMESTAMP)
    public Date getBindTime() {
        return bindTime;
    }

    public void setBindTime(Date bindTime) {
        this.bindTime = bindTime;
    }

    @Column(columnDefinition = "text")
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
    
    public void updateSocial(SocialBean socialBean){
        this.deviceId = socialBean.getDeviceId();
        this.data = socialBean.getData();
        this.bindTime = socialBean.getBindTime();
        this.fromUid = socialBean.getFromUid();
        this.platform = socialBean.getPlatform();
        this.accessToken = socialBean.getAccessToken();
    }

    @Override
    public String toString() {
        return "Social [platform=" + platform + ", fromUid=" + fromUid
                + ", bindTime=" + bindTime + ", data=" + data + "]";
    }

}
