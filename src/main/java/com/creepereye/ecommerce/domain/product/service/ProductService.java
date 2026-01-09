package com.creepereye.ecommerce.domain.product.service;


import com.creepereye.ecommerce.domain.product.dto.ProductCreateRequestDto;
import com.creepereye.ecommerce.domain.product.dto.ProductSearchResponseDto;
import com.creepereye.ecommerce.domain.product.dto.ProductUpdateRequestDto;
import com.creepereye.ecommerce.domain.product.entity.*;
import com.creepereye.ecommerce.domain.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductSearchRepository productSearchRepository;
    private final ColourGroupRepository colourGroupRepository;
    private final DepartmentRepository departmentRepository;
    private final GarmentGroupRepository garmentGroupRepository;
    private final GraphicalAppearanceRepository graphicalAppearanceRepository;
    private final IndexGroupRepository indexGroupRepository;
    private final IndexRepository indexRepository;
    private final PerceivedColourMasterRepository perceivedColourMasterRepository;
    private final PerceivedColourValueRepository perceivedColourValueRepository;
    private final ProductGroupRepository productGroupRepository;
    private final ProductTypeRepository productTypeRepository;
    private final SectionRepository sectionRepository;



    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Integer id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product createProduct(ProductCreateRequestDto dto) {
        log.info("Attempting to create product with DTO: {}", dto);
        Product product = Product.builder()
                .productCode(dto.getProductCode())
                .prodName(dto.getProdName())
                .detailDesc(dto.getDetailDesc())
                .price(dto.getPrice())
                .productType(new ProductType(null, dto.getProductTypeName()))
                .productGroup(new ProductGroup(null, dto.getProductGroupName()))
                .graphicalAppearance(new GraphicalAppearance(null, dto.getGraphicalAppearanceName()))
                .colourGroup(new ColourGroup(null, dto.getColourGroupName()))
                .perceivedColourValue(new PerceivedColourValue(null, dto.getPerceivedColourValueName()))
                .perceivedColourMaster(new PerceivedColourMaster(null, dto.getPerceivedColourMasterName()))
                .department(new Department(null, dto.getDepartmentName()))
                .index(new Index(null, dto.getIndexName()))
                .indexGroup(new IndexGroup(null, dto.getIndexGroupName()))
                .section(new Section(null, dto.getSectionName()))
                .garmentGroup(new GarmentGroup(null, dto.getGarmentGroupName()))
                .build();
        log.debug("Product entity built: {}", product);
        Product savedProduct = save(product);
        
        // V2 Sync
        syncToSearch(savedProduct);
        
        log.info("Product created and saved: {}", savedProduct);
        return savedProduct;
    }

    @Transactional
    public Product updateProduct(Integer id, ProductUpdateRequestDto dto) {
        log.info("🔄 Attempting to update product with ID: {} and DTO: {}", id, dto);
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Product not found with id: {}", id);
                    return new IllegalArgumentException("Product not found with id: " + id);
                });
        log.debug("🔍 Found existing product: {}", existingProduct);

        existingProduct.setProductCode(dto.getProductCode());
        existingProduct.setProdName(dto.getProdName());
        existingProduct.setDetailDesc(dto.getDetailDesc());
        existingProduct.setPrice(dto.getPrice());

        existingProduct.setProductType(resolveProductType(new ProductType(null, dto.getProductTypeName())));
        existingProduct.setProductGroup(resolveProductGroup(new ProductGroup(null, dto.getProductGroupName())));
        existingProduct.setGraphicalAppearance(resolveGraphicalAppearance(new GraphicalAppearance(null, dto.getGraphicalAppearanceName())));
        existingProduct.setColourGroup(resolveColourGroup(new ColourGroup(null, dto.getColourGroupName())));
        existingProduct.setPerceivedColourValue(resolvePerceivedColourValue(new PerceivedColourValue(null, dto.getPerceivedColourValueName())));
        existingProduct.setPerceivedColourMaster(resolvePerceivedColourMaster(new PerceivedColourMaster(null, dto.getPerceivedColourMasterName())));
        existingProduct.setDepartment(resolveDepartment(new Department(null, dto.getDepartmentName())));
        existingProduct.setIndex(resolveIndex(new Index(null, dto.getIndexName())));
        existingProduct.setIndexGroup(resolveIndexGroup(new IndexGroup(null, dto.getIndexGroupName())));
        existingProduct.setSection(resolveSection(new Section(null, dto.getSectionName())));
        existingProduct.setGarmentGroup(resolveGarmentGroup(new GarmentGroup(null, dto.getGarmentGroupName())));

        log.debug("💾 Saving updated product for ID: {}", id);
        Product updatedProduct = productRepository.save(existingProduct);
        
        // V2 Sync
        syncToSearch(updatedProduct);
        
        log.info("✅ Product updated and saved: {}", updatedProduct);
        return updatedProduct;
    }

    @Transactional
    public Product save(Product product) {
        log.debug("Resolving categories for product: {}", product.getProdName());
        product.setProductType(resolveProductType(product.getProductType()));
        product.setProductGroup(resolveProductGroup(product.getProductGroup()));
        product.setGraphicalAppearance(resolveGraphicalAppearance(product.getGraphicalAppearance()));
        product.setColourGroup(resolveColourGroup(product.getColourGroup()));
        product.setPerceivedColourValue(resolvePerceivedColourValue(product.getPerceivedColourValue()));
        product.setPerceivedColourMaster(resolvePerceivedColourMaster(product.getPerceivedColourMaster()));
        product.setDepartment(resolveDepartment(product.getDepartment()));
        product.setIndex(resolveIndex(product.getIndex()));
        product.setIndexGroup(resolveIndexGroup(product.getIndexGroup()));
        product.setSection(resolveSection(product.getSection()));
        product.setGarmentGroup(resolveGarmentGroup(product.getGarmentGroup()));
        log.debug("Categories resolved for product: {}", product.getProdName());
        Product saved = productRepository.save(product);
        syncToSearch(saved);
        return saved;
    }

    private void syncToSearch(Product product) {
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
            log.debug("Synced product id {} to ProductSearch (V2)", product.getProductId());
        } catch (Exception e) {
            log.error("Failed to sync product to search entity", e);
            // Non-blocking error for V1 flow? Or strictly transactional?
            // Ideally strictly transactional, but for now we log.
        }
    }

    private ProductType resolveProductType(ProductType incomingEntity) {
        log.debug("Resolving ProductType for name: {}", incomingEntity != null ? incomingEntity.getProductTypeName() : "null");
        if (incomingEntity == null || incomingEntity.getProductTypeName() == null) {
            log.warn("Incoming ProductType entity or name is null, returning null.");
            return null;
        }

        return productTypeRepository.findByProductTypeName(incomingEntity.getProductTypeName())
                .orElseGet(() -> {
                    try {
                        log.info("ProductType '{}' not found, attempting to create new one.", incomingEntity.getProductTypeName());
                        Integer newProductTypeNo = incomingEntity.getProductTypeNo();
                        if (newProductTypeNo == null) {
                            newProductTypeNo = productTypeRepository.findMaxProductTypeNo().orElse(0) + 1;
                            log.debug("Generated new ProductTypeNo: {}", newProductTypeNo);
                        }
                        incomingEntity.setProductTypeNo(newProductTypeNo);
                        ProductType savedProductType = productTypeRepository.save(incomingEntity);
                        log.info("Created and saved new ProductType: {}", savedProductType);
                        return savedProductType;
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        // Concurrent insertion happened, try to find it again
                        log.warn("Concurrent insertion of ProductType '{}' detected. Retrying to find existing entity.", incomingEntity.getProductTypeName());
                        return productTypeRepository.findByProductTypeName(incomingEntity.getProductTypeName())
                                .orElseThrow(() -> new IllegalStateException("Failed to find ProductType after concurrent insertion: " + incomingEntity.getProductTypeName(), e));
                    }
                });
    }

    private ProductGroup resolveProductGroup(ProductGroup entity) {
        log.debug("Resolving ProductGroup for name: {}", entity != null ? entity.getProductGroupName() : "null");
        if (entity == null || entity.getProductGroupName() == null) {
            log.warn("Incoming ProductGroup entity or name is null, returning null.");
            return null;
        }

        return productGroupRepository.findByProductGroupName(entity.getProductGroupName())
                .orElseGet(() -> {
                    try {
                        log.info("ProductGroup '{}' not found, attempting to create new one.", entity.getProductGroupName());
                        char newCode;
                        Optional<Character> maxCodeOptional = productGroupRepository.findTopByOrderByProductGroupCodeDesc();
                        if (maxCodeOptional.isEmpty()) {
                            newCode = 'A';
                        } else {
                            newCode = (char) (maxCodeOptional.get() + 1);
                        }
                        log.debug("Generated new ProductGroupCode: {}", newCode);
                        entity.setProductGroupCode(newCode);
                        ProductGroup savedProductGroup = productGroupRepository.save(entity);
                        log.info("Created and saved new ProductGroup: {}", savedProductGroup);
                        return savedProductGroup;
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        // Concurrent insertion happened, try to find it again
                        log.warn("Concurrent insertion of ProductGroup '{}' detected. Retrying to find existing entity.", entity.getProductGroupName());
                        return productGroupRepository.findByProductGroupName(entity.getProductGroupName())
                                .orElseThrow(() -> new IllegalStateException("Failed to find ProductGroup after concurrent insertion: " + entity.getProductGroupName(), e));
                    }
                });
    }

    private GraphicalAppearance resolveGraphicalAppearance(GraphicalAppearance incomingEntity) {
        log.debug("Resolving GraphicalAppearance for name: {}", incomingEntity != null ? incomingEntity.getGraphicalAppearanceName() : "null");
        if (incomingEntity == null || incomingEntity.getGraphicalAppearanceName() == null) {
            log.warn("Incoming GraphicalAppearance entity or name is null, returning null.");
            return null;
        }

        return graphicalAppearanceRepository.findByGraphicalAppearanceName(incomingEntity.getGraphicalAppearanceName())
                .orElseGet(() -> {
                    try {
                        log.info("GraphicalAppearance '{}' not found, creating new one.", incomingEntity.getGraphicalAppearanceName());
                        Integer newGraphicalAppearanceNo = incomingEntity.getGraphicalAppearanceNo();
                        if (newGraphicalAppearanceNo == null) {
                            newGraphicalAppearanceNo = graphicalAppearanceRepository.findMaxGraphicalAppearanceNo().orElse(0) + 1;
                            log.debug("Generated new GraphicalAppearanceNo: {}", newGraphicalAppearanceNo);
                        }
                        incomingEntity.setGraphicalAppearanceNo(newGraphicalAppearanceNo);
                        GraphicalAppearance savedGraphicalAppearance = graphicalAppearanceRepository.save(incomingEntity);
                        log.info("Created and saved new GraphicalAppearance: {}", savedGraphicalAppearance);
                        return savedGraphicalAppearance;
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.warn("Concurrent insertion of GraphicalAppearance '{}' detected. Retrying to find existing entity.", incomingEntity.getGraphicalAppearanceName());
                        return graphicalAppearanceRepository.findByGraphicalAppearanceName(incomingEntity.getGraphicalAppearanceName())
                                .orElseThrow(() -> new IllegalStateException("Failed to find GraphicalAppearance after concurrent insertion: " + incomingEntity.getGraphicalAppearanceName(), e));
                    }
                });
    }

    private ColourGroup resolveColourGroup(ColourGroup incomingEntity) {
        log.debug("Resolving ColourGroup for name: {}", incomingEntity != null ? incomingEntity.getColourGroupName() : "null");
        if (incomingEntity == null || incomingEntity.getColourGroupName() == null) {
            log.warn("Incoming ColourGroup entity or name is null, returning null.");
            return null;
        }

        return colourGroupRepository.findByColourGroupName(incomingEntity.getColourGroupName())
                .orElseGet(() -> {
                    try {
                        log.info("ColourGroup '{}' not found, creating new one.", incomingEntity.getColourGroupName());
                        Integer newColourGroupCode = incomingEntity.getColourGroupCode();
                        if (newColourGroupCode == null) {
                            newColourGroupCode = colourGroupRepository.findMaxColourGroupCode().orElse(0) + 1;
                            log.debug("Generated new ColourGroupCode: {}", newColourGroupCode);
                        }
                        incomingEntity.setColourGroupCode(newColourGroupCode);
                        ColourGroup savedColourGroup = colourGroupRepository.save(incomingEntity);
                        log.info("Created and saved new ColourGroup: {}", savedColourGroup);
                        return savedColourGroup;
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.warn("Concurrent insertion of ColourGroup '{}' detected. Retrying to find existing entity.", incomingEntity.getColourGroupName());
                        return colourGroupRepository.findByColourGroupName(incomingEntity.getColourGroupName())
                                .orElseThrow(() -> new IllegalStateException("Failed to find ColourGroup after concurrent insertion: " + incomingEntity.getColourGroupName(), e));
                    }
                });
    }

    private PerceivedColourValue resolvePerceivedColourValue(PerceivedColourValue incomingEntity) {
        log.debug("Resolving PerceivedColourValue for name: {}", incomingEntity != null ? incomingEntity.getPerceivedColourValueName() : "null");
        if (incomingEntity == null || incomingEntity.getPerceivedColourValueName() == null) {
            log.warn("Incoming PerceivedColourValue entity or name is null, returning null.");
            return null;
        }

        return perceivedColourValueRepository.findByPerceivedColourValueName(incomingEntity.getPerceivedColourValueName())
                .orElseGet(() -> {
                    try {
                        log.info("PerceivedColourValue '{}' not found, creating new one.", incomingEntity.getPerceivedColourValueName());
                        Integer newPerceivedColourValueId = incomingEntity.getPerceivedColourValueId();
                        if (newPerceivedColourValueId == null) {
                            newPerceivedColourValueId = perceivedColourValueRepository.findMaxPerceivedColourValueId().orElse(0) + 1;
                            log.debug("Generated new PerceivedColourValueId: {}", newPerceivedColourValueId);
                        }
                        incomingEntity.setPerceivedColourValueId(newPerceivedColourValueId);
                        PerceivedColourValue savedPerceivedColourValue = perceivedColourValueRepository.save(incomingEntity);
                        log.info("Created and saved new PerceivedColourValue: {}", savedPerceivedColourValue);
                        return savedPerceivedColourValue;
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.warn("Concurrent insertion of PerceivedColourValue '{}' detected. Retrying to find existing entity.", incomingEntity.getPerceivedColourValueName());
                        return perceivedColourValueRepository.findByPerceivedColourValueName(incomingEntity.getPerceivedColourValueName())
                                .orElseThrow(() -> new IllegalStateException("Failed to find PerceivedColourValue after concurrent insertion: " + incomingEntity.getPerceivedColourValueName(), e));
                    }
                });
    }

    private PerceivedColourMaster resolvePerceivedColourMaster(PerceivedColourMaster incomingEntity) {
        log.debug("Resolving PerceivedColourMaster for name: {}", incomingEntity != null ? incomingEntity.getPerceivedColourMasterName() : "null");
        if (incomingEntity == null || incomingEntity.getPerceivedColourMasterName() == null) {
            log.warn("Incoming PerceivedColourMaster entity or name is null, returning null.");
            return null;
        }

        return perceivedColourMasterRepository.findByPerceivedColourMasterName(incomingEntity.getPerceivedColourMasterName())
                .orElseGet(() -> {
                    try {
                        log.info("PerceivedColourMaster '{}' not found, creating new one.", incomingEntity.getPerceivedColourMasterName());
                        Integer newPerceivedColourMasterId = incomingEntity.getPerceivedColourMasterId();
                        if (newPerceivedColourMasterId == null) {
                            newPerceivedColourMasterId = perceivedColourMasterRepository.findMaxPerceivedColourMasterId().orElse(0) + 1;
                            log.debug("Generated new PerceivedColourMasterId: {}", newPerceivedColourMasterId);
                        }
                        incomingEntity.setPerceivedColourMasterId(newPerceivedColourMasterId);
                        PerceivedColourMaster savedPerceivedColourMaster = perceivedColourMasterRepository.save(incomingEntity);
                        log.info("Created and saved new PerceivedColourMaster: {}", savedPerceivedColourMaster);
                        return savedPerceivedColourMaster;
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.warn("Concurrent insertion of PerceivedColourMaster '{}' detected. Retrying to find existing entity.", incomingEntity.getPerceivedColourMasterName());
                        return perceivedColourMasterRepository.findByPerceivedColourMasterName(incomingEntity.getPerceivedColourMasterName())
                                .orElseThrow(() -> new IllegalStateException("Failed to find PerceivedColourMaster after concurrent insertion: " + incomingEntity.getPerceivedColourMasterName(), e));
                    }
                });
    }

    private Department resolveDepartment(Department incomingEntity) {
        log.debug("Resolving Department for name: {}", incomingEntity != null ? incomingEntity.getDepartmentName() : "null");
        if (incomingEntity == null || incomingEntity.getDepartmentName() == null) {
            log.warn("Incoming Department entity or name is null, returning null.");
            return null;
        }

        return departmentRepository.findByDepartmentName(incomingEntity.getDepartmentName())
                .orElseGet(() -> {
                    try {
                        log.info("Department '{}' not found, creating new one.", incomingEntity.getDepartmentName());
                        Integer newDepartmentNo = incomingEntity.getDepartmentNo();
                        if (newDepartmentNo == null) {
                            newDepartmentNo = departmentRepository.findMaxDepartmentNo().orElse(0) + 1;
                            log.debug("Generated new DepartmentNo: {}", newDepartmentNo);
                        }
                        incomingEntity.setDepartmentNo(newDepartmentNo);
                        Department savedDepartment = departmentRepository.save(incomingEntity);
                        log.info("Created and saved new Department: {}", savedDepartment);
                        return savedDepartment;
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.warn("Concurrent insertion of Department '{}' detected. Retrying to find existing entity.", incomingEntity.getDepartmentName());
                        return departmentRepository.findByDepartmentName(incomingEntity.getDepartmentName())
                                .orElseThrow(() -> new IllegalStateException("Failed to find Department after concurrent insertion: " + incomingEntity.getDepartmentName(), e));
                    }
                });
    }

    private Index resolveIndex(Index incomingEntity) {
        log.debug("Resolving Index for name: {}", incomingEntity != null ? incomingEntity.getIndexName() : "null");
        if (incomingEntity == null || incomingEntity.getIndexName() == null) {
            log.warn("Incoming Index entity or name is null, returning null.");
            return null;
        }

        return indexRepository.findByIndexName(incomingEntity.getIndexName())
                .orElseGet(() -> {
                    try {
                        log.info("Index '{}' not found, creating new one.", incomingEntity.getIndexName());
                        char newCode;
                        Optional<Character> maxCodeOptional = indexRepository.findTopByOrderByIndexCodeDesc();
                        if (maxCodeOptional.isEmpty()) {
                            newCode = 'A';
                        } else {
                            newCode = (char) (maxCodeOptional.get() + 1);
                        }
                        log.debug("Generated new IndexCode: {}", newCode);
                        incomingEntity.setIndexCode(newCode);
                        Index savedIndex = indexRepository.save(incomingEntity);
                        log.info("Created and saved new Index: {}", savedIndex);
                        return savedIndex;
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.warn("Concurrent insertion of Index '{}' detected. Retrying to find existing entity.", incomingEntity.getIndexName());
                        return indexRepository.findByIndexName(incomingEntity.getIndexName())
                                .orElseThrow(() -> new IllegalStateException("Failed to find Index after concurrent insertion: " + incomingEntity.getIndexName(), e));
                    }
                });
    }

    private IndexGroup resolveIndexGroup(IndexGroup incomingEntity) {
        log.debug("Resolving IndexGroup for name: {}", incomingEntity != null ? incomingEntity.getIndexGroupName() : "null");
        if (incomingEntity == null || incomingEntity.getIndexGroupName() == null) {
            log.warn("Incoming IndexGroup entity or name is null, returning null.");
            return null;
        }

        return indexGroupRepository.findByIndexGroupName(incomingEntity.getIndexGroupName())
                .orElseGet(() -> {
                    try {
                        log.info("IndexGroup '{}' not found, creating new one.", incomingEntity.getIndexGroupName());
                        Integer newIndexGroupNo = incomingEntity.getIndexGroupNo();
                        if (newIndexGroupNo == null) {
                            newIndexGroupNo = indexGroupRepository.findMaxIndexGroupNo().orElse(0) + 1;
                            log.debug("Generated new IndexGroupNo: {}", newIndexGroupNo);
                        }
                        incomingEntity.setIndexGroupNo(newIndexGroupNo);
                        IndexGroup savedIndexGroup = indexGroupRepository.save(incomingEntity);
                        log.info("Created and saved new IndexGroup: {}", savedIndexGroup);
                        return savedIndexGroup;
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.warn("Concurrent insertion of IndexGroup '{}' detected. Retrying to find existing entity.", incomingEntity.getIndexGroupName());
                        return indexGroupRepository.findByIndexGroupName(incomingEntity.getIndexGroupName())
                                .orElseThrow(() -> new IllegalStateException("Failed to find IndexGroup after concurrent insertion: " + incomingEntity.getIndexGroupName(), e));
                    }
                });
    }

    private Section resolveSection(Section incomingEntity) {
        log.debug("Resolving Section for name: {}", incomingEntity != null ? incomingEntity.getSectionName() : "null");
        if (incomingEntity == null || incomingEntity.getSectionName() == null) {
            log.warn("Incoming Section entity or name is null, returning null.");
            return null;
        }

        return sectionRepository.findBySectionName(incomingEntity.getSectionName())
                .orElseGet(() -> {
                    try {
                        log.info("Section '{}' not found, creating new one.", incomingEntity.getSectionName());
                        Integer newSectionNo = incomingEntity.getSectionNo();
                        if (newSectionNo == null) {
                            newSectionNo = sectionRepository.findMaxSectionNo().orElse(0) + 1;
                            log.debug("Generated new SectionNo: {}", newSectionNo);
                        }
                        incomingEntity.setSectionNo(newSectionNo);
                        Section savedSection = sectionRepository.save(incomingEntity);
                        log.info("Created and saved new Section: {}", savedSection);
                        return savedSection;
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.warn("Concurrent insertion of Section '{}' detected. Retrying to find existing entity.", incomingEntity.getSectionName());
                        return sectionRepository.findBySectionName(incomingEntity.getSectionName())
                                .orElseThrow(() -> new IllegalStateException("Failed to find Section after concurrent insertion: " + incomingEntity.getSectionName(), e));
                    }
                });
    }

    private GarmentGroup resolveGarmentGroup(GarmentGroup incomingEntity) {
        log.debug("Resolving GarmentGroup for name: {}", incomingEntity != null ? incomingEntity.getGarmentGroupName() : "null");
        if (incomingEntity == null || incomingEntity.getGarmentGroupName() == null) {
            log.warn("Incoming GarmentGroup entity or name is null, returning null.");
            return null;
        }

        return garmentGroupRepository.findByGarmentGroupName(incomingEntity.getGarmentGroupName())
                .orElseGet(() -> {
                    try {
                        log.info("GarmentGroup '{}' not found, creating new one.", incomingEntity.getGarmentGroupName());
                        Integer newGarmentGroupNo = incomingEntity.getGarmentGroupNo();
                        if (newGarmentGroupNo == null) {
                            newGarmentGroupNo = garmentGroupRepository.findMaxGarmentGroupNo().orElse(0) + 1;
                            log.debug("Generated new GarmentGroupNo: {}", newGarmentGroupNo);
                        }
                        incomingEntity.setGarmentGroupNo(newGarmentGroupNo);
                        GarmentGroup savedGarmentGroup = garmentGroupRepository.save(incomingEntity);
                        log.info("Created and saved new GarmentGroup: {}", savedGarmentGroup);
                        return savedGarmentGroup;
                    } catch (org.springframework.dao.DataIntegrityViolationException e) {
                        log.warn("Concurrent insertion of GarmentGroup '{}' detected. Retrying to find existing entity.", incomingEntity.getGarmentGroupName());
                        return garmentGroupRepository.findByGarmentGroupName(incomingEntity.getGarmentGroupName())
                                .orElseThrow(() -> new IllegalStateException("Failed to find GarmentGroup after concurrent insertion: " + incomingEntity.getGarmentGroupName(), e));
                    }
                });
    }

    @Transactional
    public void deleteById(Integer id) {
        productRepository.deleteById(id);
        productSearchRepository.deleteById(id);
    }

    public List<Product> search(String keyword) {
        return productRepository.findByProdNameContainingIgnoreCase(keyword);
    }

    public List<ProductSearchResponseDto> searchByName(String keyword) {
        return productRepository.findTop20ByProdNameContainingIgnoreCase(keyword).stream()
                .map(product -> new ProductSearchResponseDto(product.getProductId(), product.getProdName()))
                .collect(Collectors.toList());
    }

    public List<String> searchNamesV1(String keyword) {
        return productRepository.findTop20ByProdNameContainingIgnoreCase(keyword).stream()
                .map(Product::getProdName)
                .collect(Collectors.toList());
    }

    // V2 Search Methods
    public Optional<ProductSearch> findByIdV2(Integer id) {
        // Retrieves directly from the denormalized table (No Joins)
        return productSearchRepository.findById(id);
    }

    public List<ProductSearch> searchV2(String keyword) {
        // Use the index-friendly prefix search first, or fallback to containing
        // For demonstration, let's assume we want "Containing" but on the denormalized table
        // Note: Standard B-Tree index on 'prodName' optimizes 'Starts With' (prefix), not 'Containing' (infix).
        // However, avoiding joins makes this faster regardless.
        return productSearchRepository.findTop20ByProdNameContainingIgnoreCase(keyword);
    }

    public List<ProductSearchResponseDto> searchByNameV2(String keyword) {
        return productSearchRepository.findTop20ByProdNameContainingIgnoreCase(keyword).stream()
                .map(product -> new ProductSearchResponseDto(product.getProductId(), product.getProdName()))
                .collect(Collectors.toList());
    }

    public List<String> searchNamesV2(String keyword) {
        // 1. Try Fast Prefix Search First ("Item%")
        List<String> results = productSearchRepository.findTop20ByProdNameStartingWithIgnoreCase(keyword).stream()
                .map(ProductSearch::getProdName)
                .collect(Collectors.toList());

        // 2. If results are insufficient (e.g., < 5), fallback/append Contains Search ("%Item%")
        if (results.size() < 5) {
            List<String> fallbackResults = productSearchRepository.findTop20ByProdNameContainingIgnoreCase(keyword).stream()
                    .map(ProductSearch::getProdName)
                    .filter(name -> !results.contains(name)) // Avoid duplicates
                    .collect(Collectors.toList());
            
            results.addAll(fallbackResults);
        }
        
        return results.stream().limit(20).collect(Collectors.toList());
    }

    public List<String> searchNamesV2WithCategory(String keyword, String productTypeName) {
        return productSearchRepository.findByProductTypeNameAndProdNameContainingIgnoreCase(productTypeName, keyword).stream()
                .map(ProductSearch::getProdName)
                .limit(20) // Limit consistency
                .collect(Collectors.toList());
    }

    public List<String> searchNamesV2WithFilters(String keyword, String productType, String department, String productGroup, String section) {
        return productSearchRepository.searchByMultipleCategories(keyword, productType, department, productGroup, section).stream()
                .map(ProductSearch::getProdName)
                .limit(20)
                .collect(Collectors.toList());
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void syncAllProductData() {
        if (productSearchRepository.count() == 0) {
            log.info("🚀 Starting initial data migration for ProductSearch (V2)...");
            List<Product> allProducts = productRepository.findAll();
            int count = 0;
            for (Product product : allProducts) {
                syncToSearch(product);
                count++;
            }
            log.info("✅ Migration complete. Synced {} products to ProductSearch.", count);
        } else {
            log.info("👌 ProductSearch data already exists. Skipping migration.");
        }
    }
}