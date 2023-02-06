package top.abosen.requestparamobject;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author qiubaisen
 * @date 2023/2/6
 */
@RestController
@Tag(name = "Request Param Object")
public class RequestParamObjectController {

    @GetMapping("/request-param")
    public Result query(
            @RequestParam(required = false) String query,
            @RequestParam(name = "nest.filter", required = false) String filter,
            @RequestParam(required = false) int page,
            @RequestParam(required = false) int size
    ) {
        Query cmd = Query.builder().query(query).nest(new NestQuery(filter)).page(page).size(size).build();
        return new Result(cmd, "request-param");
    }

    @GetMapping("/request-object")
    @Operation(summary = "query by request object")
    public Result query(Query query) {
        return new Result(query, "request-object");
    }


    @Data
    @Builder
    public static class Query {
        @Schema(description = "query name")
        String query;
        NestQuery nest;
        int page;
        int size;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NestQuery {
        @Schema(description = "nest query filter")
        String filter;
    }

    @Value
    public static class Result {
        Query query;
        String name;
    }
}
