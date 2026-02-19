package com.autoguide.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StaticResourcesConfig implements WebFluxConfigurer {

    private final String uploadsLocation;

    public StaticResourcesConfig(@Value("${app.uploads.base-path:${user.dir}/uploads}") String uploadsBasePath) {
        Path uploadsPath = Paths.get(uploadsBasePath).toAbsolutePath().normalize();
        String location = uploadsPath.toUri().toString();
        this.uploadsLocation = location.endsWith("/") ? location : location + "/";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsLocation);
    }
}
