package com.vcg.search.filter;

import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.vcg.search.bean.Render;
import com.vcg.search.bean.UrlToken;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * Created by dongsijia on 2019/2/14.
 */
public class TokenFilter extends ZuulFilter {
    private final static Logger LOGGER = LoggerFactory.getLogger(TokenFilter.class);

    /**
     * 四种类型：pre,routing,error,post
     * pre：主要用在路由映射的阶段是寻找路由映射表的
     * routing:具体的路由转发过滤器是在routing路由器，具体的请求转发的时候会调用
     * error:一旦前面的过滤器出错了，会调用error过滤器。
     * post:当routing，error运行完后才会调用该过滤器，是在最后阶段的
     */
    @Override public String filterType() {
        return PRE_TYPE;
    }

    /**
     * 自定义过滤器执行的顺序，数值越大越靠后执行，越小就越先执行
     */
    @Override public int filterOrder() {
        return 0;
    }

    /**
     * 控制过滤器生效不生效，可以在里面写一串逻辑来控制
     */
    @Override public boolean shouldFilter() {
        return true;
    }

    @Override public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String url = request.getRequestURI();
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            LOGGER.warn("token is empty, url:{}", url);
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            Render response = new Render(1001, "token is empty");
            ctx.setResponseBody(JSONObject.toJSONString(response));
            return null;
        }
        if (!checkToken(url, token)) {
            LOGGER.warn("token invalid, url:{}, token:{}", url, token);
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            Render response = new Render(1002, "token invalid");
            ctx.setResponseBody(JSONObject.toJSONString(response));
            return null;
        }
        return null;
    }

    private boolean checkToken(String url, String token) {
        if (url.startsWith("/api/blacklist")) {
            url = "/api/blacklist";
        }
        String[] urls = url.split("/");
        if (urls.length > 4) {
            StringBuilder newUrl = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                newUrl.append(urls[i]);
                newUrl.append("/");
            }
            url = newUrl.toString();
        }
        if (url.endsWith("/")) {
            url = url.substring(0, url.lastIndexOf("/"));
        }
        if (UrlToken.get(url) != null && UrlToken.get(url).equals(token)) {
            return true;
        }
        return false;
    }
}
