package com.activiti.controller;

import com.activiti.business.ModelerService;
import com.activiti.swagger.ModelerSwagger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@ModelerSwagger
@Api(tags = "流程审批")
@RestController
@RequestMapping("/models")
public class ModelerController {
    @Autowired
    private ModelerService modelerService;

    @ApiOperation("定义模型")
    @GetMapping
    public void modeler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        modelerService.modeler(request, response);
    }

    @ApiOperation("发布模型")
    @PostMapping("/{modelId}/deployment")
    public String deployment(@PathVariable String modelId) throws IOException {
        return modelerService.deployment(modelId);
    }

    @ApiOperation("启动流程")
    @PostMapping("/start")
    public String start(@RequestParam String processName) {
        return modelerService.start(processName);
    }

    @ApiOperation("审批")
    @PostMapping("/{processInstanceId}/approval")
    public void Approval(@PathVariable String processInstanceId) {
        modelerService.approval(processInstanceId);
    }

    @ApiOperation("流程历史节点")
    @GetMapping("/{processInstanceId}/history")
    public List<Map<String, Object>> historyNode(@PathVariable String processInstanceId) {
        return modelerService.historyNode(processInstanceId);
    }
}
