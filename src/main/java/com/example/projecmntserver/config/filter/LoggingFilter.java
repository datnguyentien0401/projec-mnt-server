package com.example.projecmntserver.config.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.CharUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@WebFilter(urlPatterns = "/v1/*")
@Configuration
public class LoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        doLoggingFilter(wrap(request), wrap(response), filterChain);

        filterChain.doFilter(request, response);
    }

    private static void doLoggingFilter(ContentCachingRequestWrapper request,
                                        ContentCachingResponseWrapper response,
                                        FilterChain filterChain) throws ServletException, IOException {

        final StopWatch stopWatch = new StopWatch();
        try {
            stopWatch.start();
            filterChain.doFilter(request, response);
        } finally {
            try {
                stopWatch.stop();
                final StringBuilder buffer = new StringBuilder("\n");
                writeLog(request, response, buffer);
                log.info(buffer.toString());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            response.copyBodyToResponse();
        }
    }

    private static void writeLog(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
                                 StringBuilder buffer) {
        writeRequestLog(request, buffer);

        final int status = response.getStatus();

        buffer.append("[ API-RESPONSE ] ")
              .append(CharUtils.LF)
              .append(status).append(' ')
              .append(HttpStatus.valueOf(status).getReasonPhrase())
              .append(CharUtils.LF);

        writeBody(response.getContentAsByteArray(), buffer);
    }

    private static void writeRequestLog(ContentCachingRequestWrapper request, StringBuilder buffer) {

        buffer.append("[ API-REQUEST ] ")
              .append(CharUtils.LF);

        writeRequestUri(request, buffer);
        writeBody(request.getContentAsByteArray(), buffer);
    }

    private static void writeRequestUri(ContentCachingRequestWrapper request, StringBuilder buffer) {
        final String queryString = request.getQueryString();

        buffer.append(request.getMethod()).append(' ')
              .append(request.getRequestURI());

        if (!Objects.isNull(queryString)) {
            buffer.append('?').append(queryString);
        }
        buffer.append(CharUtils.LF);
    }

    private static void writeBody(byte[] content, StringBuilder buffer) {
        if (Objects.isNull(content) || content.length < 1) {
            return;
        }
        buffer.append("BODY [ ");
        buffer.append(new String(content, StandardCharsets.UTF_8))
              .append(" ]\n");
    }

    private static ContentCachingRequestWrapper wrap(HttpServletRequest request) {
        if (request instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) request;
        }
        return new ContentCachingRequestWrapper(request);
    }

    private static ContentCachingResponseWrapper wrap(HttpServletResponse response) {
        if (response instanceof ContentCachingResponseWrapper) {
            return (ContentCachingResponseWrapper) response;
        }
        return new ContentCachingResponseWrapper(response);
    }

}
