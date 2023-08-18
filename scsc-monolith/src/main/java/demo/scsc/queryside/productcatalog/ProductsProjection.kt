package demo.scsc.queryside.productcatalog;

import demo.scsc.Constants;
import demo.scsc.api.productcatalog.ProductUpdateReceivedEvent;
import demo.scsc.api.productcatalog.ProductListQuery;
import demo.scsc.api.productcatalog.ProductListQueryResponse;
import demo.scsc.config.JpaPersistenceUnit;
import jakarta.persistence.EntityManager;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@ProcessingGroup(Constants.PROCESSING_GROUP_PRODUCT)
public class ProductsProjection {

    private static final String GET_PRODUCTS_SQL = "SELECT p FROM ProductEntity AS p";

    @EventHandler
    public void on(ProductUpdateReceivedEvent productUpdateReceivedEvent) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();

        if (productUpdateReceivedEvent.onSale()) {
            em.merge(toEntity(productUpdateReceivedEvent));
        } else {
            ProductEntity productEntity = em.find(ProductEntity.class, productUpdateReceivedEvent.id());
            if (productEntity != null) em.remove(productEntity);
        }

        em.getTransaction().commit();
    }

    @QueryHandler
    public ProductListQueryResponse getProducts(ProductListQuery query) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();

        List<ProductEntity> products = em.createQuery(getProductsSql(query.sortBy()), ProductEntity.class).getResultList();
        ProductListQueryResponse response = new ProductListQueryResponse(
                products.stream()
                        .map(productEntity -> new ProductListQueryResponse.ProductInfo(
                                productEntity.getId(),
                                productEntity.getName(),
                                productEntity.getDesc(),
                                productEntity.getPrice(),
                                productEntity.getImage()
                        ))
                        .collect(Collectors.toList())
        );

        em.getTransaction().commit();
        return response;
    }

    @NotNull
    private ProductEntity toEntity(@NotNull ProductUpdateReceivedEvent productUpdateReceivedEvent) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(productUpdateReceivedEvent.id());
        productEntity.setName(productUpdateReceivedEvent.name());
        productEntity.setDesc(productUpdateReceivedEvent.desc());
        productEntity.setPrice(productUpdateReceivedEvent.price());
        productEntity.setImage(productUpdateReceivedEvent.image());
        return productEntity;
    }


    private static String getProductsSql(String sortBy) {
        return GET_PRODUCTS_SQL + " ORDER BY " + sortBy;
    }
}
