package demo.scsc.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.axonframework.common.transaction.Transaction;
import org.axonframework.common.transaction.TransactionManager;

public class JpaTransactionManager implements TransactionManager {

    final JpaPersistenceUnit axonPersistenceUnit;
    public JpaTransactionManager(JpaPersistenceUnit axonPersistenceUnit) {
        this.axonPersistenceUnit = axonPersistenceUnit;
    }

    @Override
    public Transaction startTransaction() {
        final EntityManager em = axonPersistenceUnit.getThreadLocalEntityManager();
        final EntityTransaction tx = em.getTransaction();
        if (tx.isActive()) {
            return new Transaction() {
                @Override
                public void commit() {
                }

                @Override
                public void rollback() {
                }
            };
        }
        tx.begin();
        return new Transaction() {
            @Override
            public void commit() {
                tx.commit();
            }

            @Override
            public void rollback() {
                tx.rollback();
            }
        };
    }
}
