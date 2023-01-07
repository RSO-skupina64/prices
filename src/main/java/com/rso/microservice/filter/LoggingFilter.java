package com.rso.microservice.filter;

import com.rso.microservice.util.Constants;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    BuildProperties buildProperties;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        String requestId = req.getHeader("X-Request-Id");
        if (requestId == null || requestId.isBlank())
            requestId = UUID.randomUUID().toString();

        MDC.put(Constants.MDC_NAME, "Prices");
        MDC.put(Constants.MDC_REQUEST_ID, requestId);
        MDC.put(Constants.MDC_VERSION, buildProperties.getVersion());
        filterChain.doFilter(servletRequest, servletResponse);
        MDC.remove(Constants.MDC_NAME);
        MDC.remove(Constants.MDC_REQUEST_ID);
        MDC.remove(Constants.MDC_VERSION);
    }

}
