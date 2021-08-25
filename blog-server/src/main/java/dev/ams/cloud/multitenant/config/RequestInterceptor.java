package dev.ams.cloud.multitenant.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class RequestInterceptor extends HandlerInterceptorAdapter {
	
	@Autowired
	AppProperties appProperties;
	
	@Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object object) throws Exception {

        String requestURI = request.getRequestURI();
        String tenantID = request.getHeader("X-TenantID");
        if(requestURI.contains("tenants")) {
        	TenantContext.setCurrentTenant(appProperties.getDefaultTenant());
            return true;
        }
        
        log.info("RequestURI::" + requestURI +" || Search for X-TenantID  :: " + tenantID);
        
        if (tenantID == null) {
            response.getWriter().write("X-TenantID not present in the Request Header");
            response.setStatus(400);
            return false;
        }
        TenantContext.setCurrentTenant(tenantID);
        return true;
    }

    @Override
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
        TenantContext.clear();
    }
}
