package demo.scsc.queryside.shoppingcart

import demo.scsc.api.shoppingCart.GetCartQueryResponse
import demo.scsc.config.microstream.MicrostreamStore.forLocation
import one.microstream.storage.types.StorageManager
import java.util.*

class CartStore {
    private val storageManager: StorageManager

    init {
        storageManager = forLocation("carts")!!
    }

    fun saveCart(owner: String, cartId: UUID) {
        val cartsRoot = getCartsRoot(true)
        cartsRoot!!.byUser[owner] = cartId
        cartsRoot.byId[cartId] = GetCartQueryResponse(cartId, LinkedList())
        storageManager.store(cartsRoot.byUser)
        storageManager.store(cartsRoot.byId)
    }

    fun saveProduct(cartId: UUID, productId: UUID) {
        val cartsRoot = cartsRoot ?: throw IllegalStateException("No cart storage")
        val getCartQueryResponse = cartsRoot.byId[cartId]
            ?: throw IllegalStateException("No cart with id $cartId")

        val withAnother = getCartQueryResponse.copy(products = getCartQueryResponse.products + productId)
        storageManager.store(withAnother.products)
        storageManager.store(withAnother)
    }

    fun removeProduct(cartId: UUID, productId: UUID?) {
        val cartsRoot = cartsRoot ?: throw IllegalStateException("No cart storage")
        val (_, products) = cartsRoot.byId[cartId]
            ?: throw IllegalStateException("No cart with id $cartId")
        storageManager.store(products - productId)
    }

    fun getOwnersCarts(owner: String): GetCartQueryResponse? {
        var getCartQueryResponse: GetCartQueryResponse? = null
        val cartsRoot = cartsRoot
        if (cartsRoot != null) {
            val cartId = cartsRoot.byUser[owner]
            if (cartId != null) {
                getCartQueryResponse = cartsRoot.byId[cartId]
            }
        }
        return getCartQueryResponse
    }

    fun removeCart(cartId: UUID) {
        val cartsRoot = cartsRoot
        cartsRoot!!.byId.remove(cartId)
        cartsRoot.byUser.entries.removeIf { (_, value): Map.Entry<String, UUID> -> value == cartId }
        storageManager.store(cartsRoot.byUser)
        storageManager.store(cartsRoot.byId)
    }

    fun reset() {
        storageManager!!.setRoot(CartsRoot())
        storageManager.storeRoot()
    }

    private val cartsRoot: CartsRoot?
        private get() = getCartsRoot(false)

    private fun getCartsRoot(create: Boolean): CartsRoot? {
        var cartsRoot = storageManager!!.root() as CartsRoot
        if (cartsRoot == null && create) {
            cartsRoot = CartsRoot()
            storageManager.setRoot(cartsRoot)
            storageManager.storeRoot()
        }
        return cartsRoot
    }

    class CartsRoot {
        val byUser: MutableMap<String, UUID> = HashMap()
        val byId: MutableMap<UUID, GetCartQueryResponse> = HashMap()
        override fun toString(): String {
            return "StorageRoot{" +
                    "cartsByUser=" + byUser +
                    ", cartsById=" + byId +
                    '}'
        }
    }
}
