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
public class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws
                                                                                                                IOException {
        logRequestDetails(request, body);
        final ClientHttpResponse response = execution.execute(request, body);
        logResponseDetails(response);
        return response;
    }

    private static void logRequestDetails(HttpRequest request, byte[] body) {
        log.info("Request method: " + request.getMethod());
        log.info("Request URI: " + request.getURI());
        log.info("Request headers: " + request.getHeaders());
        log.info("Request body: " + new String(body));
    }

    private static void logResponseDetails(ClientHttpResponse response) throws IOException {
        log.info("Response status: " + response.getStatusCode());
        log.info("Response headers: " + response.getHeaders());
        log.info("Response body: " + response.getBody());
    }
}
