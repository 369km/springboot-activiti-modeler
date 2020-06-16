## 依赖
```maven
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.7.RELEASE</version>
    </parent>

    <properties>
        <activiti.version>5.22.0</activiti.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <!--activiti启动器-->
        <dependency>
            <groupId>org.activiti</groupId>
            <artifactId>activiti-spring-boot-starter-basic</artifactId>
            <version>${activiti.version}</version>
        </dependency>
        <!--activiti流程图-->
        <dependency>
            <groupId>org.activiti</groupId>
            <artifactId>activiti-modeler</artifactId>
            <version>${activiti.version}</version>
        </dependency>
        <!--activiti在线设计-->
        <dependency>
            <groupId>org.activiti</groupId>
            <artifactId>activiti-diagram-rest</artifactId>
            <version>${activiti.version}</version>
        </dependency>
    </dependencies>
```

## 配置
```yml
server:
  port: 80
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/activiti?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&useSSL=true&nullCatalogMeansCurrent=true
    username: root
    password: 123456
  activiti:
    check-process-definitions: false
```

## 修改activiti官方提供的源码
1. 进入源码文件中的modules\activiti-webapp-explorer2\src\main\webapp目录，
复制diagram-viewer、editor-app、modeler.html三个文件到springboot项目中的resources目录下的static目录下。
2. 解压activiti-5.22.0.zip，在Activiti-5.22.0的libs中找到activiti-modeler-5.22.0-sources.jar，
将其解压，将会找到以下三个类：ModelEditorJsonRestResource,ModelSaveRestResource,StencilsetRestResource。
添加注解：@RestController,@RequestMapping("/service")
3. 修改 resources目录下的static/editor-app/app-cfg.js,
4. 将源码路径modules\activiti-webapp-explorer2\src\main\resources\stencilset.json复制到springboot项目中的resources目录下

```js
ACTIVITI.CONFIG = {
    'contextRoot' : '/service',
};
```

## 配置静态资源
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
```

## 配置activiti
```java
@Configuration
public class ActivitiConfig {
    @Bean
    public ProcessEngineConfiguration processEngineConfiguration(DataSource dataSource, PlatformTransactionManager transactionManager) {
        SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
        processEngineConfiguration.setDataSource(dataSource);
        processEngineConfiguration.setDatabaseSchemaUpdate("true");
        processEngineConfiguration.setDatabaseType("mysql");
        processEngineConfiguration.setTransactionManager(transactionManager);
        processEngineConfiguration.setActivityFontName("宋体");
        processEngineConfiguration.setAnnotationFontName("宋体");
        processEngineConfiguration.setLabelFontName("宋体");
        return processEngineConfiguration;
    }

    @Bean
    public ProcessEngineFactoryBean processEngineInstance(ProcessEngineConfiguration processEngineConfiguration) {
        ProcessEngineFactoryBean processEngineFactoryBean = new ProcessEngineFactoryBean();
        processEngineFactoryBean.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) processEngineConfiguration);
        return processEngineFactoryBean;
    }

    @Autowired
    private ProcessEngine processEngineInstance;

    @Bean
    public RepositoryService repositoryService() {
        return processEngineInstance.getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService() {
        return processEngineInstance.getRuntimeService();
    }

    @Bean
    public TaskService taskService() {
        return processEngineInstance.getTaskService();
    }

    @Bean
    public HistoryService historyService() {
        return processEngineInstance.getHistoryService();
    }

    @Bean
    public FormService formService() {
        return processEngineInstance.getFormService();
    }

    @Bean
    public IdentityService identityService() {
        return processEngineInstance.getIdentityService();
    }

    @Bean
    public ManagementService managementService() {
        return processEngineInstance.getManagementService();
    }

    @Bean
    public DynamicBpmnService dynamicBpmnService() {
        return processEngineInstance.getDynamicBpmnService();
    }
}
```

## 入口
1. 源码
```java
@RestController
@RequestMapping("/process")
public class ModelerController {
    @Autowired
    private RepositoryService repositoryService;

    @GetMapping
    public void modeler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.set("stencilset", stencilSetNode);

        long dateTime = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        String name = "P" + dateTime;
        ObjectNode modelObjectNode = objectMapper.createObjectNode();
        modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, 1);
        modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, "");

        Model modelData = repositoryService.newModel();
        modelData.setMetaInfo(modelObjectNode.toString());
        modelData.setName(name);
        modelData.setKey(String.valueOf(dateTime));
        repositoryService.saveModel(modelData);

        repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
        response.sendRedirect(request.getContextPath() + "/modeler.html?modelId=" + modelData.getId());
    }
}
```
2. 启动类配置(配置了两个包扫描和排除了activiti自带的安全认证)
```java
@SpringBootApplication(
        scanBasePackages = {"com.activity", "com.foo"},
        exclude = {org.activiti.spring.boot.SecurityAutoConfiguration.class, SecurityAutoConfiguration.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```
3. 访问地址：http:localhost:80/process