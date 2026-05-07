package infrastructure.persistence.implementation;

import infrastructure.db.JPAUtil;
import infrastructure.persistence.GenericRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractGenericRepositoryImplementation<T, ID> implements GenericRepository<T, ID> {

    protected Class<T> entityClass;

    public AbstractGenericRepositoryImplementation(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected <R> R doInTransaction(Function<EntityManager, R> function){
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();
            R result = function.apply(em);
            tx.commit();
            return result;
        } catch (Exception ex) {
            if (tx != null && tx.isActive())
                tx.rollback();
            throw new RuntimeException(ex);
        } finally {
            em.close();
        }
    }

    @Override
    public T create(T t) {
        return doInTransaction(em -> {
            em.persist(t);
            return t;
        });
    }

    @Override
    public T update(T t) {
        return doInTransaction(em -> {
            return em.merge(t);
        });
    }

    @Override
    public T findById(ID id) {
        return doInTransaction(em -> {
            return em.find(entityClass, id);
        });
    }

    @Override
    public List<T> loadAll() {
        String query = "FROM " + entityClass.getSimpleName();
        return doInTransaction(em -> {
            return em.createQuery(query, entityClass)
                    .getResultList();
        });
    }
}
