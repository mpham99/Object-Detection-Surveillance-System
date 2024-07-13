package com.jetson.rest.controllers;

import com.jetson.rest.models.HardwareModel;
import com.jetson.rest.services.HardwareService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/hardware")
@CrossOrigin
public class HardwareController {

    @Autowired private HardwareService service;

    @GetMapping()
    @ApiOperation(
            value = "Get all Hardware",
            notes = "Get list of all Hardware",
            response = HardwareModel.class)
    public List<HardwareModel> getAllHardware() {
        return service.getAllHardware();
    }

    @GetMapping("/{id}")
    @ApiOperation(
            value = "Get Hardware by ID",
            notes = "Get Hardware by ID",
            response = HardwareModel.class)
    public HardwareModel getHardwareById(@PathVariable("id") String id) {
        Optional<HardwareModel> hw;
        hw = service.getHardwareById(id);
        return hw.orElse(null);
    }

    @GetMapping("/key")
    @ApiOperation(
            value = "Get Hardware Stream Key by Name",
            notes = "Get Stream Key of Hardware by Name",
            response = String.class)
    public String getHardwareStreamKeyByName(@RequestParam(name = "name") String name) {
        HardwareModel hw = service.getHardwareByName(name);
        return (hw == null) ? null : hw.getStreamKey();
    }

    @PostMapping()
    @ApiOperation(
            value = "Add new Hardware",
            notes = "Used by Hardware to register itself",
            response = HardwareModel.class)
    public ResponseEntity<Object> addHardware(@RequestBody HardwareModel hwModel) {

        // Missing details
        if (hwModel.getName() == null || "".equals(hwModel.getName()))
            return new ResponseEntity<>("Can't register empty model", HttpStatus.BAD_REQUEST);

        HardwareModel existingModel = service.getHardwareByName(hwModel.getName());

        // Model already exists
        if (existingModel != null)
            return new ResponseEntity<>("This device is already registered", HttpStatus.BAD_REQUEST);

        // Posted successfully
        return new ResponseEntity<>(service.postHardwareModel(hwModel), HttpStatus.ACCEPTED);
    }

    @PutMapping("/key/{name}")
    @ApiOperation(
            value = "Update Hardware stream key",
            notes = "Update Hardware stream key",
            response = String.class)
    public ResponseEntity<Object> updateStreamKey(@PathVariable("name") String name, @RequestBody String newStreamKey) {

        // Missing Details
        if (newStreamKey == null)
            return new ResponseEntity<>("Can't update with empty stream key", HttpStatus.BAD_REQUEST);

        HardwareModel hw = service.getHardwareByName(name);

        // Hardware doesn't exist
        if (hw == null)
            return new ResponseEntity<>("Can't update stream key of a non-existent device", HttpStatus.BAD_REQUEST);

        // Successfully updated Stream Key
        hw.setStreamKey(newStreamKey);
        service.postHardwareModel(hw);
        return new ResponseEntity<>("Device's stream key was successfully updated", HttpStatus.OK);
    }

}
