package demo.scsc.queryside.shoppingcart;

import demo.scsc.api.shoppingcart.GetCartQueryResponse;
import demo.scsc.config.microstream.MicrostreamStore;
import one.microstream.storage.types.StorageManager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

public class CartStore {

    private final StorageManager storageManager;

    public CartStore() {
        storageManager = MicrostreamStore.forLocation("carts");
    }

    public void saveCart(String owner, UUID cartId) {

        CartsRoot cartsRoot = getCartsRoot(true);

        cartsRoot.byUser.put(owner, cartId);
        cartsRoot.byId.put(cartId, new GetCartQueryResponse(cartId, new LinkedList<>()));

        storageManager.store(cartsRoot.byUser);
        storageManager.store(cartsRoot.byId);

    }


    public void saveProduct(UUID cartId, UUID productId) {

        CartsRoot cartsRoot = getCartsRoot();
        if (cartsRoot == null) {
            throw new IllegalStateException("No cart storage");
        }

        GetCartQueryResponse getCartQueryResponse = cartsRoot.byId.get(cartId);
        if (getCartQueryResponse == null) {
            throw new IllegalStateException("No cart with id " + cartId);
        }

        getCartQueryResponse.products().add(productId);


        storageManager.store(getCartQueryResponse.products());
        storageManager.store(getCartQueryResponse);
    }

    public void removeProduct(UUID cartId, UUID productId) {
        CartsRoot cartsRoot = getCartsRoot();
        if (cartsRoot == null) {
            throw new IllegalStateException("No cart storage");
        }

        GetCartQueryResponse getCartQueryResponse = cartsRoot.byId.get(cartId);
        if (getCartQueryResponse == null) {
            throw new IllegalStateException("No cart with id " + cartId);
        }

        getCartQueryResponse.products().remove(productId);
        storageManager.store(getCartQueryResponse.products());
    }

    public GetCartQueryResponse getOwnersCarts(String owner) {
        GetCartQueryResponse getCartQueryResponse = null;
        CartsRoot cartsRoot = getCartsRoot();
        if (cartsRoot != null) {
            UUID cartId = cartsRoot.byUser.get(owner);
            if (cartId != null) {
                getCartQueryResponse = cartsRoot.byId.get(cartId);
            }
        }
        return getCartQueryResponse;
    }

    public void removeCart(UUID cartId) {
        CartsRoot cartsRoot = getCartsRoot();
        cartsRoot.byId.remove(cartId);
        cartsRoot.byUser.entrySet().removeIf(entry -> entry.getValue().equals(cartId));
        storageManager.store(cartsRoot.byUser);
        storageManager.store(cartsRoot.byId);
    }

    public void reset() {
        storageManager.setRoot(new CartsRoot());
        storageManager.storeRoot();
    }


    private CartsRoot getCartsRoot() {
        return getCartsRoot(false);
    }

    private CartsRoot getCartsRoot(boolean create) {
        CartsRoot cartsRoot = (CartsRoot) storageManager.root();
        if (cartsRoot == null && create) {
            cartsRoot = new CartsRoot();
            storageManager.setRoot(cartsRoot);
            storageManager.storeRoot();
        }
        return cartsRoot;
    }


    public static class CartsRoot {
        public final Map<String, UUID> byUser = new HashMap<>();
        public final Map<UUID, GetCartQueryResponse> byId = new HashMap<>();

        @Override
        public String toString() {
            return "StorageRoot{" +
                    "cartsByUser=" + byUser +
                    ", cartsById=" + byId +
                    '}';
        }
    }
}
