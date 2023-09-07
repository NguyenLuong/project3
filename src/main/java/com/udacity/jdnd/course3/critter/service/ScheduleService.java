package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.entity.Customer;
import com.udacity.jdnd.course3.critter.entity.Employee;
import com.udacity.jdnd.course3.critter.entity.Pet;
import com.udacity.jdnd.course3.critter.entity.Schedule;
import com.udacity.jdnd.course3.critter.exception.DataNotFoundException;
import com.udacity.jdnd.course3.critter.repository.CustomerRepository;
import com.udacity.jdnd.course3.critter.repository.EmployeeRepository;
import com.udacity.jdnd.course3.critter.repository.PetRepository;
import com.udacity.jdnd.course3.critter.repository.ScheduleRepository;
import com.udacity.jdnd.course3.critter.schedule.ScheduleDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduleService {

    private static final String PET_LNK_SCH_NOT_FND_MSG="Pet link to schedule not found.";

    private static final String EMP_LNK_SCH_NOT_FND_MSG="Employee link to schedule not found.";

    private static final String CUS_LNK_SCH_NOT_FND_MSG="Customer link to schedule not found.";

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public ScheduleDTO save(ScheduleDTO scheduleDTO) {
        Schedule schedule = scheduleRepository.save(this.convertScheduleDTOToSchedule(scheduleDTO));
        return this.convertScheduleToScheduleDTO(schedule);
    }

    public List<ScheduleDTO> getAll() {
        Iterable<Schedule> schedules = scheduleRepository.findAll();
        List<ScheduleDTO> scheduleDTOS = new ArrayList<>();
        for (Schedule schedule : schedules) {
            scheduleDTOS.add(this.convertScheduleToScheduleDTO(schedule));
        }
        return scheduleDTOS;
    }

    public List<ScheduleDTO> getScheduleForPet(long petId) {
        Optional<Pet> pet = petRepository.findById(petId);
        if (pet.isPresent()) {
            List<Schedule> schedules = scheduleRepository.findByPet(pet.get());
            List<ScheduleDTO> scheduleDTOS = new ArrayList<>();
            for (Schedule s : schedules) {
                scheduleDTOS.add(this.convertScheduleToScheduleDTO(s));
            }

            return scheduleDTOS;
        } else {
            throw new DataNotFoundException(PET_LNK_SCH_NOT_FND_MSG);
        }
    }

    public List<ScheduleDTO> getScheduleForEmployee(long employeeId) {
        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isPresent()) {
            List<Schedule> schedules = scheduleRepository.findByEmployee(employee.get());
            List<ScheduleDTO> scheduleDTOS = new ArrayList<>();
            for (Schedule s : schedules) {
                scheduleDTOS.add(this.convertScheduleToScheduleDTO(s));
            }

            return scheduleDTOS;
        } else {
            throw new DataNotFoundException(EMP_LNK_SCH_NOT_FND_MSG);
        }
    }

    public List<ScheduleDTO> getScheduleForCustomer(long customerId) {
        Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isPresent()) {
            List<Pet> pets = petRepository.findByCustomer(customer.get());
            List<Schedule> customerSchedule = new ArrayList<>();
            for (Pet pet : pets) {
                List<Schedule> schedules = scheduleRepository.findByPet(pet);
                customerSchedule.addAll(schedules);
            }

            List<ScheduleDTO> customerScheduleDTOs = new ArrayList<>();
            for (Schedule schedule : customerSchedule) {
                customerScheduleDTOs.add(this.convertScheduleToScheduleDTO(schedule));
            }

            return customerScheduleDTOs;
        } else {
            throw new DataNotFoundException(CUS_LNK_SCH_NOT_FND_MSG);
        }
    }

    private ScheduleDTO convertScheduleToScheduleDTO(Schedule schedule) {
        ScheduleDTO scheduleDTO = new ScheduleDTO();
        BeanUtils.copyProperties(schedule, scheduleDTO);
        List<Long> employeeIds = new ArrayList<>();
        List<Long> petIds = new ArrayList<>();

        employeeIds.add(schedule.getEmployee().getId());
        petIds.add(schedule.getPet().getId());
        scheduleDTO.setEmployeeIds(employeeIds);
        scheduleDTO.setPetIds(petIds);

        return scheduleDTO;
    }

    private Schedule convertScheduleDTOToSchedule(ScheduleDTO scheduleDTO) {
        Long employeeId = scheduleDTO.getEmployeeIds().get(0);
        Long petId = scheduleDTO.getPetIds().get(0);
        Schedule schedule = new Schedule();

        Optional<Employee> employee = employeeRepository.findById(employeeId);
        if (employee.isPresent()) {
            Optional<Pet> pet = petRepository.findById(petId);
            if (pet.isPresent()) {
                BeanUtils.copyProperties(scheduleDTO, schedule);
                schedule.setEmployee(employee.get());
                schedule.setPet(pet.get());

                return schedule;
            } else {
                throw new DataNotFoundException(PET_LNK_SCH_NOT_FND_MSG);
            }
        } else {
            throw new DataNotFoundException(EMP_LNK_SCH_NOT_FND_MSG);
        }
    }
}
