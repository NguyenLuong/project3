package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.entity.Customer;
import com.udacity.jdnd.course3.critter.entity.Pet;
import com.udacity.jdnd.course3.critter.exception.DataNotFoundException;
import com.udacity.jdnd.course3.critter.repository.CustomerRepository;
import com.udacity.jdnd.course3.critter.repository.PetRepository;
import com.udacity.jdnd.course3.critter.user.CustomerDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private static final String CUS_NOT_FND_MSG="No customer found!";

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PetRepository petRepository;

    public CustomerDTO save(CustomerDTO customerDTO) {
        Customer customer = customerRepository.save(this.convertCustomerDTOToCustomer(customerDTO));
        return this.convertCustomerToCustomerDTO(customer);
    }

    public List<CustomerDTO> getAllCustomers() {
        Iterable<Customer> customers = customerRepository.findAll();
        List<CustomerDTO> customerDTOs = new ArrayList<>();
        customers.forEach(c -> {
            customerDTOs.add(this.convertCustomerToCustomerDTO(c));
        });
        return customerDTOs;
    }

    public CustomerDTO getOwnerByPet(long petId) {
        Optional<Pet> pet = petRepository.findById(petId);
        if (pet.isPresent()) {
            Customer customer = customerRepository.findByPets(pet.get());
            return this.convertCustomerToCustomerDTO(customer);
        } else {
            throw new DataNotFoundException(CUS_NOT_FND_MSG);
        }
    }

    private CustomerDTO convertCustomerToCustomerDTO(Customer customer) {
        CustomerDTO customerDTO = new CustomerDTO();
        BeanUtils.copyProperties(customer, customerDTO);
        List<Long> petIds = new ArrayList<>();
        if (customer.getPets() != null) {
            customer.getPets().forEach(p -> {
                petIds.add(p.getId());
            });
            customerDTO.setPetIds(petIds);
        }
        return customerDTO;
    }

    private Customer convertCustomerDTOToCustomer(CustomerDTO customerDTO) {
        Customer customer = new Customer();
        BeanUtils.copyProperties(customerDTO, customer);
        return customer;
    }
}
