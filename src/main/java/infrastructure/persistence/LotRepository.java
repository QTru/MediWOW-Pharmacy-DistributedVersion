package infrastructure.persistence;

import core.entities.Lot;

import java.util.List;

public interface LotRepository extends GenericRepository<Lot, String> {
    List<Lot> findAvailableLotsByProductId(String productId);
}