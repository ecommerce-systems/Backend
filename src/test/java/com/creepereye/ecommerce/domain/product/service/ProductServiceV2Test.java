package com.creepereye.ecommerce.domain.product.service;

import com.creepereye.ecommerce.domain.product.entity.Product;
import com.creepereye.ecommerce.domain.product.entity.ProductSearch;
import com.creepereye.ecommerce.domain.product.repository.ProductRepository;
import com.creepereye.ecommerce.domain.product.repository.ProductSearchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceV2Test {

    @Mock private ProductRepository productRepository;
    @Mock private ProductSearchRepository productSearchRepository;

    @InjectMocks
    private ProductServiceV2 productService;

    @Test
    @DisplayName("Search Names V2 - Should use Prefix Search first")
    void searchNamesV2_prefixSearch() {
        String keyword = "Start";
        List<ProductSearch> prefixResults = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            prefixResults.add(ProductSearch.builder().productId(i).prodName("Start Product " + i).build());
        }

        when(productSearchRepository.findTop20ByProdNameStartingWithIgnoreCase(keyword))
                .thenReturn(prefixResults);

        List<String> results = productService.searchNamesV2(keyword);

        assertThat(results).hasSize(5);
        verify(productSearchRepository).findTop20ByProdNameStartingWithIgnoreCase(keyword);
        verify(productSearchRepository, never()).findTop20ByProdNameContainingIgnoreCase(anyString());
    }

    @Test
    @DisplayName("Sync To Search - Should save to ProductSearchRepository")
    void syncToSearch_success() {
        Product product = Product.builder().productId(1).prodName("Test").build();
        productService.syncToSearch(product);
        verify(productSearchRepository).save(any(ProductSearch.class));
    }

    @Test
    @DisplayName("Sync All Data - Should sync all products")
    void syncAllProductData_success() {
        when(productSearchRepository.count()).thenReturn(0L);
        when(productRepository.findAll()).thenReturn(List.of(Product.builder().productId(1).build()));

        productService.syncAllProductData();

        verify(productSearchRepository).save(any(ProductSearch.class));
    }
}
