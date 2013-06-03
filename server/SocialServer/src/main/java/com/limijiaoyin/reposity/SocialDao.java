package com.limijiaoyin.reposity;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.limijiaoyin.dto.Platform;
import com.limijiaoyin.entity.Social;

@Component
public interface SocialDao extends CrudRepository<Social, Long> {
    
    Social findByDeviceIdAndPlatform(String deviceId,Platform platform);
    
    List<Social> findByDeviceId(String deviceId);
}

