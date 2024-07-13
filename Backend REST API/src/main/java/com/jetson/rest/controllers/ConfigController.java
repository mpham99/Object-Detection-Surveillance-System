package com.jetson.rest.controllers;

import com.jetson.rest.models.ConfigModel;
import com.jetson.rest.services.ConfigService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/detection")
@CrossOrigin
public class ConfigController {

    @Autowired private ConfigService service;

    @PostMapping("/config")
    @ApiOperation(
            value = "Add Selected Configuration for Detection",
            notes = "Add Selected Configuration for Detection",
            response = ConfigModel.class)
    public ResponseEntity<Object> postConfig(@RequestBody ConfigModel model) {
        ConfigModel existingModel = service.getConfigByHardware(model.getHardware());

        if (!model.isSet())
            return new ResponseEntity<>("Missing configuration", HttpStatus.BAD_REQUEST);

        if (existingModel == null) {
            // Post new config
            return new ResponseEntity<>(service.postModel(model), HttpStatus.ACCEPTED);
        } else {
            // Replace existing config
            existingModel.setModel(model.getModel());
            existingModel.setObjects(model.getObjects());
            existingModel.setCount(model.getCount());
            return new ResponseEntity<>(service.postModel(existingModel), HttpStatus.ACCEPTED);
        }
    }

    @PostMapping("/config/compare")
    @ApiOperation(
            value = "Compare Configuration for Detection",
            notes = "Compare Configuration for Detection",
            response = ConfigModel.class)
    public ResponseEntity<Object> checkConfig(@RequestBody ConfigModel model) {

        ConfigModel existingModel = service.getConfigByHardware(model.getHardware());

        // Model doesn't exist
        if (existingModel == null)
            return new ResponseEntity<>("Bad Request", HttpStatus.BAD_REQUEST);

        // No change to configuration
        if (existingModel.equals(model))
            return new ResponseEntity<>(false, HttpStatus.ACCEPTED);

        // Configuration changed
        return new ResponseEntity<>(existingModel, HttpStatus.OK);
    }
}
