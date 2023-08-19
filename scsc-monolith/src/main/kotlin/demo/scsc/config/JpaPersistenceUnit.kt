package demo.scsc.config

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import org.axonframework.common.jpa.EntityManagerProvider
import org.axonframework.common.transaction.TransactionManager

class JpaPersistenceUnit private constructor(persistenceUnit: String?) {
    private val emf: EntityManagerFactory
    private val entityManagerThreadLocal = ThreadLocal<EntityManager>()

    init {
        emf = Persistence.createEntityManagerFactory(persistenceUnit)
    }

    val threadLocalEntityManager: EntityManager?
        get() {
            var entityManager = entityManagerThreadLocal.get()
            if (entityManager == null) {
                entityManager = emf.createEntityManager()
                entityManagerThreadLocal.set(entityManager)
            }
            return entityManager
        }
    val newEntityManager: EntityManager
        get() = emf.createEntityManager()
    val entityManagerProvider: EntityManagerProvider
        get() = JpaEntityManagerProvider(this)
    val transactionManager: TransactionManager
        get() = JpaTransactionManager(this)

    companion object {
        private val persistenceUnits: MutableMap<String, JpaPersistenceUnit> = mutableMapOf()

        fun forName(persistenceUnitName: String): JpaPersistenceUnit? {
            if (persistenceUnits.containsKey(persistenceUnitName)) {
                return persistenceUnits[persistenceUnitName]
            }
            val persistenceUnit = JpaPersistenceUnit(persistenceUnitName)
            persistenceUnits[persistenceUnitName] = persistenceUnit
            return persistenceUnit
        }
    }
}
