package com.creepereye.ecommerce.domain.product.service;


import com.creepereye.ecommerce.domain.product.entity.*;
import com.creepereye.ecommerce.domain.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
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

    public List<ColourGroup> findAllColourGroups() {
        return colourGroupRepository.findAll();
    }

    public List<Department> findAllDepartments() {
        return departmentRepository.findAll();
    }

    public List<GarmentGroup> findAllGarmentGroups() {
        return garmentGroupRepository.findAll();
    }

    public List<GraphicalAppearance> findAllGraphicalAppearances() {
        return graphicalAppearanceRepository.findAll();
    }

    public List<IndexGroup> findAllIndexGroups() {
        return indexGroupRepository.findAll();
    }

    public List<Index> findAllIndices() {
        return indexRepository.findAll();
    }

    public List<PerceivedColourMaster> findAllPerceivedColourMasters() {
        return perceivedColourMasterRepository.findAll();
    }

    public List<PerceivedColourValue> findAllPerceivedColourValues() {
        return perceivedColourValueRepository.findAll();
    }

    public List<ProductGroup> findAllProductGroups() {
        return productGroupRepository.findAll();
    }

    public List<ProductType> findAllProductTypes() {
        return productTypeRepository.findAll();
    }

    public List<Section> findAllSections() {
        return sectionRepository.findAll();
    }
}
