package top.abosen.requestparamobject;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author qiubaisen
 * @date 2023/2/6
 */

@Configuration
public class SpringDocConfiguration {
    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("test")
                .packagesToScan("top.abosen")
                .build();
    }
}
