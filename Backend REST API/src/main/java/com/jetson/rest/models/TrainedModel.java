package com.jetson.rest.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "trained_models")
public class TrainedModel {

    @Id private String id;
    private String name;
    private String weights;
    private String weightsFileId;
    private String config;
    private String configFileId;
    private String names;
    private String namesFileId;
}
