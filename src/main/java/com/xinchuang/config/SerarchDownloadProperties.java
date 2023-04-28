package com.xinchuang.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class SerarchDownloadProperties {
//    @Value("${search.search-download-url}")
//    private String searchDownloadUrl;
    
    @Value("${search.config-path}")
    private String configPath;
}
