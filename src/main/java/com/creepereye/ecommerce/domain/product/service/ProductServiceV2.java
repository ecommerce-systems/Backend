package com.creepereye.ecommerce.domain.product.service;

import com.creepereye.ecommerce.domain.product.dto.ProductSearchResponseDto;
import com.creepereye.ecommerce.domain.product.entity.Product;
import com.creepereye.ecommerce.domain.product.entity.ProductSearch;
import com.creepereye.ecommerce.domain.product.repository.ProductRepository;
import com.creepereye.ecommerce.domain.product.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceV2 {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceV2.class);

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;

    public Optional<ProductSearch> findByIdV2(Integer id) {
        return productSearchRepository.findById(id);
    }

    public List<String> searchNamesV2(String keyword) {
        List<String> results = productSearchRepository.findTop20ByProdNameStartingWithIgnoreCase(keyword).stream()
                .map(ProductSearch::getProdName)
                .collect(Collectors.toList());

        if (results.size() < 5) {
            List<String> fallbackResults = productSearchRepository.findTop20ByProdNameContainingIgnoreCase(keyword).stream()
                    .map(ProductSearch::getProdName)
                    .filter(name -> !results.contains(name))
                    .collect(Collectors.toList());
            results.addAll(fallbackResults);
        }
        return results.stream().limit(20).collect(Collectors.toList());
    }

    public List<String> searchNamesV2WithFilters(String keyword, String productType, String department, String productGroup, String section) {
        return productSearchRepository.searchByMultipleCategories(keyword, productType, department, productGroup, section).stream()
                .map(ProductSearch::getProdName)
                .limit(20)
                .collect(Collectors.toList());
    }

    public void syncToSearch(Product product) {
        try {
            ProductSearch searchEntity = ProductSearch.builder()
                    .productId(product.getProductId())
                    .prodName(product.getProdName())
                    .detailDesc(product.getDetailDesc())
                    .price(product.getPrice())
                    .imageUrl(product.getProductImage() != null ? product.getProductImage().getImageUrl() : null)
                    .productTypeName(product.getProductType() != null ? product.getProductType().getProductTypeName() : null)
                    .productGroupName(product.getProductGroup() != null ? product.getProductGroup().getProductGroupName() : null)
                    .graphicalAppearanceName(product.getGraphicalAppearance() != null ? product.getGraphicalAppearance().getGraphicalAppearanceName() : null)
                    .colourGroupName(product.getColourGroup() != null ? product.getColourGroup().getColourGroupName() : null)
                    .perceivedColourValueName(product.getPerceivedColourValue() != null ? product.getPerceivedColourValue().getPerceivedColourValueName() : null)
                    .perceivedColourMasterName(product.getPerceivedColourMaster() != null ? product.getPerceivedColourMaster().getPerceivedColourMasterName() : null)
                    .departmentName(product.getDepartment() != null ? product.getDepartment().getDepartmentName() : null)
                    .indexName(product.getIndex() != null ? product.getIndex().getIndexName() : null)
                    .indexGroupName(product.getIndexGroup() != null ? product.getIndexGroup().getIndexGroupName() : null)
                    .sectionName(product.getSection() != null ? product.getSection().getSectionName() : null)
                    .garmentGroupName(product.getGarmentGroup() != null ? product.getGarmentGroup().getGarmentGroupName() : null)
                    .build();
            productSearchRepository.save(searchEntity);
        } catch (Exception e) {
            log.error("Failed to sync product to search entity", e);
        }
    }

    @Transactional
    public void deleteById(Integer id) {
        productSearchRepository.deleteById(id);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncAllProductData() {
        if (productSearchRepository.count() == 0) {
            log.info("🚀 Starting initial data migration for ProductSearch (V2)...");
            List<Product> allProducts = productRepository.findAll();
            allProducts.forEach(this::syncToSearch);
            log.info("✅ Migration complete. Synced {} products to ProductSearch.", allProducts.size());
        }
    }
}
