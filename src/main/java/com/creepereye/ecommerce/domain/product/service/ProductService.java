package com.creepereye.ecommerce.domain.product.service;


import com.creepereye.ecommerce.domain.product.dto.ProductCreateRequestDto;
import com.creepereye.ecommerce.domain.product.dto.ProductSearchResponseDto;
import com.creepereye.ecommerce.domain.product.dto.ProductUpdateRequestDto;
import com.creepereye.ecommerce.domain.product.entity.*;
import com.creepereye.ecommerce.domain.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
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

        return save(product);
    }

    @Transactional
    public Product updateProduct(Integer id, ProductUpdateRequestDto dto) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

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

        return productRepository.save(existingProduct);
    }

    @Transactional
    public Product save(Product product) {
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

        return productRepository.save(product);
    }

    private ProductType resolveProductType(ProductType incomingEntity) {
        if (incomingEntity == null || incomingEntity.getProductTypeName() == null) return null;

        return productTypeRepository.findByProductTypeName(incomingEntity.getProductTypeName())
                .orElseGet(() -> {
                    // Not found by name, create a new one
                    Integer newProductTypeNo = incomingEntity.getProductTypeNo();
                    if (newProductTypeNo == null) {
                        newProductTypeNo = productTypeRepository.findMaxProductTypeNo().orElse(0) + 1;
                    }
                    incomingEntity.setProductTypeNo(newProductTypeNo);
                    return productTypeRepository.save(incomingEntity);
                });
    }

    private ProductGroup resolveProductGroup(ProductGroup entity) {
        if (entity == null || entity.getProductGroupName() == null) return null;

        return productGroupRepository.findByProductGroupName(entity.getProductGroupName())
                .orElseGet(() -> {
                    int newCodeInt = productGroupRepository.findMaxProductGroupCodeAsInt().orElse(0) + 1;
                    char newCode = (char) ('A' + newCodeInt - 1);
                    entity.setProductGroupCode(newCode);
                    return productGroupRepository.save(entity);
                });
    }

    private GraphicalAppearance resolveGraphicalAppearance(GraphicalAppearance incomingEntity) {
        if (incomingEntity == null || incomingEntity.getGraphicalAppearanceName() == null) return null;

        return graphicalAppearanceRepository.findByGraphicalAppearanceName(incomingEntity.getGraphicalAppearanceName())
                .orElseGet(() -> {
                    // Not found by name, create a new one
                    Integer newGraphicalAppearanceNo = incomingEntity.getGraphicalAppearanceNo();
                    if (newGraphicalAppearanceNo == null) {
                        newGraphicalAppearanceNo = graphicalAppearanceRepository.findMaxGraphicalAppearanceNo().orElse(0) + 1;
                    }
                    incomingEntity.setGraphicalAppearanceNo(newGraphicalAppearanceNo);
                    return graphicalAppearanceRepository.save(incomingEntity);
                });
    }

    private ColourGroup resolveColourGroup(ColourGroup incomingEntity) {
        if (incomingEntity == null || incomingEntity.getColourGroupName() == null) return null;

        return colourGroupRepository.findByColourGroupName(incomingEntity.getColourGroupName())
                .orElseGet(() -> {
                    // Not found by name, create a new one
                    Integer newColourGroupCode = incomingEntity.getColourGroupCode();
                    if (newColourGroupCode == null) {
                        newColourGroupCode = colourGroupRepository.findMaxColourGroupCode().orElse(0) + 1;
                    }
                    incomingEntity.setColourGroupCode(newColourGroupCode);
                    return colourGroupRepository.save(incomingEntity);
                });
    }

    private PerceivedColourValue resolvePerceivedColourValue(PerceivedColourValue incomingEntity) {
        if (incomingEntity == null || incomingEntity.getPerceivedColourValueName() == null) return null;

        return perceivedColourValueRepository.findByPerceivedColourValueName(incomingEntity.getPerceivedColourValueName())
                .orElseGet(() -> {
                    // Not found by name, create a new one
                    Integer newPerceivedColourValueId = incomingEntity.getPerceivedColourValueId();
                    if (newPerceivedColourValueId == null) {
                        newPerceivedColourValueId = perceivedColourValueRepository.findMaxPerceivedColourValueId().orElse(0) + 1;
                    }
                    incomingEntity.setPerceivedColourValueId(newPerceivedColourValueId);
                    return perceivedColourValueRepository.save(incomingEntity);
                });
    }

    private PerceivedColourMaster resolvePerceivedColourMaster(PerceivedColourMaster incomingEntity) {
        if (incomingEntity == null || incomingEntity.getPerceivedColourMasterName() == null) return null;

        return perceivedColourMasterRepository.findByPerceivedColourMasterName(incomingEntity.getPerceivedColourMasterName())
                .orElseGet(() -> {
                    // Not found by name, create a new one
                    Integer newPerceivedColourMasterId = incomingEntity.getPerceivedColourMasterId();
                    if (newPerceivedColourMasterId == null) {
                        newPerceivedColourMasterId = perceivedColourMasterRepository.findMaxPerceivedColourMasterId().orElse(0) + 1;
                    }
                    incomingEntity.setPerceivedColourMasterId(newPerceivedColourMasterId);
                    return perceivedColourMasterRepository.save(incomingEntity);
                });
    }

    private Department resolveDepartment(Department incomingEntity) {
        if (incomingEntity == null || incomingEntity.getDepartmentName() == null) return null;

        return departmentRepository.findByDepartmentName(incomingEntity.getDepartmentName())
                .orElseGet(() -> {
                    // Not found by name, create a new one
                    Integer newDepartmentNo = incomingEntity.getDepartmentNo();
                    if (newDepartmentNo == null) {
                        newDepartmentNo = departmentRepository.findMaxDepartmentNo().orElse(0) + 1;
                    }
                    incomingEntity.setDepartmentNo(newDepartmentNo);
                    return departmentRepository.save(incomingEntity);
                });
    }

    private Index resolveIndex(Index incomingEntity) {
        if (incomingEntity == null || incomingEntity.getIndexName() == null) return null;

        return indexRepository.findByIndexName(incomingEntity.getIndexName())
                .orElseGet(() -> {
                    // Not found by name, create a new one if indexCode is provided
                    if (incomingEntity.getIndexCode() == null) {
                        // Cannot create a new Index entity without an indexCode
                        return null;
                    }
                    return indexRepository.save(incomingEntity);
                });
    }

    private IndexGroup resolveIndexGroup(IndexGroup incomingEntity) {
        if (incomingEntity == null || incomingEntity.getIndexGroupName() == null) return null;

        return indexGroupRepository.findByIndexGroupName(incomingEntity.getIndexGroupName())
                .orElseGet(() -> {
                    // Not found by name, create a new one
                    Integer newIndexGroupNo = incomingEntity.getIndexGroupNo();
                    if (newIndexGroupNo == null) {
                        newIndexGroupNo = indexGroupRepository.findMaxIndexGroupNo().orElse(0) + 1;
                    }
                    incomingEntity.setIndexGroupNo(newIndexGroupNo);
                    return indexGroupRepository.save(incomingEntity);
                });
    }

    private Section resolveSection(Section incomingEntity) {
        if (incomingEntity == null || incomingEntity.getSectionName() == null) return null;

        return sectionRepository.findBySectionName(incomingEntity.getSectionName())
                .orElseGet(() -> {
                    // Not found by name, create a new one
                    Integer newSectionNo = incomingEntity.getSectionNo();
                    if (newSectionNo == null) {
                        newSectionNo = sectionRepository.findMaxSectionNo().orElse(0) + 1;
                    }
                    incomingEntity.setSectionNo(newSectionNo);
                    return sectionRepository.save(incomingEntity);
                });
    }

    private GarmentGroup resolveGarmentGroup(GarmentGroup incomingEntity) {
        if (incomingEntity == null || incomingEntity.getGarmentGroupName() == null) return null;

        return garmentGroupRepository.findByGarmentGroupName(incomingEntity.getGarmentGroupName())
                .orElseGet(() -> {
                    // Not found by name, create a new one
                    Integer newGarmentGroupNo = incomingEntity.getGarmentGroupNo();
                    if (newGarmentGroupNo == null) {
                        newGarmentGroupNo = garmentGroupRepository.findMaxGarmentGroupNo().orElse(0) + 1;
                    }
                    incomingEntity.setGarmentGroupNo(newGarmentGroupNo);
                    return garmentGroupRepository.save(incomingEntity);
                });
    }

    @Transactional
    public void deleteById(Integer id) {
        productRepository.deleteById(id);
    }

    public List<Product> search(String keyword) {
        return productRepository.findByProdNameContainingIgnoreCase(keyword);
    }

    public List<ProductSearchResponseDto> searchByName(String keyword) {
        return productRepository.findTop20ByProdNameContainingIgnoreCase(keyword).stream()
                .map(product -> new ProductSearchResponseDto(product.getProductId(), product.getProdName()))
                .collect(Collectors.toList());
    }
}