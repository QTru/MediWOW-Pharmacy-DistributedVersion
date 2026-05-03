package core;

import core.utils.idgenerator.SequenceInitializer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class CreateDBSchema {
    public static void main(String[] args) {
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("mariadb-pu");
             EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            SequenceInitializer.createSequences(em);
            em.getTransaction().commit();
        }
    }
}
