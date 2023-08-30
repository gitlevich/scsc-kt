package demo.scsc.queryside.shoppingcart

import demo.scsc.api.shoppingcart.GetCartQuery
import demo.scsc.config.microstream.MicrostreamStore.forLocation
import one.microstream.storage.types.StorageManager
import java.util.*

class CartStore {
    private val storageManager: StorageManager = forLocation("carts")

    fun saveCart(owner: String, cartId: UUID) {
        cartsRoot.byUser[owner] = cartId
        cartsRoot.byId[cartId] = GetCartQuery.Response(cartId, LinkedList())
        storageManager.store(cartsRoot.byUser)
        storageManager.store(cartsRoot.byId)
    }

    fun saveProduct(cartId: UUID, productId: UUID) {
        val cart = cartsRoot.byId[cartId]
            ?: throw IllegalStateException("No cart with id $cartId")

        cartsRoot.byId[cartId] = cart + productId
        storageManager.store(cartsRoot.byId)
    }

    fun removeProduct(cartId: UUID, productId: UUID) {
        val cart = cartsRoot.byId[cartId]
            ?: throw IllegalStateException("No cart with id $cartId")
        cartsRoot.byId[cartId] = cart - productId
        storageManager.store(cartsRoot.byId)
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
        get() = storageManager.root() as? CartsRoot ?: let {
            val cartsRoot = CartsRoot()
            storageManager.setRoot(cartsRoot)
            storageManager.storeRoot()
            cartsRoot
        }

    data class CartsRoot(
        val byUser: MutableMap<String, UUID> = mutableMapOf(),
        val byId: MutableMap<UUID, GetCartQuery.Response> = mutableMapOf()
    )
}
