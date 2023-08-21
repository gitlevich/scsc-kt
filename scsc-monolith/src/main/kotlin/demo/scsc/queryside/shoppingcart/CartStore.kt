package demo.scsc.queryside.shoppingcart

import demo.scsc.api.shoppingCart.GetCartQuery
import demo.scsc.config.microstream.MicrostreamStore.forLocation
import one.microstream.storage.types.StorageManager
import java.util.*

class CartStore {
    private val storageManager: StorageManager = forLocation("carts")!!

    fun saveCart(owner: String, cartId: UUID) {
        val cartsRoot = getCartsRoot(true)
        cartsRoot.byUser[owner] = cartId
        cartsRoot.byId[cartId] = GetCartQuery.Response(cartId, LinkedList())
        storageManager.store(cartsRoot.byUser)
        storageManager.store(cartsRoot.byId)
    }

    fun saveProduct(cartId: UUID, productId: UUID) {
        val getCartQueryResponse = cartsRoot.byId[cartId]
            ?: throw IllegalStateException("No cart with id $cartId")

        val withAnother = getCartQueryResponse.copy(products = getCartQueryResponse.products + productId)
        storageManager.store(withAnother.products)
        storageManager.store(withAnother)
    }

    fun removeProduct(cartId: UUID, productId: UUID?) {
        val (_, products) = cartsRoot.byId[cartId]
            ?: throw IllegalStateException("No cart with id $cartId")
        storageManager.store(products - productId)
    }

    fun getOwnersCarts(owner: String): GetCartQuery.Response? {
        val cartsRoot = cartsRoot
        val cartId = cartsRoot.byUser[owner]
        return cartId?.let { cartsRoot.byId[it] }
    }

    fun removeCart(cartId: UUID) {
        cartsRoot.byId.remove(cartId)
        cartsRoot.byUser.entries.removeIf { (_, value): Map.Entry<String, UUID> -> value == cartId }
        storageManager.store(cartsRoot.byUser)
        storageManager.store(cartsRoot.byId)
    }

    fun reset() {
        storageManager.setRoot(CartsRoot())
        storageManager.storeRoot()
    }

    private val cartsRoot: CartsRoot
        get() = getCartsRoot(false)

    private fun getCartsRoot(create: Boolean): CartsRoot {
        var cartsRoot = storageManager.root() as CartsRoot
        if (create) {
            cartsRoot = CartsRoot()
            storageManager.setRoot(cartsRoot)
            storageManager.storeRoot()
        }
        return cartsRoot
    }

    class CartsRoot {
        val byUser: MutableMap<String, UUID> = HashMap()
        val byId: MutableMap<UUID, GetCartQuery.Response> = HashMap()
        override fun toString(): String {
            return "StorageRoot{" +
                    "cartsByUser=" + byUser +
                    ", cartsById=" + byId +
                    '}'
        }
    }
}
