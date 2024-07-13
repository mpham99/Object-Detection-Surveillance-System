package com.jetson.rest.controllers;

import com.jetson.rest.models.UserModel;
import com.jetson.rest.services.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserService service;

    @PostMapping("/add")
    @ResponseBody
    @ApiOperation(
            value = "Add a user",
            notes = "Provide username and password to be added for use with login endpoint")
    public String add(@RequestBody UserModel user) {

        String rawPassword = user.getPassword();
        user.setPassword("{bcrypt}" + passwordEncoder.encode(rawPassword));

        // Username already exists
        if (service.findByUsername(user.getUsername()) != null)
            return "A user with this username already exists";

        return service.save(user) ? "User added" : "Failed to add user";
    }
}
