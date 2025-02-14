package com.rh4.services;

import com.rh4.entities.TaskAssignment;
import com.rh4.repositories.TaskAssignmentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskAssignmentService {
    @Autowired
    private TaskAssignmentRepo taskAssignmentRepo;

    public List<TaskAssignment> getTasksByIntern(String intern) {
        return taskAssignmentRepo.findByIntern(intern);
    }

    public List<TaskAssignment> getTasksAssignedBy(String assignedById) {
        return taskAssignmentRepo.findByAssignedById(assignedById);
    }

    public Optional<TaskAssignment> getTaskById(Long taskId) {
        return taskAssignmentRepo.findById(taskId);
    }

    public TaskAssignment saveTask(TaskAssignment task) {
        return taskAssignmentRepo.save(task);
    }

    public void deleteTask(Long taskId) {
        taskAssignmentRepo.deleteById(taskId);
    }

    public List<TaskAssignment> getAllTasks() {
        return taskAssignmentRepo.findAll();
    }

    public boolean updateTaskStatus(Long taskId, String status) {
        Optional<TaskAssignment> taskOpt = taskAssignmentRepo.findById(taskId);
        if (taskOpt.isPresent()) {
            TaskAssignment task = taskOpt.get();
            task.setStatus(status); // Only update status
            taskAssignmentRepo.save(task);
            return true;
        }
        return false;
    }
}