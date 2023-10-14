package de.tim_greller.susserver.service.execution;

import java.util.Optional;

import de.tim_greller.susserver.dto.CutSourceDTO;
import de.tim_greller.susserver.persistence.entity.ComponentEntity;
import de.tim_greller.susserver.persistence.entity.PatchEntity;
import de.tim_greller.susserver.persistence.keys.ComponentForeignKey;
import de.tim_greller.susserver.persistence.keys.ComponentKey;
import de.tim_greller.susserver.persistence.repository.ComponentRepository;
import de.tim_greller.susserver.persistence.repository.CutRepository;
import de.tim_greller.susserver.persistence.repository.PatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CutService {

    private final PatchService patchService;
    private final CutRepository cutRepository;
    private final ComponentRepository componentRepository;
    private final PatchRepository patchRepository;

    @Autowired
    public CutService(PatchService patchService, CutRepository cutRepository, ComponentRepository componentRepository,
                      PatchRepository patchRepository) {
        this.patchService = patchService;
        this.cutRepository = cutRepository;
        this.componentRepository = componentRepository;
        this.patchRepository = patchRepository;
    }

    public Optional<CutSourceDTO> getCutForComponent(String componentName) {
        final ComponentEntity component = componentRepository.findById(componentName).orElseThrow();
        return cutRepository.findById(new ComponentKey(component)).map(CutSourceDTO::fromCutEntity);
    }

    public void storePatch(String componentName, String newSource) {
        ComponentEntity component = componentRepository.findById(componentName).orElseThrow();
        CutSourceDTO cut = getCutForComponent(componentName).orElseThrow();
        String patch = patchService.createPatch(cut.getSourceCode(), newSource);
        patchRepository.save(new PatchEntity(patch, new ComponentForeignKey(component)));
    }

}
