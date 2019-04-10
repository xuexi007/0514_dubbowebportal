package com.offcn.config;

import com.offcn.filter.LoginFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigFilter {

    @Bean
    public FilterRegistrationBean someFilterRegistration() {

        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(getLoginFilter());
        registration.addUrlPatterns("/api/order/*");
        registration.addUrlPatterns("/api/user/*");

        registration.setOrder(1);
        return registration;
    }

    @Bean
    public LoginFilter getLoginFilter(){

        return new LoginFilter();
    }
}
