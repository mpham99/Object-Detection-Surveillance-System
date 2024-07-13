package com.jetson.rest.repositories;

import com.jetson.rest.models.TrainedModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainedModelRepository extends MongoRepository<TrainedModel, String> {
    TrainedModel findByName(String name);
}
