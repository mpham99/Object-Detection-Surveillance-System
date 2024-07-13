package com.jetson.rest.services;

import com.jetson.rest.models.ConfigModel;
import com.jetson.rest.repositories.ConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigService {

    @Autowired private ConfigRepository repository;

    public ConfigModel getConfigByHardware(String hardware) {
        return repository.findFirstByHardware(hardware);
    }

    public ConfigModel postModel(ConfigModel model) {
        return repository.save(model);
    }

}
