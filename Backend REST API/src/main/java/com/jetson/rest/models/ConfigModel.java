package com.jetson.rest.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Arrays;

@Getter
@Setter
@Document(collection = "config")
public class ConfigModel {

    @Id private String id;
    private String hardware;
    private String model;
    private String[] objects;
    private String[] count;

    public ConfigModel() { }

    public ConfigModel(String hardware) {
        this.hardware = hardware;
    }

    public ConfigModel(String hardware, String model, String[] objects, String[] count) {
        this.hardware = hardware;
        this.model = model;
        this.objects = objects;
        this.count = count;
    }

    public boolean isSet() {
        return hardware != null
                && model != null
                && objects != null
                && count != null;
    }

    public boolean equals(ConfigModel configModel) {
        return model.equals(configModel.model)
                && Arrays.equals(objects, configModel.objects)
                && Arrays.equals(count, configModel.count);
    }

}
