package cn.nmefc.ocean.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 开发环境跨域配置；生产环境应收紧为实际前端域名。 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**").allowedOrigins("http://localhost:8080", "http://localhost:5173").allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
