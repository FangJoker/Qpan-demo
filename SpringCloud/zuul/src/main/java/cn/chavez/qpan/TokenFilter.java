package cn.chavez.qpan;

import com.alibaba.fastjson.JSONObject;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/4/14 15:56
 */
public class TokenFilter extends ZuulFilter {

    @Override
    public String filterType() {
        //可以在请求被路由之前调用
        return "pre";
    }

    @Override
    public int filterOrder() {
        //filter执行顺序，通过数字指定 ,优先级为0，数字越大，优先级越低
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        //是否执行该过滤器，此处为true，说明需要过滤
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        ctx.setSendZuulResponse(true);
        System.out.println("--->>> TokenFilter {" + request.getMethod() + "}" + "{" + request.getRequestURL().toString() + "}");
        try {
            if (JWTokenSupport.validateJWTokenAspect()) {
                //对请求进行路由
                ctx.setResponseStatusCode(200);
                return null;
            } else {
                ctx.setResponseStatusCode(401);
                tokenErrorHandler(ctx);
            }
        } catch (Exception e) {
            e.printStackTrace();
            tokenErrorHandler(ctx);
        }
        return null;
    }

    void tokenErrorHandler(RequestContext ctx){
        JSONObject response = new JSONObject();
        response.put("msg","invalid token");
        ctx.setResponseBody(response.toJSONString());
    }
}
