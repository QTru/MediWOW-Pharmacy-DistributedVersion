package core.utils.idgenerator;

import jakarta.persistence.EntityManager;

import java.util.List;

public class SequenceInitializer {
    public static void createSequences(EntityManager entityManager) {
        List.of("customer_id", "invoice_id", "invoice_line_id",
                        "lot_id", "lot_allocation_id", "measurement_name_id",
                        "product_id", "promotion_id", "promotion_action_id",
                        "promotion_condition_id", "shift_id", "staff_id",
                        "unit_of_measure_id")
                .forEach(name -> entityManager.createNativeQuery(
                        "CREATE SEQUENCE IF NOT EXISTS seq_" + name + " START WITH 1 INCREMENT BY 1"
                ).executeUpdate());
    }
}
