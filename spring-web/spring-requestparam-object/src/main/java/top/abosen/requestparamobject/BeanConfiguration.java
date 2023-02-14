package top.abosen.requestparamobject;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * @author qiubaisen
 * @date 2023/2/6
 */

@Configuration
public class BeanConfiguration {
    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
                .group("test")
                .packagesToScan("top.abosen")
                .build();
    }

    @Bean
    public OncePerRequestFilter filter(){
        return new SnakeCaseParamFilter();
    }
}
