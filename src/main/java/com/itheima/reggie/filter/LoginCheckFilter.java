package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/*
检查用户是否完成登录
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*") // 先将所有请求都拦截下来，然后再进行手动筛选
public class LoginCheckFilter implements Filter {
    //spring提供的路径匹配器，专门用于匹配请求路径，支持通配符；
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //前置：强制转换为http协议的请求对象、响应对象 （转换原因：要使用子类中特有方法）
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 定义不需要进行拦截的url请求路劲。比如登录请求和退出请求，以及所有的静态资源页面
        //此处需要注意的是，虽然用户是通过静态页面的按钮进行服务的访问，但是实际上对于数据调用的请求真正传递给服务器的还是controller上的请求路径；
        //所以静态页面不需要拦截，只要对页面上展示的数据进行管理即可；
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/login",
                "/user/loginout",
                "/user/sendMsg"
        };
        //1、获取请求路径
        String requestUrl = request.getRequestURI();
        log.error("拦截到请求：{}", requestUrl);
        //2、判断本次请求是否需要处理
        boolean check = check(urls,requestUrl);
        if (check) {
            log.info("本次请求不需要处理：{}", requestUrl);
            filterChain.doFilter(request, response);
            return;
        }
        //3-1、获取请求头中的session信息，看本次浏览器端请求是否是已登录状态
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录");
            Long empId  = (Long)request.getSession().getAttribute("employee");
            log.info("empId:{}",empId);
            BaseContext.setCurrentID(empId);
            filterChain.doFilter(request, response);
            return;
        }
        //3-2、获取请求头中的session信息，看本次移动端用户请求是否是已登录状态
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录");
            Long userId  = (Long)request.getSession().getAttribute("user");
            log.info("userId:{}",userId);
            BaseContext.setCurrentID(userId);
            filterChain.doFilter(request, response);
            return;
        }

        //4、如果没有登录，那么需要将未登录的信息返回给前端，前端在 backend/js/request.js下定义了响应拦截器，根据返回的未登录信息会自动跳转到登录页面
        log.info("用户未登录，正在跳转登录页面....");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN"))); // 将相应信息写回到响应对象response中返回
        return;
    }

    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
