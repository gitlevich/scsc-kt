package demo.scsc.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;

import java.util.HashMap;
import java.util.Map;

public class JpaPersistenceUnit {

    private static final Map<String, JpaPersistenceUnit> persistenceUnits = new HashMap<>();
    private final EntityManagerFactory emf;
    private final ThreadLocal<EntityManager> entityManagerThreadLocal = new ThreadLocal<>();

    private JpaPersistenceUnit(String persistenceUnit) {
        this.emf = Persistence.createEntityManagerFactory(persistenceUnit);
    }

    public static JpaPersistenceUnit forName(String persistenceUnitName) {
        if (persistenceUnits.containsKey(persistenceUnitName)) {
            return persistenceUnits.get(persistenceUnitName);
        }
        JpaPersistenceUnit persistenceUnit = new JpaPersistenceUnit(persistenceUnitName);
        persistenceUnits.put(persistenceUnitName, persistenceUnit);
        return persistenceUnit;
    }

    public EntityManager getThreadLocalEntityManager() {
        EntityManager entityManager = entityManagerThreadLocal.get();
        if (entityManager == null) {
            entityManager = emf.createEntityManager();
            entityManagerThreadLocal.set(entityManager);
        }
        return entityManager;
    }

    public EntityManager getNewEntityManager() {
        return emf.createEntityManager();
    }

    public EntityManagerProvider getEntityManagerProvider() {
        return new JpaEntityManagerProvider(this);
    }

    public TransactionManager getTransactionManager() {
        return new JpaTransactionManager(this);
    }

}
