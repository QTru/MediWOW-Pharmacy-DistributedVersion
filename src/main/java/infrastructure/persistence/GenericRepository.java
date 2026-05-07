package infrastructure.persistence;

import java.util.List;

public interface GenericRepository<T, ID> {
    T create(T t);
    T update(T t);
    T findById(ID id);
    List<T> loadAll();
}
