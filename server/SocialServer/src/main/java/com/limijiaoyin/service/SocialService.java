package com.limijiaoyin.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.limijiaoyin.dto.Platform;
import com.limijiaoyin.dto.SocialBean;
import com.limijiaoyin.entity.Social;
import com.limijiaoyin.reposity.SocialDao;

public class SocialService {
    private SocialDao socialDao;

    @Transactional
    public void save(Social p) {
        socialDao.save(p);
    }
    
    @Transactional
    public void save(SocialBean socialBean) {
        Social social = new Social();
        social.updateSocial(socialBean);
        socialDao.save(social);
    }

    @Transactional(readOnly=true)
    public Social getSocialById(long id) {
        return socialDao.findOne(id);
    }
    
    @Transactional(readOnly=true)
    public List<Social> getSocialByDeviceId(String deviceId){
        return socialDao.findByDeviceId(deviceId);
    }
    
    @Transactional(readOnly=true)
    public Social getSocialByDeviceIdAndPlatform(String deviceId,Platform platform){
        return socialDao.findByDeviceIdAndPlatform(deviceId, platform);
    }
    
    public void update(SocialBean socialBean){
        Social social = socialDao.findOne(socialBean.getId());
        if(social != null){
            social.updateSocial(socialBean);
            socialDao.save(social);
        }
    }
    
    public void deleteSocial(Long id){
        socialDao.delete(id);
    }

    public void setSocialDao(SocialDao socialDao) {
        this.socialDao = socialDao;
    }
    
}