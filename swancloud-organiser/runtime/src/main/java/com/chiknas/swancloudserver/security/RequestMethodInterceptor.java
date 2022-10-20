package com.chiknas.swancloudserver.security;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author nkukn
 * @since 4/5/2021
 */
public class RequestMethodInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // allow only GET and POST requests to harden the server from attacks like HTTP Verb Tampering Attack
        if (HttpMethod.GET.matches(request.getMethod()) || HttpMethod.POST.matches(request.getMethod())) {
            return true;
        } else {
            response.sendError(HttpStatus.METHOD_NOT_ALLOWED.value());
            return false;
        }
    }
}
