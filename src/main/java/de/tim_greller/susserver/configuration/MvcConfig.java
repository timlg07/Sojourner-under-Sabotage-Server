package de.tim_greller.susserver.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${paths.api}") String apiUrl;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("home");
        registry.addViewController("/home").setViewName("home");
        registry.addViewController("/hello").setViewName("hello");
        registry.addViewController("/game").setViewName("game");
        registry.addViewController("/editor").setViewName("editor");
        registry.addViewController("/error").setViewName("error");
        registry.addViewController("/admin").setViewName("admin");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(apiUrl + "/**");
    }

}
