package demo.scsc.queryside

import demo.scsc.Constants.SCSC
import demo.scsc.config.JpaPersistenceUnit
import jakarta.persistence.EntityManager
import jakarta.persistence.NoResultException
import org.axonframework.queryhandling.QueryExecutionException

fun tx(f: (EntityManager) -> Unit) {
    val em: EntityManager = JpaPersistenceUnit.forName(SCSC)!!.newEntityManager
    em.transaction.begin()
    f(em)
    em.transaction.commit()
    em.close()
}

fun <QUERY, RESPONSE> answer(q: QUERY, f: (EntityManager) -> RESPONSE): RESPONSE = try {
    f(JpaPersistenceUnit.forName(SCSC)!!.newEntityManager)
} catch (e: NoResultException) {
    throw QueryExecutionException("Unable to execute query ${q.toString()}", null)
}
