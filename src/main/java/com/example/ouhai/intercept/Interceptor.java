package com.example.ouhai.intercept;


public interface Interceptor {
    // 事前方法
    boolean before();

    // 事后方法
    void after();

    Object around(Invocation invocation) throws Throwable;

    void afterReturning();

    void afterThrowing();

    // 是否使用around方法取代原有方法
    boolean useAround();

}
