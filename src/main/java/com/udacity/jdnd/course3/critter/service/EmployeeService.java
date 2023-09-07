package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.entity.Employee;
import com.udacity.jdnd.course3.critter.entity.Schedule;
import com.udacity.jdnd.course3.critter.exception.DataNotFoundException;
import com.udacity.jdnd.course3.critter.repository.EmployeeRepository;
import com.udacity.jdnd.course3.critter.repository.ScheduleRepository;
import com.udacity.jdnd.course3.critter.user.EmployeeDTO;
import com.udacity.jdnd.course3.critter.user.EmployeeRequestDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.*;

@Service
public class EmployeeService {

    private static final String EMP_NOT_FOUND_MSG="Employee not found.";

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    public EmployeeDTO save(EmployeeDTO employeeDTO) {
        Employee employee = employeeRepository.save(this.convertEmployeeDTOToEmployee(employeeDTO));
        return this.convertEmployeeToEmployeeDTO(employee);
    }

    public EmployeeDTO findById(long employeeId) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isPresent()) {
            return this.convertEmployeeToEmployeeDTO(employee.get());
        } else {
            throw new DataNotFoundException(EMP_NOT_FOUND_MSG);
        }
    }

    public void setAvailability(Set<DayOfWeek> daysAvailable, long employeeId) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            employee.setDaysAvailable(daysAvailable);

            employeeRepository.save(employee);
        } else {
            throw new DataNotFoundException(EMP_NOT_FOUND_MSG);
        }
    }

    public List<EmployeeDTO> findEmployeeForService(EmployeeRequestDTO employeeRequestDTO) {
        List<Schedule> schedules = scheduleRepository.findByDate(employeeRequestDTO.getDate());
        Iterable<Employee> allEmployees = employeeRepository.findAll();
        String dayOfWeek = employeeRequestDTO.getDate().getDayOfWeek().name();
        List<EmployeeDTO> employeeDTOS = new ArrayList<>();

        if (schedules.size() > 0) {
            boolean availableFlg = true;
            for (Employee e : allEmployees) {
                for (Schedule s : schedules) {
                    if (s.getEmployee().getId() == e.getId()) {
                        availableFlg = false;
                        break;
                    }
                }

                if (availableFlg == true) {
                    if (e.getSkills().containsAll(employeeRequestDTO.getSkills())) {
                        if (e.getDaysAvailable().contains(dayOfWeek)) {
                            employeeDTOS.add(this.convertEmployeeToEmployeeDTO(e));
                        }
                    }
                }
            }
        } else {
            for (Employee e : allEmployees) {
                if (e.getSkills().containsAll(employeeRequestDTO.getSkills())) {
                    if (e.getDaysAvailable().contains(dayOfWeek)) {
                        employeeDTOS.add(this.convertEmployeeToEmployeeDTO(e));
                    }
                }
            }
        }

        return employeeDTOS;
    }

    private Employee convertEmployeeDTOToEmployee(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        return employee;
    }

    private EmployeeDTO convertEmployeeToEmployeeDTO(Employee employee) {
        EmployeeDTO employeeDTO = new EmployeeDTO();
        BeanUtils.copyProperties(employee, employeeDTO);
        return employeeDTO;
    }
}
