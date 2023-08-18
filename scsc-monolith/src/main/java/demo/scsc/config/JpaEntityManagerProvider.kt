package demo.scsc.config;

import jakarta.persistence.EntityManager;
import org.axonframework.common.jpa.EntityManagerProvider;

public class JpaEntityManagerProvider implements EntityManagerProvider {
    private final JpaPersistenceUnit jpaPersistence;

    JpaEntityManagerProvider(JpaPersistenceUnit jpaPersistence) {
        this.jpaPersistence = jpaPersistence;
    }

    @Override
    public EntityManager getEntityManager() {
        return jpaPersistence.getThreadLocalEntityManager();
    }
}
