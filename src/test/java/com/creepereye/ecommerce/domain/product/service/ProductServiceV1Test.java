package com.creepereye.ecommerce.domain.product.service;

import com.creepereye.ecommerce.domain.product.dto.ProductCreateRequestDto;
import com.creepereye.ecommerce.domain.product.dto.ProductUpdateRequestDto;
import com.creepereye.ecommerce.domain.product.entity.*;
import com.creepereye.ecommerce.domain.product.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceV1Test {

    @Mock private ProductRepository productRepository;
    @Mock private ProductServiceV2 productServiceV2;
    @Mock private ColourGroupRepository colourGroupRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private GarmentGroupRepository garmentGroupRepository;
    @Mock private GraphicalAppearanceRepository graphicalAppearanceRepository;
    @Mock private IndexGroupRepository indexGroupRepository;
    @Mock private IndexRepository indexRepository;
    @Mock private PerceivedColourMasterRepository perceivedColourMasterRepository;
    @Mock private PerceivedColourValueRepository perceivedColourValueRepository;
    @Mock private ProductGroupRepository productGroupRepository;
    @Mock private ProductTypeRepository productTypeRepository;
    @Mock private SectionRepository sectionRepository;

    @InjectMocks
    private ProductServiceV1 productService;

    @Test
    @DisplayName("Create Product - Should resolve categories and save")
    void createProduct_success() {
        ProductCreateRequestDto dto = new ProductCreateRequestDto();
        dto.setProductCode(1001);
        dto.setProdName("Test Product");
        dto.setProductTypeName("Type A");

        when(productTypeRepository.findByProductTypeName("Type A"))
                .thenReturn(Optional.of(new ProductType(1, "Type A")));
        
        Product savedProduct = Product.builder()
                .productId(1)
                .productCode(1001)
                .prodName("Test Product")
                .productType(new ProductType(1, "Type A"))
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        Product result = productService.createProduct(dto);

        assertThat(result).isNotNull();
        verify(productRepository).save(any(Product.class));
        verify(productServiceV2).syncToSearch(any(Product.class));
    }

    @Test
    @DisplayName("Search Names V1 - Should use ProductRepository")
    void searchNamesV1_success() {
        String keyword = "test";
        Product p1 = Product.builder().productId(1).prodName("Test Product 1").build();
        when(productRepository.findTop20ByProdNameContainingIgnoreCase(keyword))
                .thenReturn(List.of(p1));

        List<String> results = productService.searchNamesV1(keyword);

        assertThat(results).hasSize(1);
        verify(productRepository).findTop20ByProdNameContainingIgnoreCase(keyword);
    }
}
