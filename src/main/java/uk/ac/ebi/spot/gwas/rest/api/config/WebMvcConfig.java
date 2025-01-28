package uk.ac.ebi.spot.gwas.rest.api.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.MultipartFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executor;


public class WebMvcConfig {

    @Configuration
    //@ConditionalOnExpression("!'${spring.profiles.active}'.equals('dev') && !'${spring.profiles.active}'.equals('test')")
    @ConditionalOnExpression("!'${spring.profiles.active}'.equals('test')")
    public static class SandboxWebMvcConfig implements WebMvcConfigurer {

        @Bean
        public Executor taskExecutor() {
            return new SimpleAsyncTaskExecutor();
        }



        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET", "POST", "PUT", "OPTIONS", "DELETE", "PATCH", "FETCH")
                    .allowedHeaders("*", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials");
        }
    }
}