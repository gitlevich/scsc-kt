package demo.scsc.util

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import demo.scsc.Constants.SCSC
import demo.scsc.config.AxonFramework
import demo.scsc.config.JpaPersistenceUnit
import jakarta.persistence.EntityManager
import jakarta.persistence.NoResultException
import org.axonframework.queryhandling.QueryExecutionException

fun <RESULT> tx(config: Config, f: (EntityManager) -> RESULT): RESULT =
    JpaPersistenceUnit.forName(SCSC, config)!!.newEntityManager.run {
        try {
            transaction.begin()
            f(this).also {
                transaction.commit()
                close()
            }
        } catch (e: Exception) {
            transaction.rollback()
            throw e
        }
    }

fun <QUERY, RESPONSE> answer(
    q: QUERY,
    config: Config,
    f: (EntityManager) -> RESPONSE
): RESPONSE = try {
    f(JpaPersistenceUnit.forName(SCSC, config)!!.newEntityManager)
} catch (e: NoResultException) {
    throw QueryExecutionException("Unable to execute query ${q.toString()}", null)
}

