package top.abosen.requestparamobject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author qiubaisen
 * @date 2023/2/6
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = "spring.jackson.property-naming-strategy=SNAKE_CASE")
@AutoConfigureMockMvc
class ControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void should_request_with_param() throws Exception {
        mockMvc.perform(
                get("/request-param")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("query", "some query string")
                        .queryParam("nest.filter", "nested")
                        .queryParam("page", "1")
                        .queryParam("size", "10")
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.name").value("request-param"),
                jsonPath("$.query.query").isString(),
                jsonPath("$.query.nest.filter").value("nested"),
                jsonPath("$.query.page").isNumber()
        ).andReturn();
    }

    @Test
    void should_request_with_object() throws Exception {
        mockMvc.perform(
                get("/request-object")
                        .accept(MediaType.APPLICATION_JSON)
                        .queryParam("query", "some query string")
                        .queryParam("nest.filter", "nested")
                        .queryParam("page", "1")
                        .queryParam("size", "10")
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.name").value("request-object"),
                jsonPath("$.query.query").isString(),
                jsonPath("$.query.nest.filter").value("nested"),
                jsonPath("$.query.page").isNumber()
        ).andReturn();
    }

    @Test
    void should_return_openapi_doc_when_request_with_object() throws Exception {
        mockMvc.perform(
                get("/v3/api-docs/test")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk(),
                jsonPath("$.paths.['/request-object']").exists(),
                jsonPath("$.components.schemas.NestQuery.properties.filter.description")
                        .value("nest query filter")
        ).andReturn();
    }

}