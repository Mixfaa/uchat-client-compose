package uchat.message

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DatabindContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase
import uchat.message.transactions.TransactionBase
import uchat.message.transactions.TransactionType

class TransactionTypeIdResolver : TypeIdResolverBase() {
    private lateinit var baseType: JavaType

    override fun init(bt: JavaType?) {
        baseType = bt!!
    }

    override fun idFromValue(value: Any?): String {
        if (value !is TransactionBase) throw Exception("Cant resolve type")
        return TransactionType.fromTransactionClass(value).ordinal.toString()
    }

    override fun idFromValueAndType(value: Any?, suggestedType: Class<*>?): String {
        TODO("Not yet implemented")
    }

    override fun typeFromId(context: DatabindContext?, id: String?): JavaType {
        val transactionType = TransactionType.forValue(id!!)

        return context!!.constructSpecializedType(
            baseType,
            transactionType.transactionClass.java
        )
    }

    override fun getMechanism(): JsonTypeInfo.Id {
        return JsonTypeInfo.Id.CUSTOM
    }
}