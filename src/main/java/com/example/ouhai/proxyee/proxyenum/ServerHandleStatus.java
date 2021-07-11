package com.example.ouhai.proxyee.proxyenum;

public enum ServerHandleStatus {

    NOT_CONNECT(0, "还未连接"),
    HTTP(1, "http连接"),
    SSL(2, "ssl连接");


    private int value;

    private String name;

    ServerHandleStatus(int value, String name) {
        this.name = name;
        this.value = value;
    }

    public int toInt() {
        return this.value;
    }
}
