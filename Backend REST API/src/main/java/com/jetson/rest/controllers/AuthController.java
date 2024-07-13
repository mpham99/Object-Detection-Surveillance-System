package com.jetson.rest.controllers;

import com.jetson.rest.models.AuthModel;
import com.jetson.rest.response.AuthResponse;
import com.jetson.rest.security.JwtTokenUtil;
import com.jetson.rest.services.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired private UserService userService;
    @Autowired private JwtTokenUtil jwtTokenUtil;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("login")
    @ApiOperation(
            value = "Login with username & password",
            notes = "Provide username and password to receive access token for subsequent REST calls",
            response = AuthResponse.class)
    public ResponseEntity<Object> login(@RequestBody AuthModel authModel) {
        UserDetails user = userService.findByUsername(authModel.getUsername());

        if (user == null)
            return new ResponseEntity<>("User not found", HttpStatus.UNAUTHORIZED);
        if (!passwordEncoder.matches(authModel.getPassword(), user.getPassword().substring(8)))
            return new ResponseEntity<>("Authorization failed", HttpStatus.UNAUTHORIZED);

        AuthResponse auth = new AuthResponse();
        auth.setAccessToken(jwtTokenUtil.generateToken(user));
        auth.setRefreshToken(jwtTokenUtil.generateRefreshToken(user));
        return new ResponseEntity<>(auth, HttpStatus.OK);
    }

    @PostMapping("refresh")
    @ApiOperation(
            value = "Provides new access token",
            notes = "Provide refresh token to receive access token with new expiration",
            response = AuthResponse.class)
    public ResponseEntity<Object> refresh(@RequestHeader("authorization") String refreshToken) {
        if (refreshToken == null)
            return new ResponseEntity<>("no refresh token provided", HttpStatus.BAD_REQUEST);

        refreshToken = refreshToken.substring(7);
        if (!jwtTokenUtil.isRefreshToken(refreshToken))
            return new ResponseEntity<>("Not refresh token", HttpStatus.BAD_REQUEST);

        UserDetails user = userService.findByUsername(jwtTokenUtil.getUsernameFromToken(refreshToken));
        AuthResponse auth = new AuthResponse();
        auth.setAccessToken(jwtTokenUtil.generateToken(user));
        auth.setRefreshToken(jwtTokenUtil.generateRefreshToken(user));
        return new ResponseEntity<>(auth, HttpStatus.OK);
    }
}
