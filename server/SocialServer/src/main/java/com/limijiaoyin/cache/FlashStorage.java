package com.limijiaoyin.cache;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class FlashStorage implements InitializingBean, DisposableBean,
        ICacheClient {

    private MemcachedClient client;

    private String address;

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
        client.set(key, TIME_TO_LIVE_SECONDS, value);
    }

    public void set(String key, Object value, int exp) {
        client.set(key, exp, value);
    }

    public void inc(String key) {
        client.incr(key, 1, 0, TIME_TO_LIVE_SECONDS);
    }

    public void inc(String key, int exp) {
        client.incr(key, 1, 0, exp);
    }

    @Override
    public void delete(String key) {
        client.delete(key);

    }

    private final static int TIME_TO_LIVE_SECONDS = 30 * 60;

}
