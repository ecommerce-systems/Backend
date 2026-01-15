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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    @DisplayName("상품 생성 시, 카테고리를 확인하고 Product와 ProductSearch를 모두 저장한다")
    void createProduct_shouldSaveProductAndProductSearch() {
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
    @DisplayName("V1 페이징 검색 시, ProductRepository를 통해 결과를 반환한다")
    void searchResultsV1_shouldReturnPageFromProductRepository() {
        String keyword = "test";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = mock(Page.class);

        when(productRepository.findByProdNameContainingIgnoreCase(keyword, pageable)).thenReturn(page);

        Page<Product> result = productService.searchResultsV1(keyword, pageable);

        assertThat(result).isNotNull();
        verify(productRepository).findByProdNameContainingIgnoreCase(keyword, pageable);
    }

    @Test
    @DisplayName("상품 수정 시, Product와 ProductSearch 엔티티를 모두 업데이트한다")
    void updateProduct_shouldUpdateProductAndProductSearch() {
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
