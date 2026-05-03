package core.entities;

import core.utils.idgenerator.implementation.GeneratedId;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@EqualsAndHashCode(of = "id")

@Entity
@Table(name = "measurement_names")
public class MeasurementName {
    @Id
    @GeneratedId(prefix = "MEA", numberLength = 4, sequenceName = "seq_measurement_name_id")
    @Column(name = "measurement_name_id")
    private String id;
    private String name;
}
