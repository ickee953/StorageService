package com.github.ickee953.micros.common;

public class SavedResult<K, V> {
    K resource;
    V status;

    public SavedResult(K resource, V status){
        this.resource = resource;
        this.status = status;
    }

    public K getResource() {
        return resource;
    }

    public void setResource(K resource) {
        this.resource = resource;
    }

    public V getStatus() {
        return status;
    }

    public void setStatus(V status) {
        this.status = status;
    }
}
