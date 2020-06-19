package com.activiti.config;

import com.activiti.swagger.ModelerSwagger;
import com.google.common.collect.Sets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.*;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.Set;

import static springfox.documentation.builders.PathSelectors.any;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    protected static final Contact contact = new Contact("foo", "", "");
    protected static final String LICENCE = "Foo Licence";
    protected Set<String> jsonProduces = Sets.newHashSet(new String[]{"application/json"});
    protected Set<String> streamProduces = Sets.newHashSet(new String[]{"application/octet-stream"});

    @Bean
    UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder().deepLinking(true).displayOperationId(false).defaultModelsExpandDepth(1)
                .defaultModelExpandDepth(1).defaultModelRendering(ModelRendering.MODEL).displayRequestDuration(false)
                .docExpansion(DocExpansion.NONE).filter(false).maxDisplayedTags((Integer)null).operationsSorter(OperationsSorter.ALPHA)
                .showExtensions(false).tagsSorter(TagsSorter.ALPHA).supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS).validatorUrl((String)null).build();
    }

    @Bean
    public Docket modelerApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("流程审批")
                .apiInfo(modelerInfo())
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(ModelerSwagger.class))
                .paths(any())
                .build()
                .genericModelSubstitutes(ResponseEntity.class)
                .useDefaultResponseMessages(false)
                .enableUrlTemplating(false)
                .produces(jsonProduces);
    }

    private ApiInfo modelerInfo() {
        return new ApiInfo("流程审批API",
                "模型定义、流程发起、流程审批等api",
                "1.0.0", "no", contact, LICENCE, "", new ArrayList<>());
    }
}
