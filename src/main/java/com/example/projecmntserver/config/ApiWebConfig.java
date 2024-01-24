package com.example.projecmntserver.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ApiWebConfig implements WebMvcConfigurer {
    @Value("${web.config.cors-allow-all}")
    private boolean isCorsAllowAll;
    @Value("#{'${web.config.corsOrigin}'.split(',')}")
    private List<String> rawOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (isCorsAllowAll) {
            registry.addMapping("/**").allowedOrigins("*").allowedMethods("*").allowedHeaders("*");
        } else {
            registry.addMapping("/**")
                    .allowCredentials(Boolean.TRUE)
                    .allowedOrigins(getOrigin())
                    .allowedMethods("*")
                    .allowedHeaders("*");
        }
    }

    public String[] getOrigin() {
        final String[] arrOrigin = new String[rawOrigins.size()];
        return rawOrigins.toArray(arrOrigin);
    }
}
