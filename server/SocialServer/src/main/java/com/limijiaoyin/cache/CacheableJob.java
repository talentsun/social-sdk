package com.limijiaoyin.cache;

public abstract class CacheableJob<T> {
    private final ICacheClient cacheClient;

    private final String key;

    private static Object nullObject = new String(
            "it is a null object in cache, i am unique, right?");

    public CacheableJob(ICacheClient cacheClient, String key) {
        super();
        this.cacheClient = cacheClient;
        this.key = key;
    }

    protected abstract T doJobInternal();

    @SuppressWarnings("unchecked")
    public final T execute() {
        Object cache = null;

        try {
            cache = cacheClient.get(key);
        } catch (Exception e) {}

        if (cache != null) {
            if (cache.equals(nullObject)) {
                return null;
            } else {
                return (T) cache;
            }
        }

        T result = doJobInternal();
        try {
            if (result != null) {
                cacheClient.set(key, result);
            } else {
                cacheClient.set(key, nullObject);
            }
        } catch (Exception e) {}
        return result;
    }
}
