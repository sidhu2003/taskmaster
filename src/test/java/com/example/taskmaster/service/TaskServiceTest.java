package com.example.taskmaster.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.example.taskmaster.model.Task;
import com.example.taskmaster.repository.TaskRepository;
import com.example.taskmaster.exception.TaskNotFoundException;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
    }

    @Test
    void createTask_ShouldReturnSavedTask() {
        Task savedTask = taskService.createTask(task);
        
        assertNotNull(savedTask.getId());
        assertEquals(task.getTitle(), savedTask.getTitle());
    }

    @Test
    void getTaskById_WhenTaskExists_ShouldReturnTask() {
        Task savedTask = taskRepository.save(task);
        
        Task foundTask = taskService.getTaskById(savedTask.getId());
        
        assertNotNull(foundTask);
        assertEquals(savedTask.getId(), foundTask.getId());
    }

    @Test
    void getTaskById_WhenTaskDoesNotExist_ShouldThrowException() {
        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(999L));
    }

    @Test
    void updateTask_ShouldUpdateTaskFields() {
        Task savedTask = taskRepository.save(task);
        
        Task updateDetails = new Task();
        updateDetails.setTitle("Updated Title");
        updateDetails.setDescription("Updated Description");
        updateDetails.setCompleted(true);
        
        Task updatedTask = taskService.updateTask(savedTask.getId(), updateDetails);
        
        assertEquals("Updated Title", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertTrue(updatedTask.isCompleted());
    }

    @Test
    void deleteTask_WhenTaskExists_ShouldDeleteTask() {
        Task savedTask = taskRepository.save(task);
        
        taskService.deleteTask(savedTask.getId());
        
        assertFalse(taskRepository.existsById(savedTask.getId()));
    }
}