package demo.scsc.commandside.order;

import demo.scsc.Constants;
import demo.scsc.api.productcatalog.ProductUpdateReceivedEvent;
import demo.scsc.config.JpaPersistenceUnit;
import jakarta.persistence.EntityManager;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@ProcessingGroup(Constants.PROCESSING_GROUP_PRODUCT)
public class ProductValidation {

    @EventHandler
    public void on(ProductUpdateReceivedEvent productUpdateReceivedEvent) {
        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        em.getTransaction().begin();
        em.merge(toEntity(productUpdateReceivedEvent));
        em.getTransaction().commit();
    }

    public ProductValidationInfo forProduct (UUID id) {

        ProductValidationInfo productValidationInfo = null;

        EntityManager em = JpaPersistenceUnit.forName("SCSC").getNewEntityManager();
        ProductValidationEntity productValidationEntity = em.find(ProductValidationEntity.class, id);
        if (productValidationEntity != null) {
            productValidationInfo = new ProductValidationInfo(productValidationEntity);
        }

        return productValidationInfo;
    }

    @NotNull
    private ProductValidationEntity toEntity(@NotNull ProductUpdateReceivedEvent productUpdateReceivedEvent) {
        ProductValidationEntity productValidationEntity = new ProductValidationEntity();
        productValidationEntity.setId(productUpdateReceivedEvent.id());
        productValidationEntity.setName(productUpdateReceivedEvent.name());
        productValidationEntity.setPrice(productUpdateReceivedEvent.price());
        productValidationEntity.setOnSale(productUpdateReceivedEvent.onSale());
        return productValidationEntity;
    }

    public static class ProductValidationInfo {

        public final String name;
        public final BigDecimal price;
        public final boolean onSale;

        public ProductValidationInfo (ProductValidationEntity productValidationEntity) {
            this.name = productValidationEntity.getName();
            this.price = productValidationEntity.getPrice();
            this.onSale = productValidationEntity.isOnSale();
        }
    }
}
