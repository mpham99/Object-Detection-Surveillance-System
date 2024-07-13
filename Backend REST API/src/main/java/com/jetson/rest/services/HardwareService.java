package com.jetson.rest.services;

import com.jetson.rest.models.HardwareModel;
import com.jetson.rest.repositories.HardwareRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HardwareService {

    @Autowired private HardwareRepository repository;

    public List<HardwareModel> getAllHardware() {
        return repository.findAll();
    }

    public Optional<HardwareModel> getHardwareById(String id) {
        return repository.findById(id);
    }

    public HardwareModel getHardwareByName(String name) {
        return repository.findByName(name);
    }

    public HardwareModel postHardwareModel(HardwareModel hwModel){
        return repository.save(hwModel);
    }
}
