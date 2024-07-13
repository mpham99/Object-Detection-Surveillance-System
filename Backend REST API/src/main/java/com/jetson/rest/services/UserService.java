package com.jetson.rest.services;

import com.jetson.rest.models.UserModel;
import com.jetson.rest.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired private UserRepository repository;

    public UserModel addUser(UserModel user) {
        return repository.save(user);
    }

    public List<UserModel> findAll() {
        return repository.findAll();
    }

    public UserModel findByUsername(String username) {
        return repository.findByUsername(username);
    }

    public boolean save(UserModel user) {
        repository.save(user);
        return true;
    }
}
