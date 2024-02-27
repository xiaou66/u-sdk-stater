package io.github.xiaou66.sdk.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.xiaou66.sdk.BaseRequest;
import io.github.xiaou66.sdk.BaseResponse;

/**
 * 客户端，用于定义一个客户端的基本行为 <br />
 * 例如：请求前参数处理加签、请求出现失败是否重试、尝试次数等
 * @author xiaou
 * @date 2024/2/22
 */
public interface IClient<T extends BaseRequest<R>, R extends BaseResponse> {
    /**
     * 请求前参数处理
     *
     * @param request 请求参数
     */
    void beforeRequestParams(T request);

    /**
     * 获取json解析器
     *
     * @return json解析器
     */
    default ObjectMapper getJson() {
        ObjectMapper mapper = new ObjectMapper();
        // 解决字段不存在报错的问题
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * 请求出现失败是否重试 <br />
     * 不同的 API 要求不同的重试策略，可以通过重写此方法来实现
     * @param json 请求返回的结果值
     */
    default boolean tryRetry(String json) {
        return true;
    }

    /**
     * 尝试次数，默认 3 次
     */
    default int tryCount() {
        return 3;
    }
}
