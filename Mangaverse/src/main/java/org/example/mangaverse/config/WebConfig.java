package org.example.mangaverse.config;

import java.nio.file.Path;
import java.time.Clock;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.CacheControl;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String adminToken;
    private final Path mediaDir;

    public WebConfig(@Value("${mangaverse.admin-token:}") String adminToken,
                     @Value("${mangaverse.media-dir:media}") String mediaDir) {
        this.adminToken = adminToken;
        this.mediaDir = Path.of(mediaDir).toAbsolutePath().normalize();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    /** Uploaded chapter pages. Each upload gets a new path, so files never change and cache forever. */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/uploads/**")
                .addResourceLocations(mediaDir.toUri().toString())
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Catalog writes only; /api/me is the user's own data and is protected by Spring Security.
        registry.addInterceptor(new AdminTokenInterceptor(adminToken)).addPathPatterns("/api/manga/**");
    }
}
