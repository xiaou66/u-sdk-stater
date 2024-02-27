package io.github.xiaou66.sdk;

import io.github.xiaou66.sdk.enums.RequestMethod;

import java.util.Map;

/**
 * 请求基类参数，所有请求参数都需要继承此接口
 * @author xiaou
 * @date 2024/2/27
 */
public interface BaseRequest {
    /**
     * 获取请求url
     * @return 请求url
     */
    default String getRequestUrl() {
        return "";
    }

    /**
     * 设置当前接口的请求地址
     */
    default RequestMethod getRequestMethod() {
        return RequestMethod.GET;
    }

    /**
     * 获取 GET 请求参数
     */
    default Map<String, String> getParams(){
        return null;
    }

    /**
     * 获取 POST 请求参数
     */
    default Map<String, String> getBody(){
        return null;
    }
}
