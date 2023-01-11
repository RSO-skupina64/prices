package com.rso.microservice.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.rso.microservice.api.dto.CheckUserRoleRequestDto;
import com.rso.microservice.api.dto.MessageDto;
import com.rso.microservice.util.MDCUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RefreshScope
public class AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final AuthenticationService authenticationService;

    @Value("${microservice.authentication.url}/authentication")
    private String authenticationAuthenticationUrl;

    public AuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public boolean checkUserRoleWrapper(String jwt, String role) {
        CheckUserRoleRequestDto checkUserRoleRequest = new CheckUserRoleRequestDto(role);
        String response = this.checkUserRole(jwt, checkUserRoleRequest);

        return response.equals("checkUserRole completed, user is in role");
    }

    public String checkUserRole(String jwt, CheckUserRoleRequestDto checkUserRoleRequest) {
        log.info("checkUserRole from URL: {}", authenticationAuthenticationUrl);
        String requestId = MDCUtil.get(MDCUtil.MDCUtilKey.REQUEST_ID);
        String version = MDCUtil.get(MDCUtil.MDCUtilKey.MICROSERVICE_VERSION);
        MessageDto response = authenticationService.callCheckUserRole(jwt, checkUserRoleRequest, requestId, version);
        log.info("received response: {}", response.getMessage());
        return response.getMessage();
    }

    @HystrixCommand(fallbackMethod = "circuitBreakerCheckUserRole")
    public MessageDto callCheckUserRole(String jwt, CheckUserRoleRequestDto checkUserRoleRequest, String requestId,
                                        String version) {
        String url = String.format("%s/check-user-role", authenticationAuthenticationUrl);

        MDCUtil.putAll("Data aggregation", version, requestId);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, jwt);
        headers.add("X-Request-Id", requestId);
        HttpEntity<?> requestEntity = new HttpEntity<>(checkUserRoleRequest, headers);
        ResponseEntity<MessageDto> response = new RestTemplate().exchange(url, HttpMethod.POST, requestEntity,
                MessageDto.class);
        return response.getBody();
    }

    public MessageDto circuitBreakerCheckUserRole(String jwt, CheckUserRoleRequestDto checkUserRoleRequest,
                                                  String requestId, String version) {
        MDCUtil.putAll("Data aggregation", version, requestId);
        log.error("There was an error when calling checkUserRole, so circuit breaker was activated");
        return new MessageDto("Error while calling authentication, circuit breaker method called");
    }

}
