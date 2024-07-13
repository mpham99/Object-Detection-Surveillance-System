package com.jetson.rest.controllers;

import com.jetson.rest.models.TrainedModel;
import com.jetson.rest.services.TrainedModelService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/models")
@CrossOrigin
public class TrainedModelController {

    @Autowired private TrainedModelService service;

    @GetMapping()
    @ApiOperation(
            value = "Get all Trained Models",
            notes = "Get all Trained Models",
            response = TrainedModel.class)
    public List<TrainedModel> getAllModels() {
        return service.getAllModels();
    }

    @PostMapping()
    @ApiOperation(
            value = "Add Trained Model",
            notes = "Add Trained Model",
            response = TrainedModel.class)
    public ResponseEntity<Object> postModel(@RequestBody TrainedModel trainedModel) {
        TrainedModel existingModel = service.getModelByName(trainedModel.getName());

        // Model already exists
        if (existingModel != null)
            return new ResponseEntity<>("Trained Model with that name already exists", HttpStatus.BAD_REQUEST);

        // Successfully posted model
        return new ResponseEntity<>(service.postModel(trainedModel), HttpStatus.ACCEPTED);
    }

    @GetMapping("objects")
    @ApiOperation(
            value = "Get Detection Objects from Trained Model by Model Name",
            notes = "Get List of Objects that Trained Model is able to detect by Model Name",
            response = String.class)
    public ResponseEntity<Object> getObjects(@RequestParam("model") String modelName) throws IOException {

        // Check if model exists to retrieve file
        TrainedModel model = service.getModelByName(modelName);
        if (model == null)
            return new ResponseEntity<>("Model \"" + modelName + "\" doesn't exist", HttpStatus.BAD_REQUEST);

        // Check if file exists
        ArrayList<String> objects = service.getObjectsByModel(model);
        if (objects == null)
            return new ResponseEntity<>("Names file for model \"" + modelName + "\" doesn't exist", HttpStatus.BAD_REQUEST);

        // Success
        return new ResponseEntity<>(objects, HttpStatus.OK);
    }

    @GetMapping(value = "/files", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ApiOperation(
            value = "Download File from Trained Model by Model Name and File Type",
            notes = "Download File from Trained Model by Model Name and File Type",
            response = Byte[].class)
    public ResponseEntity<Object> getModel(
            @RequestParam("model") String modelName,
            @RequestParam("file-type") String fileType) throws IOException {

        // Check if model exists to retrieve file
        TrainedModel model = service.getModelByName(modelName);
        if (model == null)
            return new ResponseEntity("Model \"" + modelName + "\" doesn't exist", HttpStatus.BAD_REQUEST);

        // Check if file type matches a model file type
        switch (fileType) {
            case "weights":
            case "config":
            case "names":
                break;
            default:
                return new ResponseEntity("File type \"" + fileType + "\" doesn't exist", HttpStatus.BAD_REQUEST);
        }

        // Check if file ID exists
        InputStream file = service.getFileByModelAndFileType(model, fileType);
        if (file == null)
            return new ResponseEntity("Model \"" + modelName + "\" doesn't contain a \"" + fileType + "\" file", HttpStatus.BAD_REQUEST);

        // Return file contents (byte stream)
        return new ResponseEntity(IOUtils.toByteArray(file), HttpStatus.OK);
    }

    @PostMapping("/files")
    @ApiOperation(
            value = "Add a File for Trained Model by Model Name",
            notes = "Add a File for Trained Model by Model Name",
            response = TrainedModel.class)
    public ResponseEntity<Object> postModel(
            @RequestParam("model") String modelName,
            @RequestParam("file") MultipartFile file) throws IOException {

        // Check if model exists to post file
        TrainedModel model = service.getModelByName(modelName);
        if (model == null)
            return new ResponseEntity("Model \"" + modelName + "\" doesn't exist", HttpStatus.BAD_REQUEST);

        // Check if file already exists
        model = service.addFile(model, file);
        if (model == null)
            return new ResponseEntity("File already exists for this model", HttpStatus.BAD_REQUEST);

        // Add file to MongoDB & link to model
        return new ResponseEntity(model, HttpStatus.ACCEPTED);
    }
}
