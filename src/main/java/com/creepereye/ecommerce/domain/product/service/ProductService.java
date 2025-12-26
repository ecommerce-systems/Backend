package com.creepereye.ecommerce.domain.product.service;


import com.creepereye.ecommerce.domain.product.entity.*;
import com.creepereye.ecommerce.domain.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


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

    private ProductType resolveProductType(ProductType entity) {
        if (entity == null) return null;
        if (entity.getProductTypeNo() != null) return entity;
        if (entity.getProductTypeName() == null) return null;

        return productTypeRepository.findByProductTypeName(entity.getProductTypeName())
                .orElseGet(() -> {
                    Integer maxId = productTypeRepository.findMaxProductTypeNo().orElse(0);
                    entity.setProductTypeNo(maxId + 1);
                    return productTypeRepository.save(entity);
                });
    }

    private ProductGroup resolveProductGroup(ProductGroup entity) {
        if (entity == null || entity.getProductGroupName() == null) return null;
        return productGroupRepository.findById(entity.getProductGroupName())
                .orElseGet(() -> productGroupRepository.save(entity));
    }

    private GraphicalAppearance resolveGraphicalAppearance(GraphicalAppearance entity) {
        if (entity == null) return null;
        if (entity.getGraphicalAppearanceNo() != null) return entity;
        if (entity.getGraphicalAppearanceName() == null) return null;

        return graphicalAppearanceRepository.findByGraphicalAppearanceName(entity.getGraphicalAppearanceName())
                .orElseGet(() -> {
                    Integer maxId = graphicalAppearanceRepository.findMaxGraphicalAppearanceNo().orElse(0);
                    entity.setGraphicalAppearanceNo(maxId + 1);
                    return graphicalAppearanceRepository.save(entity);
                });
    }

    private ColourGroup resolveColourGroup(ColourGroup entity) {
        if (entity == null) return null;
        if (entity.getColourGroupCode() != null) return entity;
        if (entity.getColourGroupName() == null) return null;

        return colourGroupRepository.findByColourGroupName(entity.getColourGroupName())
                .orElseGet(() -> {
                    Integer maxId = colourGroupRepository.findMaxColourGroupCode().orElse(0);
                    entity.setColourGroupCode(maxId + 1);
                    return colourGroupRepository.save(entity);
                });
    }

    private PerceivedColourValue resolvePerceivedColourValue(PerceivedColourValue entity) {
        if (entity == null) return null;
        if (entity.getPerceivedColourValueId() != null) return entity;
        if (entity.getPerceivedColourValueName() == null) return null;

        return perceivedColourValueRepository.findByPerceivedColourValueName(entity.getPerceivedColourValueName())
                .orElseGet(() -> {
                    Integer maxId = perceivedColourValueRepository.findMaxPerceivedColourValueId().orElse(0);
                    entity.setPerceivedColourValueId(maxId + 1);
                    return perceivedColourValueRepository.save(entity);
                });
    }

    private PerceivedColourMaster resolvePerceivedColourMaster(PerceivedColourMaster entity) {
        if (entity == null) return null;
        if (entity.getPerceivedColourMasterId() != null) return entity;
        if (entity.getPerceivedColourMasterName() == null) return null;

        return perceivedColourMasterRepository.findByPerceivedColourMasterName(entity.getPerceivedColourMasterName())
                .orElseGet(() -> {
                    Integer maxId = perceivedColourMasterRepository.findMaxPerceivedColourMasterId().orElse(0);
                    entity.setPerceivedColourMasterId(maxId + 1);
                    return perceivedColourMasterRepository.save(entity);
                });
    }

    private Department resolveDepartment(Department entity) {
        if (entity == null) return null;
        if (entity.getDepartmentNo() != null) return entity;
        if (entity.getDepartmentName() == null) return null;

        return departmentRepository.findByDepartmentName(entity.getDepartmentName())
                .orElseGet(() -> {
                    Integer maxId = departmentRepository.findMaxDepartmentNo().orElse(0);
                    entity.setDepartmentNo(maxId + 1);
                    return departmentRepository.save(entity);
                });
    }

    private Index resolveIndex(Index entity) {
        // ID is Character, assuming no auto-creation for this one.
        if (entity == null || entity.getIndexCode() == null) return null;
        return indexRepository.findById(String.valueOf(entity.getIndexCode())).orElse(null);
    }

    private IndexGroup resolveIndexGroup(IndexGroup entity) {
        if (entity == null) return null;
        if (entity.getIndexGroupNo() != null) return entity;
        if (entity.getIndexGroupName() == null) return null;

        return indexGroupRepository.findByIndexGroupName(entity.getIndexGroupName())
                .orElseGet(() -> {
                    Integer maxId = indexGroupRepository.findMaxIndexGroupNo().orElse(0);
                    entity.setIndexGroupNo(maxId + 1);
                    return indexGroupRepository.save(entity);
                });
    }

    private Section resolveSection(Section entity) {
        if (entity == null) return null;
        if (entity.getSectionNo() != null) return entity;
        if (entity.getSectionName() == null) return null;

        return sectionRepository.findBySectionName(entity.getSectionName())
                .orElseGet(() -> {
                    Integer maxId = sectionRepository.findMaxSectionNo().orElse(0);
                    entity.setSectionNo(maxId + 1);
                    return sectionRepository.save(entity);
                });
    }

    private GarmentGroup resolveGarmentGroup(GarmentGroup entity) {
        if (entity == null) return null;
        if (entity.getGarmentGroupNo() != null) return entity;
        if (entity.getGarmentGroupName() == null) return null;

        return garmentGroupRepository.findByGarmentGroupName(entity.getGarmentGroupName())
                .orElseGet(() -> {
                    Integer maxId = garmentGroupRepository.findMaxGarmentGroupNo().orElse(0);
                    entity.setGarmentGroupNo(maxId + 1);
                    return garmentGroupRepository.save(entity);
                });
    }

    @Transactional
    public void deleteById(Integer id) {
        productRepository.deleteById(id);
    }

    public List<Product> search(String keyword) {
        Page<Product> page = productRepository.findByProdNameContainingIgnoreCase(keyword, PageRequest.of(0, 10));
        return page.getContent();
    }
}