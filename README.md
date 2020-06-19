# 简单使用
项目拉下拉只需要配置数据库，然后访问http:localhost/models,即可

# 从零开始
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

## 修改activiti官方提供的源码(进入官网https://www.activiti.org/before-you-start，下载源码)
1. 进入源码文件中的modules\activiti-webapp-explorer2\src\main\webapp目录，
复制diagram-viewer、editor-app、modeler.html三个文件到springboot项目中的resources目录下的static目录下。
2. 解压activiti-5.22.0.zip，在Activiti-5.22.0的libs中找到activiti-modeler-5.22.0-sources.jar，
将其解压，将会找到以下三个类：ModelEditorJsonRestResource,ModelSaveRestResource,StencilsetRestResource。
复制resource包下，改造成符合springboot的service
3.修改ModelSaveRestResource
```java
@Service
public class ModelSaveRestResource implements ModelDataJsonConstants {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ModelSaveRestResource.class);
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ObjectMapper objectMapper;

    public void saveModel(String modelId, String name, String json_xml, String svg_xml, String description) {
        try {
            Model model = repositoryService.getModel(modelId);
            ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());

            modelJson.put(MODEL_NAME, name);
            modelJson.put(MODEL_DESCRIPTION, description);
            model.setMetaInfo(modelJson.toString());
            model.setName(name);

            repositoryService.saveModel(model);
            repositoryService.addModelEditorSource(model.getId(), json_xml.getBytes("utf-8"));

            InputStream svgStream = new ByteArrayInputStream(svg_xml.getBytes("utf-8"));
            TranscoderInput input = new TranscoderInput(svgStream);

            PNGTranscoder transcoder = new PNGTranscoder();
            // Setup output
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outStream);

            // Do the transformation
            transcoder.transcode(input, output);
            final byte[] result = outStream.toByteArray();
            repositoryService.addModelEditorSourceExtra(model.getId(), result);
            outStream.close();

        } catch (Exception e) {
            LOGGER.error("Error saving model", e);
            throw new ActivitiException("Error saving model", e);
        }
    }
}
```
4. 修改 resources目录下的static/editor-app/app-cfg.js,
```js
ACTIVITI.CONFIG = {
    'contextRoot' : '/models',
};
```
5. 将源码路径modules\activiti-webapp-explorer2\src\main\resources\stencilset.json复制到springboot项目中的resources目录下,
汉化就是将json中对应名称替换成中文

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
@RequestMapping("/models")
public class ModelerController {
    @Autowired
    private ModelEditorJsonRestResource modelEditorJsonRestResource;
    @Autowired
    private ModelSaveRestResource modelSaveRestResource;
    @Autowired
    private StencilsetRestResource stencilsetRestResource;
    @Autowired
    private ModelerService modelerService;

    @GetMapping("/model/{modelId}/json")
    public ObjectNode getEditorJson(@PathVariable String modelId) {
        return modelEditorJsonRestResource.getEditorJson(modelId);
    }

    @PutMapping("/model/{modelId}/save")
    @ResponseStatus(value = HttpStatus.OK)
    public void saveModel(@PathVariable String modelId,
                          @RequestParam("name") String name,
                          @RequestParam("json_xml") String json_xml,
                          @RequestParam("svg_xml") String svg_xml,
                          @RequestParam("description") String description) {
        modelSaveRestResource.saveModel(modelId, name, json_xml, svg_xml, description);
    }

    @GetMapping("/editor/stencilset")
    public String getStencilset() {
        return stencilsetRestResource.getStencilset();
    }

    //定义模型
    @GetMapping
    public void modeler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        modelerService.modeler(request, response);
    }
    
    //发布模型
    @PostMapping("/{modelId}/deployment")
    public void deployment(@PathVariable String modelId) throws IOException {
        modelerService.deployment(modelId);
    }
}
```
2. 业务实现
```java
@Service
public class ModelerServiceImpl implements ModelerService {
    @Autowired
    private RepositoryService repositoryService;

    @Override
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

    @Override
    public void deployment(String modelId) throws IOException {
        Model modelData = repositoryService.getModel(modelId);
        byte[] modelEditorSource = repositoryService.getModelEditorSource(modelData.getId());
        Assert.notNull(modelEditorSource, "模型中未定义流程");

        JsonNode modelNode = new ObjectMapper().readTree(modelEditorSource);
        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        Assert.notEmpty(model.getProcesses(), "模型中未定义流程");
        byte[] modelXml = new BpmnXMLConverter().convertToXML(model);

        //发布
        String processName = modelData.getName() + ".bpmn20.xml";
        Deployment deployment = repositoryService.createDeployment()
                .name(modelData.getName())
                .addString(processName, new String(modelXml, "UTF-8"))
                .deploy();
        modelData.setDeploymentId(deployment.getId());
        repositoryService.saveModel(modelData);
    }
}
```
3. 启动类配置(排除了activiti自带的安全认证)
```java
@SpringBootApplication(
        exclude = {org.activiti.spring.boot.SecurityAutoConfiguration.class, SecurityAutoConfiguration.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```
4. 访问地址：http:localhost:80/process