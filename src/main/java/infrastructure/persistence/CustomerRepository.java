package infrastructure.persistence;

import core.entities.Customer;

public interface CustomerRepository extends GenericRepository<Customer, String> {
    Customer findByPhoneNumber(String phoneNumber);
}
