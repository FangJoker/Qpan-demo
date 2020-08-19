package cn.chavez.qpan.core.config;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/5/21 22:38
 */
@Configuration
@EnableSwagger2
public class Swagger {
    @Value("${swagger.host}")
    private String swaggerHost;

    @Bean
    public Docket createRestApi() {
        List<Parameter> pars = new ArrayList<Parameter>();
        ParameterBuilder tokenPar = new ParameterBuilder();
        tokenPar.name("Access-Token").description("Access-Token安全验证")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .defaultValue("eyJhbGciOiJIUzUxMiJ9.eyJleHAiOjE1ODU2NTM5MDMsInV1aWQiOiIxNjMzNDk0MzczMzgxMWVhOTI5MDAyNDJhYzEyMDAwNiJ9.HOsHGllmNTQ1ulEisXtrQsA-Yk3leY08ULZe4hFrfuq87s0KzKih2oajkp3cRAwhv-MtYJxtn_R-sb0T0_n2zw")
                .required(false).build();
        pars.add(tokenPar.build());

        Docket docket = new Docket(DocumentationType.SWAGGER_2);
        docket.forCodeGeneration(true)
                .select()
                .apis(RequestHandlerSelectors.basePackage("cn.chavez.qpan"))
                //过滤生成链接
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(pars)
                .useDefaultResponseMessages(false).
                securitySchemes(Collections.singletonList(apiKey()))
                .apiInfo(apiInfo());
        if (StringUtils.isNotBlank(swaggerHost)) {
            docket = docket.host(swaggerHost);
        }
        return docket;
    }

    private ApiInfo apiInfo() {
        springfox.documentation.service.Contact contact = new springfox.documentation.service.Contact("chavez", "", "");
        return new ApiInfoBuilder().title("趣盘").description("服务端接口文档").contact(contact).version("1.0.0").build();
    }

    @Bean
    public SecurityScheme apiKey() {
        return new ApiKey("access_token", "X-Access-Token", "header");
    }
}
