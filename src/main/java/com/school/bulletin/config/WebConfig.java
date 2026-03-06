package com.school.bulletin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // On protège tout SAUF le login et les fichiers statiques (CSS/images)
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") 
                .excludePathPatterns("/login", "/css/**", "/js/**", "/error/**");
    }
}