package com.jetson.rest.hardware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jetson.rest.models.HardwareModel;
import com.jetson.rest.services.HardwareService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
public class HardwareControllerTest {
    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HardwareService hardwareService;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .build();
    }
    // Hardware Model
    HardwareModel hardwareModel = new HardwareModel();

    @Test
    void getListOfAllHardware() throws Exception {
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/hardware")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void getHardwareUsingAvailableId() throws Exception {
        String id = "5fc7a97cd4d9705f098fc4e8";
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/hardware/" + id)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        Assert.notNull(result, "The response must not be empty");
    }

    @Test
    void getHardwareUsingNonAvailableId() throws Exception {
        String id = "0";
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/hardware/" + id)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals(0, content.length());
    }

    @Test
    void getStreamKeyWithDeviceName() throws Exception {
        String name = "Jetson 1";
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/hardware/key")
                        .param("name", name)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals(hardwareService.getHardwareByName(name).getStreamKey(), content);
    }

    @Test
    void getStreamKeyWithoutDeviceName() throws Exception {
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/hardware/key")
                        .param("name", "")
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals(0, content.length());
    }

    @Test
    void getStreamKeyWithNonExistingDeviceName() throws Exception {
        String name = "bogus";
        // Test
        MvcResult result = this.mockMvc.perform(
                get("/api/hardware/key")
                        .param("name", name)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals(0, content.length());
    }

    @Test
    void addNewHardwareWithNewHardwareModel() throws Exception {
        hardwareModel.setName("Jetson-demo");
        hardwareModel.setSerial("test_serial");
        hardwareModel.setStreamKey("test_stream_key");
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/hardware")
                        .content(asJsonString(hardwareModel))
                        .contentType("application/json"))
                .andExpect(status().isAccepted())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        Assert.notNull(content, "The response must not be empty");
    }

    @Test
    void addNewHardwareWithExistingHardwareModel() throws Exception {
        hardwareModel.setName("Jetson 1");
        hardwareModel.setSerial("test_serial");
        hardwareModel.setStreamKey("test_stream_key");
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/hardware")
                        .content(asJsonString(hardwareModel))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals("This device is already registered", content);
    }

    @Test
    void addNewHardwareWithHardwareModelWithoutHardwareName() throws Exception {
        hardwareModel.setName("");
        hardwareModel.setSerial("test_serial");
        hardwareModel.setStreamKey("test_stream_key");
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/hardware")
                        .content(asJsonString(hardwareModel))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals("Can't register empty model", content);
    }

    @Test
    void addNewHardwareWithoutHardwareModel() throws Exception {
        // Test
        MvcResult result = this.mockMvc.perform(
                post("/api/hardware")
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
    }

    @Test
    void updateStreamKeyWithExistingHardwareNameAndStreamKey() throws Exception {
        // Test
        String name = "Jetson-demo";
        String streamKey = "bogus_stream_key";
        MvcResult result = this.mockMvc.perform(
                put("/api/hardware/key/" + name)
                        .content(streamKey)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals("Device's stream key was successfully updated", content);
    }

    @Test
    void updateStreamKeyWithNonExistingHardwareNameAndStreamKey() throws Exception {
        // Test
        String name = "Jetson-bogus";
        String streamKey = "bogus_stream_key";
        MvcResult result = this.mockMvc.perform(
                put("/api/hardware/key/" + name)
                        .content(streamKey)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        System.out.println(content);
        assertEquals("Can't update stream key of a non-existent device", content);
    }

    @Test
    void updateStreamKeyWithExistingHardwareNameAndNoStreamKey() throws Exception {
        // Test
        String name = "Jetson-demo";
        String streamKey = null;
        MvcResult result = this.mockMvc.perform(
                put("/api/hardware/key/" + name)
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
