package com.jetson.rest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetson.rest.models.ConfigModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
public class ConfigControllerTest {
    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
    }
    // Config Model
    ConfigModel configModel = new ConfigModel();

    @Test
    void addConfigWithProperConfigModel() throws Exception {
        configModel.setHardware("Jetson-demo");
        configModel.setModel("yolov4-tiny");
        configModel.setObjects(new String[]{"person", "bicycle", "car"});
        configModel.setCount(new String[]{"person", "bicycle"});
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/detection/config")
                    .content(asJsonString(configModel))
                    .contentType("application/json"))
                .andExpect(status().isAccepted())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void addConfigWithProperConfigModelForNewHardware() throws Exception {
        configModel.setHardware("Jetson-new-test");
        configModel.setModel("yolov4-tiny");
        configModel.setObjects(new String[]{"person", "bicycle", "car"});
        configModel.setCount(new String[]{"person", "bicycle"});
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/detection/config")
                        .content(asJsonString(configModel))
                        .contentType("application/json"))
                .andExpect(status().isAccepted())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void addConfigWithModelMissingHardware() throws Exception {
        configModel.setModel("yolov4-tiny");
        configModel.setObjects(new String[]{"person", "bicycle", "car"});
        configModel.setCount(new String[]{"person", "bicycle"});
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/detection/config")
                        .content(asJsonString(configModel))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void addConfigWithModelMissingModel() throws Exception {
        configModel.setHardware("Jetson-demo");
        configModel.setObjects(new String[]{"person", "bicycle", "car"});
        configModel.setCount(new String[]{"person", "bicycle"});
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/detection/config")
                        .content(asJsonString(configModel))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void addConfigWithModelMissingObjects() throws Exception {
        configModel.setHardware("Jetson-demo");
        configModel.setModel("yolov4-tiny");
        configModel.setCount(new String[]{"person", "bicycle"});
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/detection/config")
                        .content(asJsonString(configModel))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void addConfigWithModelMissingCount() throws Exception {
        configModel.setHardware("Jetson-demo");
        configModel.setModel("yolov4-tiny");
        configModel.setObjects(new String[]{"person", "bicycle", "car"});
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/detection/config")
                        .content(asJsonString(configModel))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void compareConfigWithChangedOne() throws Exception {
        configModel.setHardware("Jetson-demo");
        configModel.setModel("yolov4-tiny");
        configModel.setObjects(new String[]{});
        configModel.setCount(new String[]{"person", "bicycle"});
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/detection/config/compare")
                        .content(asJsonString(configModel))
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        Assert.notNull(result, "The response must have hardware config");
    }

    @Test
    void compareConfigWithUnchangedOne() throws Exception {
        configModel.setHardware("Jetson-demo");
        configModel.setModel("yolov4-tiny");
        configModel.setObjects(new String[]{"person", "bicycle", "car"});
        configModel.setCount(new String[]{"person", "bicycle"});
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/detection/config/compare")
                        .content(asJsonString(configModel))
                        .contentType("application/json"))
                .andExpect(status().isAccepted())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals("false", content);
    }

    @Test
    void compareConfigWithConfigForNonExistingDevice() throws Exception {
        configModel.setHardware("Jetson-bogus");
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/detection/config/compare")
                        .content(asJsonString(configModel))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals("Bad Request", content);
    }

    @Test
    void compareConfigWithoutAnyConfig() throws Exception {
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/detection/config/compare")
                        .contentType("application/json"))
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
