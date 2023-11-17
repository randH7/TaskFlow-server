package com.rand.TaskFlow.service.implementations;

import com.rand.TaskFlow.DTO.ListOfTaskDTO;
import com.rand.TaskFlow.DTO.TaskDTO;
import com.rand.TaskFlow.entity.*;
import com.rand.TaskFlow.entity.enums.PriorityStatus;
import com.rand.TaskFlow.entity.enums.TaskStatus;
import com.rand.TaskFlow.repository.*;
import com.rand.TaskFlow.service.interfaces.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    ProjectRepository projectRepo;

    @Autowired
    ProjectAssignmentRepository projectAssignmentRepo;

    @Autowired
    TaskRepository taskRepo;

    @Autowired
    TaskAssignmentRepository taskAssignmentRepo;

    @Autowired
    EmployRepository teamMemberRepo;
    @Autowired
    TaskAssignmentServiceImpl taskAssignmentService;

    @Override
    public void createTask(TaskDTO newTask) {

        Task task = new Task(newTask.getTaskName(), projectRepo.findByProjectId(newTask.getProjectId()), newTask.getStartDate(), newTask.getDueDate(), newTask.getDescription(), newTask.getTaskStatus(), newTask.getPriorityStatus());
        taskRepo.save(task);

        taskAssignmentService.assignTaskToEmployees(newTask.getEmployeesUsername(), task);
    }

    @Override
    public String editTask(Integer projectId, Integer taskId, HashMap<String, Object> updatesTask) throws ParseException {

        Optional<Task> taskFound = taskRepo.findByTaskIdAndProject(taskId, projectRepo.findByProjectId(projectId));

        if(taskFound.isPresent()) {
            Task existingTask = taskFound.get();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (HashMap.Entry<String, Object> entry : updatesTask.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldValue = entry.getValue();

                switch (fieldName) {
                    case "taskName":
                        existingTask.setTaskName((String) fieldValue);
                        break;
                    case "dueDate":
                        //existingTask.setDueDate(new Date(dateFormat.parse((String) fieldValue).getTime()));
                        break;
                    case "description":
                        existingTask.setDescription((String) fieldValue);
                        break;
                    case "taskStatus":
                        existingTask.setTaskStatus(TaskStatus.valueOf((String)fieldValue));
                        break;
                    case "priorityStatus":
                        existingTask.setPriorityStatus(PriorityStatus.valueOf((String)fieldValue));
                        break;
                    case "teamMember":
                        if (!isAssignToProject((String)fieldValue, projectId))
                            return "Not Authorize to Add New Task in This Project.";
                        taskAssignmentRepo.deleteByTask(taskRepo.findByTaskId(taskId));
                        TaskAssignment taskAssignment = new TaskAssignment(teamMemberRepo.findByUsername((String)fieldValue), taskRepo.findByTaskId(taskId));
                        taskAssignmentRepo.save(taskAssignment);
                        break;
                }
            }

            taskRepo.save(existingTask);
            return  "["+existingTask.getTaskId()+"] Task Updated Successfully.";

        }
        return "Task Not Found.";

    }

    @Override
    public List<ListOfTaskDTO> getTasks(String username) {
        return taskRepo.findByUsername(username);
    }

    @Override
    public String deleteTask(Integer projectId, Integer taskId) {

        Optional<Task> taskFound = taskRepo.findByTaskIdAndProject(taskId, projectRepo.findByProjectId(projectId));

        if(taskFound.isPresent()) {
            taskRepo.deleteById(taskId);
            return  "["+taskFound.get().getTaskId()+"] Task Deleted Successfully.";
        }

        return "Project Not Found.";

    }

    @Override
    public boolean isAssignToProject(String teamMember, Integer projectId) {

        if(projectAssignmentRepo.findByEmployAndProject(teamMemberRepo.findByUsername(teamMember), projectRepo.findByProjectId(projectId)).isPresent())
            return true;
        return false;

    }

}
