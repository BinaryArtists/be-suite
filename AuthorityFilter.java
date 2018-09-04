package com.alisports.common.login;

import com.alibaba.buc.sso.client.util.SimpleUserUtil;
import com.alibaba.buc.sso.client.vo.BucSSOUser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.Order;

import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Created by guanpeng.cgp on 2017/7/21.
 */
@Order(1)
@WebFilter(filterName = "authorityFilter", urlPatterns = {"/admin/*", "/module/*"})
@Singleton
public class AuthorityFilter implements Filter {

    private static final String ICON_REQUEST = "/favicon.ico";

    private static final String BASE_ACTION = "/admin";
    private static final String LOGIN_PAGE_ACTION = BASE_ACTION + "/loginPage";
    private static final String VALIDATE_ACTION = BASE_ACTION + "/login";

    private static final String BASE_PAGE = "/module";
    private static final String STATIC_LOGIN_PAGE = BASE_PAGE + "/login.html";
    private static final String STATIC_ERROR_PAGE = BASE_PAGE + "/error.html";

    private static final Log log = LogFactory.getLog(AuthorityFilter.class);


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("初始化检测用户权限的过滤器");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = null;
        HttpServletResponse response = null;
        HttpSession session = null;
        String email = null;

        if (req instanceof HttpServletRequest) {
            request = (HttpServletRequest) req;
//            BucSSOUser user = SimpleUserUtil.getBucSSOUser(request);
//            log.info("bucSSOUser ID: "+user.getId());
            response = (HttpServletResponse) res;
            //TODO 去除 CORS
            addCorsHeader(response);
            String requestURI = request.getRequestURI();
            log.info("原始 requestURI: " + requestURI);
            if (StringUtils.equals(requestURI, ICON_REQUEST)) {
                log.info("favicon requestURI: " + requestURI);
                return;
            } else if (StringUtils.equals(requestURI, STATIC_LOGIN_PAGE) || StringUtils.equals(requestURI, STATIC_ERROR_PAGE) || StringUtils.equals(requestURI, LOGIN_PAGE_ACTION) || StringUtils.equals(requestURI, VALIDATE_ACTION) || RegexUtil.isStaticResource(requestURI)) {
                log.info("不过滤 requestURI: " + requestURI);
                filterChain.doFilter(req, res);
                return;
            }

            email = (String) request.getSession().getAttribute("email");
            if (!StringUtils.isBlank(email)) {
                log.info(email + "已登录，访问requestURI: " + requestURI);
                filterChain.doFilter(req, res);
            } else {
                log.info("重定向 requestURI 至登录页: " + requestURI);
                response.sendRedirect(STATIC_LOGIN_PAGE);
                return;
            }
        }
    }

    private void addCorsHeader(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
        response.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
        response.addHeader("Access-Control-Max-Age", "1728000");
    }

    @Override
    public void destroy() {
    }

}
