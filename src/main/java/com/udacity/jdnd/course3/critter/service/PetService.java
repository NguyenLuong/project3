package com.udacity.jdnd.course3.critter.service;

import com.udacity.jdnd.course3.critter.entity.Customer;
import com.udacity.jdnd.course3.critter.entity.Pet;
import com.udacity.jdnd.course3.critter.exception.DataNotFoundException;
import com.udacity.jdnd.course3.critter.pet.PetDTO;
import com.udacity.jdnd.course3.critter.repository.CustomerRepository;
import com.udacity.jdnd.course3.critter.repository.PetRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PetService {

    private static final String PET_NOT_FND_EXP="Pet data not found";

    private static final String CUS_LNK_PET_NOT_FND_MSG="Customer who link to pet not found!";

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public PetDTO save(PetDTO petDTO) {
        Optional<Customer> customer = customerRepository.findById(petDTO.getOwnerId());
        if (customer.isPresent()) {
            Pet pet = petRepository.save(this.convertPetDTOToPet(petDTO, customer.get()));
            return this.convertPetToPetDTO(pet);
        } else {
            throw new DataNotFoundException(CUS_LNK_PET_NOT_FND_MSG);
        }
    }

    public PetDTO getPetById(long petId) {
        Optional<Pet> petOptional = petRepository.findById(petId);
        if (petOptional.isPresent()) {
            return this.convertPetToPetDTO(petOptional.get());
        } else {
            throw new DataNotFoundException(PET_NOT_FND_EXP);
        }
    }

    public List<PetDTO> getAllPets() {
        Iterable<Pet> pets = petRepository.findAll();
        List<PetDTO> petDTOs = new ArrayList<>();
        pets.forEach(p -> {
            petDTOs.add(this.convertPetToPetDTO(p));
        });
        return petDTOs;
    }

    public List<PetDTO> getPetsByOwnerId(long ownerId) {
        Optional<Customer> customer = customerRepository.findById(ownerId);
        if (customer.isPresent()) {
            List<Pet> pets = new ArrayList<>();
            pets = petRepository.findByCustomer(customer.get());
            List<PetDTO> petDTOs = new ArrayList<>();
            if (pets.size() == 0) {
                throw new DataNotFoundException(PET_NOT_FND_EXP);
            } else {
                pets.forEach(p -> {
                    petDTOs.add(this.convertPetToPetDTO(p));
                });
                return petDTOs;
            }
        } else {
            throw new DataNotFoundException(CUS_LNK_PET_NOT_FND_MSG);
        }

    }

    private Pet convertPetDTOToPet(PetDTO petDTO, Customer customer) {
        Pet pet = new Pet();
        BeanUtils.copyProperties(petDTO, pet);
        pet.setCustomer(customer);
        return pet;
    }

    private PetDTO convertPetToPetDTO(Pet pet) {
        PetDTO petDTO = new PetDTO();
        BeanUtils.copyProperties(pet, petDTO);
        petDTO.setOwnerId(pet.getCustomer().getId());
        return petDTO;
    }
}
