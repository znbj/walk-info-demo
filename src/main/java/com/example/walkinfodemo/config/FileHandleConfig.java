package com.example.walkinfodemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * @ClassName FileHandleConfig
 * @Author znb
 * @Date 2021-03-07 22:43
 * @Description FileHandleConfig
 * @Version 1.0
 */
@Configuration
public class FileHandleConfig extends WebMvcConfigurationSupport {

    @Value("${config.upload-location}")
    private String fileUrl;


    @Value("${config.static-access-path}")
    private String fileUrlNew;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取对应的系统
        String os = System.getProperty("os.name");
        // 如果是window系统
        if (os.toLowerCase().startsWith("win")) {
            registry.addResourceHandler(fileUrlNew+"**").addResourceLocations("file:" + fileUrl);
            registry.addResourceHandler("/**").addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/static/");
        } else {
            registry.addResourceHandler(fileUrlNew+"**").addResourceLocations("file:" + fileUrl);
            registry.addResourceHandler("/**").addResourceLocations(ResourceUtils.CLASSPATH_URL_PREFIX + "/static/");
        }
        super.addResourceHandlers(registry);
    }

}
