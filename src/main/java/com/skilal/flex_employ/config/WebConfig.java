package com.skilal.flex_employ.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

//文件上传的本地存储，已废弃
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @org.springframework.beans.factory.annotation.Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/refresh",
                        "/error",
                        "/uploads/**",
                        "/static/**",
                        "/templates/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射 /uploads/** 到项目根目录下的 uploads 文件夹
        String path = "file:" + System.getProperty("user.dir") + File.separator + "uploads" + File.separator;
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(path);
    }
}
