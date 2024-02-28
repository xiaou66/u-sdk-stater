package io.github.xiaou66.sdk.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import io.github.xiaou66.sdk.BaseClientException;
import io.github.xiaou66.sdk.BaseRequest;
import io.github.xiaou66.sdk.BaseResponse;
import io.github.xiaou66.sdk.enums.RequestMethod;
import io.github.xiaou66.sdk.util.ReflectUtils;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author xiaou
 * @date 2024/2/22
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ClientExecutor {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private ClientExecutor () {}

    public static <R extends BaseResponse, T extends BaseRequest<R>> R execute(IClient client, T request) throws BaseClientException {
        // 1. 给 client 修改请求参数
        client.beforeRequestParams(request);
        // 2. 组织请求
        // 获取 params
        String params = getRequestParam(request);

        // 请求 URL
        String requestUrl = request.getRequestUrl() + params;

        RequestEntity<?> requestEntity = null;
        if (RequestMethod.POST.equals(request.getRequestMethod())) {
            // POST 请求
            try {
                requestEntity = RequestEntity
                        .post(requestUrl)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(client.getJson().writeValueAsString(request));
            } catch (JsonProcessingException e) {
                throw new BaseClientException(e);
            }
        } else {
            // GET 请求
            requestEntity = RequestEntity
                    .get(requestUrl)
                    .accept(MediaType.APPLICATION_JSON)
                    .build();
        }

        return execute(client, requestEntity, request);
    }

    @SuppressWarnings({"unchecked"})
    public static<R extends BaseResponse, T extends BaseRequest<R>> R execute(IClient client,
                                                        RequestEntity<?> requestEntity,
                                                        T request) throws BaseClientException {
        ResponseEntity<String> response = null;
        int tryCount = client.tryCount();
        int trys = 0;
        do {
            BaseClientException e = new BaseClientException("请求失败");
            try {
                response = REST_TEMPLATE.exchange(requestEntity, String.class);
                R res = (R) parseResult(client, response.getBody(), request);
                if (Objects.nonNull(res) && res.requestSuccess()) {
                    return res;
                }
            } catch (RestClientException exception) {
                e = new BaseClientException(exception.getMessage());
            } catch (Exception ex) {
                e = new BaseClientException(ex);

            }
            if (trys++ >= tryCount) {
                throw e;
            }
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception ignored) {}
        } while (true);
    }

    private static <R extends BaseResponse ,T extends BaseRequest<R>> Object parseResult(IClient client, String body, BaseRequest<R> request) {
        Type type = findBaseRequestByInterfaces(request);
        if (Objects.isNull(type)) {
            type = findBaseRequestBySuperClass(request);
        }

        if (Objects.isNull(type)) {
            return null;
        }
        if (!(type instanceof ParameterizedType)) {
            return null;
        }

        ParameterizedType pType = (ParameterizedType) type;

        Type[] typeArguments = pType.getActualTypeArguments();

        Type baseResp = typeArguments[0];

        TypeReference<?> typeRef = new TypeReference<Object>() {
            @Override
            public Type getType() {
                return baseResp;
            }
        };

        try {
            return client.getJson().readValue(body, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static <R extends BaseResponse> Type findBaseRequestBySuperClass(BaseRequest<R> request) {
        Type type = request.getClass().getGenericSuperclass();
        return type;
    }

    private static <R extends BaseResponse> Type findBaseRequestByInterfaces(BaseRequest<R> request) {
        return Arrays.stream(request.getClass().getGenericInterfaces())
                .filter(type -> type instanceof ParameterizedType && BaseResponse.class.isAssignableFrom((Class<?>) ((ParameterizedType) type).getRawType()))
                .findFirst()
                .orElse(null);
    }

    private static <R extends BaseResponse> String getRequestParam(BaseRequest<R> request) {
        Map<String, String> params = null;
        if (Objects.nonNull(request.getParams())) {
            params = request.getParams();
        } else if (RequestMethod.GET.equals(request.getRequestMethod())){
            params = ReflectUtils.extractFields(request);
        }

        if (Objects.isNull(params)) {
            return "";
        } else {
            return "?" + params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
        }
    }
}
