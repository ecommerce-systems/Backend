package com.creepereye.ecommerce.domain.product.service;

import com.creepereye.ecommerce.domain.product.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

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
    private CategoryService categoryService;

    @Test
    @DisplayName("findAll methods should call corresponding repository")
    void findAll_shouldCallRepositories() {
        categoryService.findAllColourGroups();
        verify(colourGroupRepository).findAll();

        categoryService.findAllDepartments();
        verify(departmentRepository).findAll();
        
        categoryService.findAllGarmentGroups();
        verify(garmentGroupRepository).findAll();
        
        categoryService.findAllGraphicalAppearances();
        verify(graphicalAppearanceRepository).findAll();

        categoryService.findAllIndexGroups();
        verify(indexGroupRepository).findAll();

        categoryService.findAllIndices();
        verify(indexRepository).findAll();

        categoryService.findAllPerceivedColourMasters();
        verify(perceivedColourMasterRepository).findAll();

        categoryService.findAllPerceivedColourValues();
        verify(perceivedColourValueRepository).findAll();

        categoryService.findAllProductGroups();
        verify(productGroupRepository).findAll();

        categoryService.findAllProductTypes();
        verify(productTypeRepository).findAll();

        categoryService.findAllSections();
        verify(sectionRepository).findAll();
    }
}
