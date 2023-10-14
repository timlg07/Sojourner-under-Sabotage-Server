package de.tim_greller.susserver.service.execution;

import java.util.List;
import java.util.Optional;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import com.github.difflib.patch.PatchFailedException;
import de.tim_greller.susserver.dto.CutSourceDTO;
import de.tim_greller.susserver.persistence.entity.ComponentEntity;
import de.tim_greller.susserver.persistence.keys.ComponentKey;
import de.tim_greller.susserver.persistence.repository.ComponentRepository;
import de.tim_greller.susserver.persistence.repository.CutRepository;
import de.tim_greller.susserver.persistence.repository.PatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CutService {

    private final CutRepository cutRepository;
    private final ComponentRepository componentRepository;
    private final PatchRepository patchRepository;

    @Autowired
    public CutService(CutRepository cutRepository, ComponentRepository componentRepository,
                      PatchRepository patchRepository) {
        this.cutRepository = cutRepository;
        this.componentRepository = componentRepository;
        this.patchRepository = patchRepository;
    }

    public Optional<CutSourceDTO> getCutForComponent(String componentName) {
        final ComponentEntity component = componentRepository.findById(componentName).orElseThrow();
        return cutRepository.findById(new ComponentKey(component)).map(CutSourceDTO::fromCutEntity);
    }

    private List<String> srcAsList(CutSourceDTO cut) {
        return List.of(cut.getSourceCode().split("\n"));
    }

    private String srcAsString(List<String> list) {
        return String.join("\n", list);
    }

    public CutSourceDTO applyPatchToCut(CutSourceDTO cut, Patch<String> patch) throws PatchFailedException {
        var newCut = new CutSourceDTO(cut);
        var oldSrc = srcAsList(cut);
        var newSrc = srcAsString(DiffUtils.patch(oldSrc, patch));
        newCut.setSourceCode(newSrc);
        return newCut;
    }

    public Patch<String> createPatch(CutSourceDTO cut, CutSourceDTO newCut) {
        return DiffUtils.diff(srcAsList(cut), srcAsList(newCut));
    }

    public void storePatch(String componentName, String newSource) {
        var cut = getCutForComponent(componentName).orElseThrow();
        var newCut = new CutSourceDTO(cut);
        newCut.setSourceCode(newSource);
        var patch = createPatch(cut, newCut);
        var component = componentRepository.findById(componentName).orElseThrow();
        System.out.println(patch.toString());
        //patchRepository.save(new PatchEntity(patch, new ComponentForeignKey(component)));
    }

}
