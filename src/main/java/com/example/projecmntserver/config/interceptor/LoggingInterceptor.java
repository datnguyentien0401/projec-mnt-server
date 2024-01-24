package com.example.projecmntserver.config.interceptor;

import java.io.IOException;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        logRequestDetails(request, body);
        final ClientHttpResponse response = execution.execute(request, body);
        logResponseDetails(response);
        return response;
    }

    private static void logRequestDetails(HttpRequest request, byte[] body) {
        log.info("JIRA Request method: " + request.getMethod());
        log.info("JIRA Request URI: " + request.getURI().getPath());
        log.info("JIRA Request headers: " + request.getHeaders());
    }

    private static void logResponseDetails(ClientHttpResponse response) throws IOException {
        log.info("JIRA Response status: " + response.getStatusCode());
        log.info("JIRA Response headers: " + response.getHeaders());
    }

}
