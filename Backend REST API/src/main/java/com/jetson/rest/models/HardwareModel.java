package com.jetson.rest.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "hardware")
public class HardwareModel {

    @Id private String id;
    private String name;
    private String serial;
    private String streamKey;

    public HardwareModel() { }

    public HardwareModel(String name, String serial) {
        this.name = name;
        this.serial = serial;
    }

    public boolean isEmpty() {
        return name.isEmpty() || serial.isEmpty();
    }
}
