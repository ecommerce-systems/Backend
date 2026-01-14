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

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductSearchRepository productSearchRepository;
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
    private ProductService productService;

    @Test
    @DisplayName("createProduct should resolve categories and save product")
    void createProduct_shouldSaveProduct() {
        ProductCreateRequestDto dto = new ProductCreateRequestDto();
        dto.setProductCode(123);
        dto.setProdName("Test Product");
        dto.setPrice(BigDecimal.valueOf(100));
        dto.setProductTypeName("Type1");

        when(productTypeRepository.findByProductTypeName("Type1"))
                .thenReturn(Optional.of(new ProductType(1, "Type1")));
        
        when(productRepository.save(any(Product.class))).thenAnswer(i -> {
            Product p = i.getArgument(0);
            p.setProductId(1);
            return p;
        });

        Product result = productService.createProduct(dto);

        assertThat(result).isNotNull();
        assertThat(result.getProdName()).isEqualTo("Test Product");
        verify(productRepository).save(any(Product.class));
        verify(productSearchRepository).save(any(ProductSearch.class));
    }

    @Test
    @DisplayName("updateProduct should update fields and categories")
    void updateProduct_shouldUpdateProduct() {
        int productId = 1;
        ProductUpdateRequestDto dto = new ProductUpdateRequestDto();
        dto.setProdName("Updated Name");
        dto.setPrice(BigDecimal.valueOf(200));

        Product existingProduct = Product.builder().productId(productId).prodName("Old Name").build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        Product result = productService.updateProduct(productId, dto);

        assertThat(result.getProdName()).isEqualTo("Updated Name");
        verify(productRepository).save(existingProduct);
        verify(productSearchRepository).save(any(ProductSearch.class));
    }
}