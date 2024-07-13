package com.jetson.rest.services;

import com.jetson.rest.models.TrainedModel;
import com.jetson.rest.repositories.TrainedModelRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.springframework.util.StringUtils.isEmpty;

@Service
public class TrainedModelService {

    @Autowired private TrainedModelRepository repository;
    @Autowired private GridFsTemplate gridTemplate;
    @Autowired private GridFsOperations gridOperations;

    public List<TrainedModel> getAllModels() {
        return repository.findAll();
    }

    public TrainedModel getModelByName(String name) {
        return repository.findByName(name);
    }

    public TrainedModel postModel(TrainedModel model) {
        return repository.save(model);
    }

    public ArrayList<String> getObjectsByModel(TrainedModel model) throws IOException {

        Scanner file;
        ArrayList<String> objects = new ArrayList<>();

        // Get File
        InputStream inputStream = getFileByModelAndFileType(model, "names");
        if (inputStream == null) return null;

        // Read objects from file
        file = new Scanner(inputStream);
        while (file.hasNext())
            objects.add(file.nextLine());
        file.close();

        return objects;
    }

    public InputStream getFileByModelAndFileType(TrainedModel model, String fileType) throws IllegalStateException, IOException {

        // Get File ID
        String id = null;
        switch (fileType) {
            case "weights":
                id = model.getWeightsFileId();
                break;
            case "config":
                id = model.getConfigFileId();
                break;
            case "names":
                id = model.getNamesFileId();
                break;
        }

        // Check if file exists
        if (id == null)
            return null;

        // Return file
        GridFSFile gridFile = gridOperations.findOne(new Query(Criteria.where("_id").is(id)));
        return gridTemplate.getResource(gridFile).getInputStream();
    }

    public TrainedModel addFile(TrainedModel model, MultipartFile file) throws IOException {

        String type = file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf(".") + 1)
                .toLowerCase();

        // Check if file already exists
        switch (type) {
            case "weights":
                if (!isEmpty(model.getWeightsFileId())) return null;
                break;
            case "cfg":
                if (!isEmpty(model.getConfigFileId())) return null;
                break;
            case "names":
                if (!isEmpty(model.getNamesFileId())) return null;
                break;
        }

        // Build file meta-data
        DBObject metaData = new BasicDBObject();
        metaData.put("model", model.getName());
        metaData.put("filename", file.getOriginalFilename());
        metaData.put("type", type);

        // Store file in MongoDB
        String id = gridTemplate.store(
                file.getInputStream(),
                file.getName(),
                file.getContentType(),
                metaData
        ).toString();

        // Link file ID with model
        switch (type) {
            case "weights":
                model.setWeightsFileId(id);
                break;
            case "cfg":
                model.setConfigFileId(id);
                break;
            case "names":
                model.setNamesFileId(id);
                break;
        }
        repository.save(model);

        // Return updated model
        return model;
    }
}
