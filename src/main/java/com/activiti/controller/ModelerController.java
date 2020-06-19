package com.activiti.controller;

import com.activiti.business.ModelerService;
import com.activiti.resource.ModelEditorJsonRestResource;
import com.activiti.resource.ModelSaveRestResource;
import com.activiti.resource.StencilsetRestResource;
import com.activiti.swagger.ModelerSwagger;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ModelerSwagger
@Api(tags = "流程审批")
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

    @ApiOperation("Activiti官方提供的在线定义模型相关接口")
    @GetMapping("/model/{modelId}/json")
    public ObjectNode getEditorJson(@PathVariable String modelId) {
        return modelEditorJsonRestResource.getEditorJson(modelId);
    }

    @ApiOperation("Activiti官方提供的在线定义模型相关接口")
    @PutMapping("/model/{modelId}/save")
    @ResponseStatus(value = HttpStatus.OK)
    public void saveModel(@PathVariable String modelId,
                          @RequestParam("name") String name,
                          @RequestParam("json_xml") String json_xml,
                          @RequestParam("svg_xml") String svg_xml,
                          @RequestParam("description") String description) {
        modelSaveRestResource.saveModel(modelId, name, json_xml, svg_xml, description);
    }

    @ApiOperation("Activiti官方提供的在线定义模型相关接口")
    @GetMapping("/editor/stencilset")
    public String getStencilset() {
        return stencilsetRestResource.getStencilset();
    }

    @ApiOperation("定义模型")
    @PostMapping
    public String modeler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return modelerService.modeler(request, response);
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

}
