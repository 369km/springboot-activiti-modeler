package com.activiti.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.task.TaskInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ModelerServiceImpl implements ModelerService {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;


    @Override
    public String modeler(HttpServletRequest request, HttpServletResponse response) throws IOException {
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
        return modelData.getId();
    }

    @Override
    public String deployment(String modelId) throws IOException {
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
        return model.getProcesses().get(0).getId();
    }

    @Override
    public String start(String processName) {
        return runtimeService.startProcessInstanceByKey(processName).getId();
    }

    @Override
    public void approval(String processInstanceId) {
        String taskId = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult().getId();
        taskService.complete(taskId);
    }

    @Override
    public List<String> pendingApproval(String assignee) {
        return taskService.createTaskQuery()
                .taskAssignee(assignee)
                .list()
                .stream()
                .map(TaskInfo::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> historyNode(String processInstanceId) {
        return historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .taskDeleteReason("completed")
                .orderByHistoricTaskInstanceEndTime()
                .desc()
                .list()
                .stream()
                .map(task -> {
                    Map<String, Object> history = new HashMap<>();
                    history.put("endTime", task.getEndTime());
                    history.put("assignee", task.getAssignee());
                    history.put("name", task.getName());
                    return history;
                })
                .collect(Collectors.toList());

    }
}
