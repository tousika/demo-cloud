package com.vcg.search.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.vcg.search.bean.Render;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.ERROR_TYPE;

/**
 * Created by dongsijia on 2019/2/18.
 */
@Component
public class ErrorFilter extends ZuulFilter {
    private static Logger LOGGER = LoggerFactory.getLogger(ErrorFilter.class);
    private static final String DEFAULT_ERR_MSG = "系统繁忙,请稍后再试";
    @Autowired
    public Environment env;

    @Override
    public String filterType() {
        return ERROR_TYPE;
    }

    @Override
    public int filterOrder() {
        return -1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        ZuulException exception = findZuulException(ctx.getThrowable());
        HttpServletRequest request = ctx.getRequest();
        int responseCode;
        Render response = new Render();
        String message = DEFAULT_ERR_MSG;
        if (StringUtils.hasText(exception.errorCause)) {
            request.setAttribute("javax.servlet.error.message", exception.errorCause);
        }
        Throwable e = ctx.getThrowable();
        Throwable re = getOriginException(e);
        if (re instanceof java.net.ConnectException) {
            response.setCode(2001);
            responseCode = HttpServletResponse.SC_REQUEST_TIMEOUT;
            message = "Real Service Connection refused";
            LOGGER.warn("uri:{}, error: {}", request.getRequestURI(), re.getMessage());
            String serviceId = "";
            if (ctx.get("serviceId") != null) {
                serviceId = ctx.get("serviceId").toString();
            }
            // TODO
//            WechatAlertUtil.alert(String.format("Error:[serviceId:%s, method:%s, uri:%s, queryString:%s," +
//                            " remoteAddr:%s, httpCode:%s, message:%s]", serviceId, request.getMethod(),
//                    request.getRequestURI(), request.getQueryString(), request.getRemoteAddr(),
//                    HttpServletResponse.SC_REQUEST_TIMEOUT, message));
        } else if (re instanceof java.net.SocketTimeoutException) {
            response.setCode(2002);
            responseCode = HttpServletResponse.SC_REQUEST_TIMEOUT;
            message = "Real Service Timeout";
            LOGGER.error("uri:{}, error: {}", request.getRequestURI(), re.getMessage());
            String serviceId = "";
            if (ctx.get("serviceId") != null) {
                serviceId = ctx.get("serviceId").toString();
            }
//            WechatAlertUtil.alert(String.format("Error:[serviceId:%s, method:%s, uri:%s, queryString:%s," +
//                            " remoteAddr:%s, httpCode:%s, message:%s]", serviceId, request.getMethod(),
//                    request.getRequestURI(), request.getQueryString(), request.getRemoteAddr(),
//                    HttpServletResponse.SC_REQUEST_TIMEOUT, message));
        } else if (re instanceof com.netflix.client.ClientException) {
            response.setCode(2003);
            responseCode = HttpServletResponse.SC_REQUEST_TIMEOUT;
            message = re.getMessage();
            LOGGER.error("uri:{}, error: {}", request.getRequestURI(), re.getMessage());
            String serviceId = "";
            if (ctx.get("serviceId") != null) {
                serviceId = ctx.get("serviceId").toString();
            }
//            WechatAlertUtil.alert(String.format("Error:[serviceId:%s, method:%s, uri:%s, queryString:%s," +
//                            " remoteAddr:%s, httpCode:%s, message:%s]", serviceId, request.getMethod(),
//                    request.getRequestURI(), request.getQueryString(), request.getRemoteAddr(),
//                    HttpServletResponse.SC_REQUEST_TIMEOUT, message));
        } else if ("429".equals(re.getMessage())) {
            response.setCode(2005);
            responseCode = HttpStatus.TOO_MANY_REQUESTS.value();
            message = "ratelimit," + re.getMessage();
            LOGGER.warn("ratelimt!!! uri:{}, error: {}", request.getRequestURI(), re.getMessage());
            String serviceId = "";
            if (ctx.get("serviceId") != null) {
                serviceId = ctx.get("serviceId").toString();
            }
//            WechatAlertUtil.alert(String.format("Error:[serviceId:%s, method:%s, uri:%s, queryString:%s," +
//                            " remoteAddr:%s, httpCode:%s, message:%s]", serviceId, request.getMethod(),
//                    request.getRequestURI(), request.getQueryString(), request.getRemoteAddr(),
//                    HttpStatus.TOO_MANY_REQUESTS, message));
        } else {
            response.setCode(2000);
            responseCode = HttpServletResponse.SC_BAD_GATEWAY;
            LOGGER.error("Error during filtering", e);
            String serviceId = "";
            if (ctx.get("serviceId") != null) {
                serviceId = ctx.get("serviceId").toString();
            }
//            WechatAlertUtil.alert(String.format("Error:[serviceId:%s, method:%s, uri:%s, queryString:%s," +
//                            " remoteAddr:%s, httpCode:%s, message:%s]", serviceId, request.getMethod(),
//                    request.getRequestURI(), request.getQueryString(), request.getRemoteAddr(),
//                    HttpServletResponse.SC_BAD_GATEWAY, message));
        }
        response.setMsg(message);

        ctx.setResponseStatusCode(responseCode);
        ctx.setResponseBody(JSON.toJSONString(response));
        ctx.addZuulResponseHeader("Content-Type", MediaType.APPLICATION_JSON.toString());
        ctx.setSendZuulResponse(false);

        return null;
    }

    ZuulException findZuulException(Throwable throwable) {
        if (throwable.getCause() instanceof ZuulRuntimeException) {
            // this was a failure initiated by one of the local filters
            return (ZuulException) throwable.getCause().getCause();
        }

        if (throwable.getCause() instanceof ZuulException) {
            // wrapped zuul exception
            return (ZuulException) throwable.getCause();
        }

        if (throwable instanceof ZuulException) {
            // exception thrown by zuul lifecycle
            return (ZuulException) throwable;
        }

        // fallback, should never get here
        return null;
    }

    private Throwable getOriginException(Throwable e) {
        e = e.getCause();
        while (e.getCause() != null) {
            e = e.getCause();
        }
        return e;
    }

}
