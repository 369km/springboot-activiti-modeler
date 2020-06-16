package com.activity.config;

import org.activiti.engine.*;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

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
