package com.rand.TaskFlow.repository;

import com.rand.TaskFlow.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Integer> {
}