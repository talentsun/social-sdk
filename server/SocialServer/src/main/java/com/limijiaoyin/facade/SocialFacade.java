package com.limijiaoyin.facade;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.limijiaoyin.cache.CacheableJob;
import com.limijiaoyin.cache.ICacheClient;
import com.limijiaoyin.dto.Platform;
import com.limijiaoyin.dto.SocialBean;
import com.limijiaoyin.entity.Social;
import com.limijiaoyin.service.SocialService;
import com.limijiaoyin.utils.SocialUtils;

public class SocialFacade {
    @Autowired 
    private SocialService socialService;
    
    private ICacheClient cacheClient;
    
    public void save(SocialBean socialBean){
        socialService.save(socialBean);
        invalidateCache(socialBean);
    }
    
    public void update(SocialBean socialBean){
        socialService.update(socialBean);
        invalidateCache(socialBean);
    }
    
    public SocialBean findSocialBeanByDeviceIdAndPlatform(final String deviceId,final Platform platform){
        
        return new CacheableJob<SocialBean>(cacheClient,
                getSocialBeanByDeviceIdAndPlatformCacheKey(deviceId,platform)) {
            @Override
            protected SocialBean doJobInternal() {
                Social social = socialService.getSocialByDeviceIdAndPlatform(deviceId, platform);
                return SocialUtils.getSocialBeanBySocial(social);
            }
        }.execute();
    }
    
    private String getSocialBeanByDeviceIdAndPlatformCacheKey(String deviceId,Platform platform){
        String plat = "";
        if(platform.equals(Platform.SINA)){
            plat = "sina";
        } else if(platform.equals(Platform.QQ)){
            plat = "qq";
        } else if(platform.equals(Platform.RENREN)){
            plat = "renren";
        }
        return "limijiaoyin_" + deviceId + "_" + plat;
    }
    
    private String getSocialBeanByDeviceIdCacheKey(String deviceId){
        return "limijiaoyin_" + deviceId;
    }
    
    
    public void deleteSocial(Long id){
        Social social = socialService.getSocialById(id);
        if(social != null){
            invalidateCache(SocialUtils.getSocialBeanBySocial(social));
        }
        socialService.deleteSocial(id);
    }
    
    private void invalidateCache(SocialBean socialBean) {
        try {
            if (socialBean != null) {
                cacheClient
                        .delete(getSocialBeanByDeviceIdAndPlatformCacheKey(socialBean.getDeviceId(),socialBean.getPlatform()));
                cacheClient.delete(getSocialBeanByDeviceIdCacheKey(socialBean.getDeviceId()));
            }
        } catch (Exception e) {}
    }
    
    public List<SocialBean> findSocialByDeviceId(final String deviceId){
        
        return new CacheableJob<List<SocialBean>>(cacheClient,
                getSocialBeanByDeviceIdCacheKey(deviceId)) {
            @Override
            protected List<SocialBean> doJobInternal() {
                List<Social> socialList = socialService.getSocialByDeviceId(deviceId);
                List<SocialBean> socialBeanList = new ArrayList<SocialBean>();
                for(Social social : socialList){
                    socialBeanList.add(SocialUtils.getSocialBeanBySocial(social));
                }
                return socialBeanList;
            }
        }.execute();
    }
    
    

    public ICacheClient getCacheClient() {
        return cacheClient;
    }

    public void setCacheClient(ICacheClient cacheClient) {
        this.cacheClient = cacheClient;
    }
}
