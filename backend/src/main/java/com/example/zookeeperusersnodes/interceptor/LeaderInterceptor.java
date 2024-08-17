package com.example.zookeeperusersnodes.interceptor;

import com.example.zookeeperusersnodes.annotation.LeaderOnly;
import com.example.zookeeperusersnodes.dto.ServerResponseDTO;
import com.example.zookeeperusersnodes.services.interfaces.NotificationService;
import com.example.zookeeperusersnodes.services.interfaces.ZooKeeperService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;

@Component
public class LeaderInterceptor implements HandlerInterceptor {
    @Autowired
    private ZooKeeperService zooKeeperService;
    @Autowired
    private RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final NotificationService notificationService;

    public LeaderInterceptor(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            Method method = ((HandlerMethod) handler).getMethod();
            if (method.isAnnotationPresent(LeaderOnly.class)) {
                if (!zooKeeperService.isThisNodeLeader(this.zooKeeperService.getCurrentZNode())) {
                    // Forward request to leader instance
                    String jsonResponse;
                    String leaderAddress = this.zooKeeperService.getLeaderAddress();

//                    System.out.println("METHOD type:" + request.getMethod());
//                    System.out.println("Request URI: " + request.getRequestURI());

                    String url = "http://" + leaderAddress + request.getRequestURI();
                    url = extractAndSetParameterNamesAndValues(request, url);

                    String requestBody = getBodyFromRequest(request);
//                    if(!requestBody.isEmpty()) {
//                        System.out.println("Request body: " + requestBody);
//                    }
//                    else {
//                        System.out.println("Request: No body provided");
//                    }

                    HttpEntity<String> entity = new HttpEntity<>(requestBody.isEmpty() ? null : requestBody, extractHeaders(request));

                    ResponseEntity<ServerResponseDTO> responseEntity = restTemplate.exchange(
                            url,
                            HttpMethod.valueOf(request.getMethod()),
                            entity,
                            ServerResponseDTO.class
                    );

                    try {
                        jsonResponse = objectMapper.writeValueAsString(responseEntity.getBody());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Error converting response object to JSON", e);
                    }

                    response.getWriter().write(jsonResponse);
                    response.setStatus(HttpServletResponse.SC_OK);

                    return false;
                }
            }
        }
        return true;
    }

    public HttpHeaders extractHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                headers.add(headerName, headerValues.nextElement());
            }
        }
        return headers;
    }

    public String extractAndSetParameterNamesAndValues(HttpServletRequest request, String url) {
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            url = url.concat("?" + paramName + "=" + paramValue);
//            System.out.println("Parameter Name: " + paramName + ", Value: " + paramValue);
        }

        return url;
    }

    public String getBodyFromRequest(HttpServletRequest request) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader;

        bufferedReader = request.getReader();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }

        bufferedReader.close();

        return stringBuilder.toString();
    }
}
