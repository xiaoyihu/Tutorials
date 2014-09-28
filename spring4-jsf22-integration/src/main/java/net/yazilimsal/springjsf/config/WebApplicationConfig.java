package net.yazilimsal.springjsf.config;

import net.yazilimsal.springjsf.jsf.viewscope.ViewScope;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan("net.yazilimsal.springjsf")
public class WebApplicationConfig {

    @Bean
    public static CustomScopeConfigurer customScopeConfigurer() {
        Map<String, Object> scopes = new HashMap<>();
        scopes.put("view", new ViewScope());

        CustomScopeConfigurer bean = new CustomScopeConfigurer();
        bean.setScopes(scopes);

        return bean;
    }

}
