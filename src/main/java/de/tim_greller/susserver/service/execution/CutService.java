package de.tim_greller.susserver.service.execution;

import java.util.Optional;

import de.tim_greller.susserver.dto.CutSourceDTO;
import de.tim_greller.susserver.persistence.entity.ActivePatchEntity;
import de.tim_greller.susserver.persistence.entity.ComponentEntity;
import de.tim_greller.susserver.persistence.entity.PatchEntity;
import de.tim_greller.susserver.persistence.keys.ComponentKey;
import de.tim_greller.susserver.persistence.keys.ComponentStageKey;
import de.tim_greller.susserver.persistence.repository.ActivePatchRepository;
import de.tim_greller.susserver.persistence.repository.ComponentRepository;
import de.tim_greller.susserver.persistence.repository.CutRepository;
import de.tim_greller.susserver.persistence.repository.PatchRepository;
import de.tim_greller.susserver.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CutService {

    private final PatchService patchService;
    private final CutRepository cutRepository;
    private final ComponentRepository componentRepository;
    private final PatchRepository patchRepository;
    private final ActivePatchRepository activePatchRepository;
    private final UserService userService;

    public Optional<CutSourceDTO> getOriginalCutForComponent(String componentName) {
        final ComponentEntity component = componentRepository.findById(componentName).orElseThrow();
        return cutRepository.findById(new ComponentKey(component)).map(CutSourceDTO::fromCutEntity);
    }

    public Optional<CutSourceDTO> getCurrentCutForComponent(String componentName) {
        Optional<CutSourceDTO> originalCut = getOriginalCutForComponent(componentName);
        Optional<ActivePatchEntity> activePatch = activePatchRepository.findByKey(componentName, userService.requireCurrentUserId());

        if (originalCut.isEmpty()) {
            return Optional.empty();
        }

        final CutSourceDTO cut = originalCut.get();
        final CutSourceDTO patchedCut = activePatch
                .map(ActivePatchEntity::getPatch)
                .map(patch -> applyPatch(cut, patch))
                .orElse(cut);
        return Optional.of(patchedCut);
    }

    public void storePatch(String componentName, int stage,  String newSource) {
        ComponentEntity component = componentRepository.findById(componentName).orElseThrow();
        CutSourceDTO cut = getOriginalCutForComponent(componentName).orElseThrow();
        String patch = patchService.createPatch(cut.getSourceCode(), newSource);
        patchRepository.save(new PatchEntity(patch, new ComponentStageKey(component, stage)));
    }

    private CutSourceDTO applyPatch(CutSourceDTO cut, PatchEntity patch) {
        String newSource = patchService.applyPatch(cut.getSourceCode(), patch.getPatch());
        cut.setSourceCode(newSource);
        return cut;
    }

}
