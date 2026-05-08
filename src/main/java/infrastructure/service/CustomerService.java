package infrastructure.service;

import core.dto.CustomerDto;

import java.util.List;

public interface CustomerService {
    CustomerDto create(CustomerDto customerDto);
    CustomerDto update(CustomerDto customerDto);
    CustomerDto findById(String id);
    List<CustomerDto> loadAll();
    CustomerDto findByPhoneNumber(String phoneNumber);
}
