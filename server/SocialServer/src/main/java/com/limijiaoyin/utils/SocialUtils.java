package com.limijiaoyin.utils;

import com.limijiaoyin.dto.SocialBean;
import com.limijiaoyin.entity.Social;

public class SocialUtils {
    public static SocialBean getSocialBeanBySocial(Social social) {
        if (social != null) {
            SocialBean socialBean = new SocialBean();

            socialBean.setAccessToken(social.getAccessToken());
            socialBean.setBindTime(social.getBindTime());
            socialBean.setData(social.getData());
            socialBean.setDeviceId(social.getDeviceId());
            socialBean.setFromUid(social.getFromUid());
            socialBean.setId(social.getId());
            socialBean.setPlatform(social.getPlatform());

            return socialBean;
        }
        return null;
    }
}
