package infrastructure.persistence.implementation;

import core.entities.Customer;
import infrastructure.persistence.CustomerRepository;

import java.time.LocalDateTime;

public class CustomerRepositoryImplementation extends AbstractGenericRepositoryImplementation<Customer, String> implements CustomerRepository {
    public CustomerRepositoryImplementation() {
        super(Customer.class);
    }

    @Override
    public Object findByPhoneNumber(String phoneNumber) {
        String query = "FROM Customer c WHERE c.phoneNumber = :phoneNumber";

        return doInTransaction(em -> {
            try {
                return em.createQuery(query, Customer.class)
                        .setParameter("phoneNumber", phoneNumber)
                        .getSingleResult();
            } catch (Exception e) {
                return null;
            }
        });
    }

    public static void main(String[] args) {
        CustomerRepository customerRepository = new CustomerRepositoryImplementation();
        Customer customer = Customer
                .builder()
                .name("John Doe")
                .phoneNumber("1234567891")
                .creationDate(LocalDateTime.now())
                .build();
//        customerRepository.create(customer);
        System.out.println(customerRepository.findByPhoneNumber("1234567891"));
    }
}
