package com.jetson.rest.trainedmodels;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetson.rest.models.TrainedModel;
import org.apache.commons.io.FileUtils;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
public class TrainedModelsControllerTest {
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
    // Trained Model
    TrainedModel trainedModel = new TrainedModel();

    @Test
    void getListOfTrainedModels() throws Exception {
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/models")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        Assert.notNull(content, "The response must not be empty");
    }

    @Test
    void addTrainedModelWithNewTrainedModel() throws Exception {
        trainedModel.setName("trained_model_demo");
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/models")
                        .content(asJsonString(trainedModel))
                        .contentType("application/json"))
                .andExpect(status().isAccepted())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        Assert.notNull(content, "The response must not be empty");
    }

    @Test
    void addTrainedModelWithAlreadyExistedTrainedModel() throws Exception {
        trainedModel.setName("trained_model_demo");
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/models")
                        .content(asJsonString(trainedModel))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals("Trained Model with that name already exists", content);
    }

    @Test
    void addTrainedModelWithoutTrainedModel() throws Exception {
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/models")
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void getListsOfDetectionObjectsForExistingTrainedModel() throws Exception {
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/models/objects")
                        .param("model", "YOLOv4")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        Assert.notNull(content, "The response must not be empty");
    }

    @Test
    void getListsOfDetectionObjectsForNonExistingTrainedModel() throws Exception {
        String name = "trained_model_demo";
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/models/objects")
                        .param("model", name)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals("Names file for model \"" + name + "\" doesn't exist", content);
    }

    @Test
    void getListsOfDetectionObjectsForExistingTrainedModelWithoutName() throws Exception {
        String name = "bogus";
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/models/objects")
                        .param("model", name)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals("Model \"" + name + "\" doesn't exist", content);
    }

    @Test
    void getListsOfDetectionObjectsWithoutModelName() throws Exception {
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/models/objects")
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void getNamesFileForExistingTrainedModel() throws Exception {
        String name = "YOLOv4 Tiny";
        String filetype = "names";
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/models/files")
                        .param("model", name)
                        .param("file-type", filetype)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        content = content.replaceAll("\\r\\n", "\n");
        File myObj = new File("src/test/java/com/jetson/rest/trainedmodels/Model/coco.names");
        assertEquals(FileUtils.readFileToString(myObj, "utf-8"), content);
    }

    @Test
    void getConfigFileForExistingTrainedModel() throws Exception {
        String name = "YOLOv4 Tiny";
        String filetype = "config";
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/models/files")
                        .param("model", name)
                        .param("file-type", filetype)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        content = content.replaceAll("\\r\\n", "\n");
        File myObj = new File("src/test/java/com/jetson/rest/trainedmodels/Model/yolov4-tiny.cfg");
        assertEquals(FileUtils.readFileToString(myObj, "utf-8"), content);
    }

    @Test
    void getWeightFileForExistingTrainedModel() throws Exception {
        String name = "YOLOv4 Tiny";
        String filetype = "weights";
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/models/files")
                        .param("model", name)
                        .param("file-type", filetype)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        File myObj = new File("src/test/java/com/jetson/rest/trainedmodels/Model/yolov4-tiny.weights");
        assertEquals(FileUtils.readFileToByteArray(myObj), result.getResponse().getContentAsByteArray());
    }


    @Test
    void getAnyFileForNonExistingTrainingModel() throws Exception {
        String name = "bogus";
        String filetype = "name";
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/models/files")
                        .param("model", name)
                        .param("file-type", filetype)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals("Model \"" + name + "\" doesn't exist", content);
    }

    @Test
    void getNonAllowedFileTypeForExistingModel() throws Exception {
        String name = "YOLOv4 Tiny";
        String filetype = "bogus";
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/models/files")
                        .param("model", name)
                        .param("file-type", filetype)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals("File type \"" + filetype + "\" doesn't exist", content);
    }


    @Test
    void getNonExistingAllowedFileTypeForExistingModel() throws Exception {
        String name = "trained_model_demo";
        String filetype = "names";
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/models/files")
                        .param("model", name)
                        .param("file-type", filetype)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals("Model \"" + name + "\" doesn't contain a \"" + filetype + "\" file", content);
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
