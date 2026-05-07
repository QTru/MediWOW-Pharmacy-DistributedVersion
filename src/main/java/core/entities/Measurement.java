package core.entities;

import core.utils.idgenerator.implementation.GeneratedId;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = "id")

@Entity
@Table(name = "measurements")
public class Measurement {
    @Id
    @GeneratedId(prefix = "MEA", numberLength = 4, sequenceName = "seq_measurement_id")
    @Column(name = "measurement_id", length = 20, nullable = false)
    private String id;
    @Column(nullable = false, unique = true)
    private String name;
}
