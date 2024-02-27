package io.github.xiaou66.sdk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author xiaou
 * @date 2024/2/22
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public interface BaseResponse {
    /**
     * 用于判断接口请求是否成功。
     */
     default boolean requestSuccess() {
         return true;
     }
}
