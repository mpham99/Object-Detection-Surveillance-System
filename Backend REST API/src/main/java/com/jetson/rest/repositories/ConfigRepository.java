package com.jetson.rest.repositories;

import com.jetson.rest.models.ConfigModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends MongoRepository<ConfigModel, String> {
    ConfigModel findFirstByHardware(String hardware);
}
