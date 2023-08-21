package demo.scsc.util

import demo.scsc.Constants.SCSC
import demo.scsc.config.JpaPersistenceUnit
import jakarta.persistence.EntityManager
import jakarta.persistence.NoResultException
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.queryhandling.QueryExecutionException

fun <RESULT> tx(f: (EntityManager) -> RESULT): RESULT =
    JpaPersistenceUnit.forName(SCSC)!!.newEntityManager.run {
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

fun <QUERY, RESPONSE> answer(q: QUERY, f: (EntityManager) -> RESPONSE): RESPONSE = try {
    f(JpaPersistenceUnit.forName(SCSC)!!.newEntityManager)
} catch (e: NoResultException) {
    throw QueryExecutionException("Unable to execute query ${q.toString()}", null)
}

fun attemptTo(f: () -> Unit) = try {
    f()
} catch (e: Exception) {
    throw CommandExecutionException(e.message, e)
}
