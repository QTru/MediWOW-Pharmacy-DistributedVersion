package infrastructure.persistence;

import core.entities.Customer;

public interface CustomerRepository extends GenericRepository<Customer, String> {
    Object findByPhoneNumber(String phoneNumber);
}
