package cn.wolfcode.car.shiro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadFilePathConfig implements WebMvcConfigurer {
    @Value("${fileData.staticAccessPath}")
    private String staticAccessPath;
    @Value("${fileData.uploadRealPath}")
    private String uploadRealPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(staticAccessPath).addResourceLocations("file:"+uploadRealPath);
    }
}