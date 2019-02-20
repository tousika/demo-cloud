package com.vcg.search.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.vcg.search.bean.Render;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * Created by dongsijia on 2019/2/18.
 */
@Component public class MethodFilter extends ZuulFilter {

    private final static Logger LOGGER = LoggerFactory.getLogger(MethodFilter.class);

    @Override public String filterType() {
        return PRE_TYPE;
    }

    @Override public int filterOrder() {
        return 0;
    }

    @Override public boolean shouldFilter() {
        return true;
    }

    @Override public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        String method = ctx.getRequest().getMethod();
        if (!method.equalsIgnoreCase("get") && !method.equalsIgnoreCase("post")) {
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            Render response = new Render(1003, method + " method not allowed");
            ctx.setResponseBody(JSON.toJSONString(response));
        }
        return null;
    }
}
