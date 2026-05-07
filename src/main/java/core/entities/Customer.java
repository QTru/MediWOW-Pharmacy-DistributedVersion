package core.entities;

import core.utils.idgenerator.implementation.GeneratedId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = "id")

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedId(prefix = "CUST", numberLength = 4, sequenceName = "seq_customer_id")
    @Column(name = "customer_id", length = 20, nullable = false)
    private String id;
    private String name;
    @Column(name = "phone_number")
    private String phoneNumber;
    private String address;
    @Column(name = "creation_date")
    private LocalDateTime creationDate;
}
