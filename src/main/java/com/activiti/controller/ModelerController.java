package com.activiti.controller;

import com.activiti.business.ModelerService;
import com.activiti.resource.ModelEditorJsonRestResource;
import com.activiti.resource.ModelSaveRestResource;
import com.activiti.resource.StencilsetRestResource;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

    @GetMapping
    public void modeler(HttpServletRequest request, HttpServletResponse response) throws IOException {
        modelerService.modeler(request, response);
    }

    @GetMapping("/modelList")
    public void modelList(){

    }

}
