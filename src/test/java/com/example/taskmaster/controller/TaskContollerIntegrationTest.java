package com.example.taskmaster.controller;

import com.example.taskmaster.model.Task;
import com.example.taskmaster.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Task testTask;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        
        testTask = new Task();
        testTask.setTitle("Integration Test Task");
        testTask.setDescription("Test Description");
        testTask.setCompleted(false);
    }

    @Test
    void createTask_Success() throws Exception {
        String taskJson = objectMapper.writeValueAsString(testTask);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is(testTask.getTitle())))
                .andExpect(jsonPath("$.description", is(testTask.getDescription())))
                .andExpect(jsonPath("$.completed", is(false)))
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()));

        List<Task> tasks = taskRepository.findAll();
        assertEquals(1, tasks.size());
        assertEquals(testTask.getTitle(), tasks.get(0).getTitle());
    }

    @Test
    void createTask_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        testTask.setTitle(""); // Title is required

        String taskJson = objectMapper.writeValueAsString(testTask);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", notNullValue())); // Should contain validation error message
    }

    @Test
    void getAllTasks_Success() throws Exception {
        Task savedTask = taskRepository.save(testTask);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(savedTask.getId().intValue())))
                .andExpect(jsonPath("$[0].title", is(savedTask.getTitle())))
                .andExpect(jsonPath("$[0].description", is(savedTask.getDescription())));
    }

    @Test
    void getTaskById_Success() throws Exception {
        Task savedTask = taskRepository.save(testTask);

        mockMvc.perform(get("/api/tasks/{id}", savedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedTask.getId().intValue())))
                .andExpect(jsonPath("$.title", is(savedTask.getTitle())))
                .andExpect(jsonPath("$.description", is(savedTask.getDescription())));
    }

    @Test
    void getTaskById_NotFound() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("not found")));
    }

    @Test
    void updateTask_Success() throws Exception {
        Task savedTask = taskRepository.save(testTask);

        Task updateRequest = new Task();
        updateRequest.setTitle("Updated Title");
        updateRequest.setDescription("Updated Description");
        updateRequest.setCompleted(true);

        String updateJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/tasks/{id}", savedTask.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedTask.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.description", is("Updated Description")))
                .andExpect(jsonPath("$.completed", is(true)));

        Task updatedTask = taskRepository.findById(savedTask.getId()).orElseThrow();
        assertEquals("Updated Title", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertTrue(updatedTask.isCompleted());
    }

    @Test
    void updateTask_NotFound() throws Exception {
        String updateJson = objectMapper.writeValueAsString(testTask);

        mockMvc.perform(put("/api/tasks/{id}", 999)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("not found")));
    }

    @Test
    void deleteTask_Success() throws Exception {
        Task savedTask = taskRepository.save(testTask);

        mockMvc.perform(delete("/api/tasks/{id}", savedTask.getId()))
                .andExpect(status().isNoContent());

        assertFalse(taskRepository.existsById(savedTask.getId()));
    }

    @Test
    void deleteTask_NotFound() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("not found")));
    }

    @Test
    void endToEndTaskFlow() throws Exception {
        // 1. Create a new task
        String createJson = objectMapper.writeValueAsString(testTask);
        String createResponse = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        Task createdTask = objectMapper.readValue(createResponse, Task.class);
        Long taskId = createdTask.getId();

        // 2. Get the created task
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is(testTask.getTitle())));

        // 3. Update the task
        Task updateRequest = new Task();
        updateRequest.setTitle("Updated in Flow");
        updateRequest.setCompleted(true);
        String updateJson = objectMapper.writeValueAsString(updateRequest);

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated in Flow")))
                .andExpect(jsonPath("$.completed", is(true)));

        // 4. Verify in the list
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(taskId.intValue())))
                .andExpect(jsonPath("$[0].title", is("Updated in Flow")));

        // 5. Delete the task
        mockMvc.perform(delete("/api/tasks/{id}", taskId))
                .andExpect(status().isNoContent());

        // 6. Verify deletion
        mockMvc.perform(get("/api/tasks/{id}", taskId))
                .andExpect(status().isNotFound());
    }
}