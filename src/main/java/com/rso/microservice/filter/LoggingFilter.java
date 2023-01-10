package com.rso.microservice.filter;

import com.rso.microservice.util.MDCUtil;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class LoggingFilter implements Filter {

    final BuildProperties buildProperties;

    public LoggingFilter(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        String requestId = req.getHeader("X-Request-Id");
        if (requestId == null || requestId.isBlank())
            requestId = UUID.randomUUID().toString();

        MDCUtil.put(MDCUtil.MDCUtilKey.MICROSERVICE_NAME, "Prices");
        MDCUtil.put(MDCUtil.MDCUtilKey.REQUEST_ID, requestId);
        MDCUtil.put(MDCUtil.MDCUtilKey.MICROSERVICE_VERSION, buildProperties.getVersion());
        filterChain.doFilter(servletRequest, servletResponse);
        MDCUtil.clear();
    }

}
