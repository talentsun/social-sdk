package com.limijiaoyin.cache;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class SpyMemcachedClient implements InitializingBean, DisposableBean,
        ICacheClient {

    private MemcachedClient client;

    private String address;

    private static final int DEFAULT_EXPIRATION = 24 * 60 * 60;

    @Override
    public void afterPropertiesSet() throws Exception {
        client = new MemcachedClient(AddrUtil.getAddresses(address));

    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public void destroy() throws Exception {
        if (client != null) {
            client.shutdown();
        }

    }

    @Override
    public Object get(String key) {

        return client.get(key);
    }

    @Override
    public void set(String key, Object value) {
        client.set(key, DEFAULT_EXPIRATION, value);
    }

    @Override
    public void delete(String key) {
        client.delete(key);

    }

}
