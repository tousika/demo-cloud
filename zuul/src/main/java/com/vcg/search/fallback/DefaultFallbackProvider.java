package com.vcg.search.fallback;

import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.context.RequestContext;
import com.vcg.search.bean.Render;
import com.vcg.search.filter.ErrorFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by dongsijia on 2019/2/18.
 */
@Component
public class DefaultFallbackProvider implements FallbackProvider {

    private static Logger LOGGER = LoggerFactory.getLogger(DefaultFallbackProvider.class);

    @Autowired
    public Environment env;

    @Override
    public String getRoute() {
        return "*";
    }

    @Override
    public ClientHttpResponse fallbackResponse() {
        return null;
    }

    @Override
    public ClientHttpResponse fallbackResponse(Throwable cause) {
        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() throws IOException {
                return HttpStatus.I_AM_A_TEAPOT;
            }

            @Override
            public int getRawStatusCode() throws IOException {
                return HttpStatus.I_AM_A_TEAPOT.value();
            }

            @Override
            public String getStatusText() throws IOException {
                return HttpStatus.I_AM_A_TEAPOT.getReasonPhrase();
            }

            @Override
            public void close() {
                RequestContext ctx = RequestContext.getCurrentContext();
                HttpServletRequest request = ctx.getRequest();
                String serviceId = "";
                if (ctx.get("serviceId") != null) {
                    serviceId = ctx.get("serviceId").toString();
                }
                LOGGER.error("uri:{}, error: {}", request.getRequestURI(), cause.getMessage());
                LOGGER.error("uri:{}, error: {}", request.getRequestURI(), cause);
                // TODO
//                WechatAlertUtil.alert(String.format("Fallback:[serviceId:%s, method:%s, uri:%s, queryString:%s," +
//                                " remoteAddr:%s, httpCode:%s, message:%s]", serviceId, request.getMethod(),
//                        request.getRequestURI(), request.getQueryString(), request.getRemoteAddr(),
//                        HttpStatus.I_AM_A_TEAPOT.value(), cause.getMessage()));
            }

            @Override
            public InputStream getBody() throws IOException {
                Render response = new Render(-1, cause.getMessage());
                return new ByteArrayInputStream(JSONObject.toJSONString(response).getBytes());
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
            }
        };
    }
}
