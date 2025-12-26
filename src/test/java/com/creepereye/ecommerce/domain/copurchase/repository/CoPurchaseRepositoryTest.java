package com.creepereye.ecommerce.domain.copurchase.repository;

import com.creepereye.ecommerce.domain.product.entity.Product;
import com.creepereye.ecommerce.domain.copurchase.entity.CoPurchase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CoPurchaseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CoPurchaseRepository coPurchaseRepository;

    @Test
    void findBySourceProductProductIdOrderByCoPurchaseCountDesc() {
        // given
        Product product1 = Product.builder().prodName("Product 1").build();
        Product product2 = Product.builder().prodName("Product 2").build();
        Product product3 = Product.builder().prodName("Product 3").build();
        entityManager.persist(product1);
        entityManager.persist(product2);
        entityManager.persist(product3);

        CoPurchase coPurchase1 = CoPurchase.builder().sourceProduct(product1).targetProduct(product2).coPurchaseCount(10L).build();
        CoPurchase coPurchase2 = CoPurchase.builder().sourceProduct(product1).targetProduct(product3).coPurchaseCount(20L).build();
        entityManager.persist(coPurchase1);
        entityManager.persist(coPurchase2);
        entityManager.flush();

        // when
        List<CoPurchase> result = coPurchaseRepository.findBySourceProductProductIdOrderByCoPurchaseCountDesc(product1.getProductId(), PageRequest.of(0, 10));

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTargetProduct()).isEqualTo(product3);
        assertThat(result.get(1).getTargetProduct()).isEqualTo(product2);
    }
}