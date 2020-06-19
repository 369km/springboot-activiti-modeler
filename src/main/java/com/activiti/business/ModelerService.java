package com.activiti.business;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ModelerService {
    /**
     * @return modelId 模型id
     */
    String modeler(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * @param modelId 模型id
     * @return processName 流程名称
     */
    String deployment(String modelId) throws IOException;

    /**
     * @param processName 流程名称
     * @return processInstanceId 流程实例id
     */
    String start(String processName);

    /**
     * @param processInstanceId 流程实例id
     */
    void approval(String processInstanceId);
}
