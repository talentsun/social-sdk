package com.limijiaoyin.cache;

public interface ICacheClient {
    Object get(String key);

    void set(String key, Object value);

    void delete(String key);
}
