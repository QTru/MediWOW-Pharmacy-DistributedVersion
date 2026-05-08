package infrastructure.service.implementation;

import core.dto.CustomerDto;
import core.entities.Customer;
import infrastructure.mapper.Mapper;
import infrastructure.persistence.CustomerRepository;
import infrastructure.persistence.implementation.CustomerRepositoryImplementation;
import infrastructure.service.CustomerService;

import java.time.LocalDateTime;
import java.util.List;

public class CustomerServiceImplementation implements CustomerService {
    CustomerRepository customerRepository;

    public CustomerServiceImplementation() {
         this.customerRepository = new CustomerRepositoryImplementation();
    }

    @Override
    public CustomerDto create(CustomerDto customerDto) {
        generalCheck(customerDto);
        
        Customer customer = Mapper.map(customerDto, Customer.class);
        customer.setCreationDate(LocalDateTime.now());
        customer = customerRepository.create(customer);
        return Mapper.map(customer, CustomerDto.class);
    }

    @Override
    public CustomerDto update(CustomerDto customerDto) {
        if (customerDto.getId() == null || customerDto.getId().isBlank())
            throw new IllegalArgumentException("Customer id cannot be null or blank");
        generalCheck(customerDto);

        Customer customer = Mapper.map(customerDto, Customer.class);
        customer = customerRepository.update(customer);
        return Mapper.map(customer, CustomerDto.class);
    }

    @Override
    public CustomerDto findById(String id) {
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Id cannot be null or blank");

        Customer customer = customerRepository.findById(id);
        if (customer == null)
            throw new IllegalArgumentException("Customer with id " + id + " not found");

        return Mapper.map(customer, CustomerDto.class);
    }

    @Override
    public List<CustomerDto> loadAll() {
        return customerRepository.loadAll()
                .stream()
                .map(customer -> Mapper.map(customer, CustomerDto.class))
                .toList();
    }

    @Override
    public CustomerDto findByPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank())
            throw new IllegalArgumentException("Phone number cannot be null or blank");

        Customer customer = customerRepository.findByPhoneNumber(phoneNumber);
        if (customer == null)
            throw new IllegalArgumentException("Customer with phone number " + phoneNumber + " not found");

        return Mapper.map(customer, CustomerDto.class);
    }
    
    private void generalCheck(CustomerDto customerDto) {
        if (customerDto.getPhoneNumber() == null || customerDto.getPhoneNumber().isBlank())
            throw new IllegalArgumentException("Customer phone number cannot be null or blank");
        if (!customerDto.getPhoneNumber().chars().allMatch(Character::isDigit))
            throw new IllegalArgumentException("Customer phone number must be a string of digits");
    }

    public static void main(String[] args) {
        CustomerService customerService = new CustomerServiceImplementation();
        CustomerDto customerDto = CustomerDto
                .builder()
                .name("John Doe")
                .phoneNumber("1234567891")
                .build();
        customerDto = customerService.create(customerDto);
        System.out.println(customerDto);
        customerDto.setName("John Doe Updated");
        customerDto = customerService.update(customerDto);
        System.out.println(customerDto);
        System.out.println(customerService.findById(customerDto.getId()));
        customerService.loadAll().forEach(System.out::println);
        System.out.println(customerService.findByPhoneNumber(customerDto.getPhoneNumber()));
    }
}
