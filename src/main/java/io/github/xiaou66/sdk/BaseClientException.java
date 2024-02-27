package io.github.xiaou66.sdk;

/**
 * @author xiaou
 * @date 2024/2/27
 */
public class BaseClientException extends Exception {
    public BaseClientException(String body) {
        super("原始响应：" + body);
    }
    public BaseClientException(Exception e) {
        super(e);
    }
}
