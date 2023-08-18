package demo.scsc.config

import org.axonframework.common.transaction.Transaction
import org.axonframework.common.transaction.TransactionManager

class JpaTransactionManager(val axonPersistenceUnit: JpaPersistenceUnit) : TransactionManager {
    override fun startTransaction(): Transaction {
        val tx = axonPersistenceUnit.threadLocalEntityManager!!.transaction
        if (tx.isActive) {
            return object : Transaction {
                override fun commit() {}
                override fun rollback() {}
            }
        }
        tx.begin()
        return object : Transaction {
            override fun commit() {
                tx.commit()
            }

            override fun rollback() {
                tx.rollback()
            }
        }
    }
}
