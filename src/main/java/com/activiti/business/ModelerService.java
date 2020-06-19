package com.activiti.business;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    /**
     * @param assignee 审批人
     * @return 待审批任务(taskId)列表
     */
    List<String> pendingApproval(String assignee);

    /**
     *
     * @param processInstanceId 流程实例id
     * @return 历史任务
     */
    List<Map<String, Object>> historyNode(String processInstanceId);
}
