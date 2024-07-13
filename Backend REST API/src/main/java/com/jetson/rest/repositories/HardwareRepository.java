package com.jetson.rest.repositories;

import com.jetson.rest.models.HardwareModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HardwareRepository extends MongoRepository<HardwareModel, String> {
    HardwareModel findByName(String name);
}
