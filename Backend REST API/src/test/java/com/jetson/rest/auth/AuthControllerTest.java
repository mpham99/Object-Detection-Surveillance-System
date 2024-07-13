package com.jetson.rest.auth;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetson.rest.models.AuthModel;
import com.jetson.rest.security.JwtTokenUtil;
import com.jetson.rest.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class AuthControllerTest {

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
    }

    @Test
    void loginWithCorrectCredential() throws Exception {
        AuthModel authModel = new AuthModel();
        authModel.setUsername("norole");
        authModel.setPassword("norole");
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/auth/login")
                        .content(asJsonString(authModel))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void loginWithIncorrectUsernameCorrectPassword() throws Exception {
        AuthModel authModel = new AuthModel();
        authModel.setUsername("bogus");
        authModel.setPassword("norole");
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/auth/login")
                        .content(asJsonString(authModel))
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void loginWithCorrectUsernameIncorrectPassword() throws Exception {
        AuthModel authModel = new AuthModel();
        authModel.setUsername("norole");
        authModel.setPassword("bogus");
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/auth/login")
                        .content(asJsonString(authModel))
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void refreshWithCorrectRefreshToken() throws Exception {
        AuthModel authModel = new AuthModel();
        authModel.setUsername("norole");
        authModel.setPassword("norole");
        // Test
        UserDetails user = userService.findByUsername(authModel.getUsername());
        MvcResult result = this.mockMvc.perform(
                post("/auth/refresh")
                        .header("authorization", "Bearer " + jwtTokenUtil.generateRefreshToken(user)))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void refreshWithEmptyRefreshToken() throws Exception {
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/auth/refresh"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void refreshWithIncorrectRefreshToken() throws Exception {
        AuthModel authModel = new AuthModel();
        authModel.setUsername("norole");
        authModel.setPassword("norole");
        // Test
        UserDetails user = userService.findByUsername(authModel.getUsername());
        MvcResult result = this.mockMvc.perform(
                post("/auth/refresh")
                    .header("authorization", "Bearer " + jwtTokenUtil.generateToken(user)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
