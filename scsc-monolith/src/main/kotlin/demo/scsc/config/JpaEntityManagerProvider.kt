package demo.scsc.config

import jakarta.persistence.EntityManager
import org.axonframework.common.jpa.EntityManagerProvider

class JpaEntityManagerProvider internal constructor(private val jpaPersistence: JpaPersistenceUnit) :
    EntityManagerProvider {
    override fun getEntityManager(): EntityManager? = jpaPersistence.threadLocalEntityManager
}
