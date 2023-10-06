package de.tim_greller.susserver.service.execution;

import java.util.Optional;

import de.tim_greller.susserver.dto.CutSourceDTO;
import de.tim_greller.susserver.persistence.entity.ComponentEntity;
import de.tim_greller.susserver.persistence.keys.ComponentKey;
import de.tim_greller.susserver.persistence.repository.ComponentRepository;
import de.tim_greller.susserver.persistence.repository.CutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CutService {

    private final CutRepository cutRepository;
    private final ComponentRepository componentRepository;

    @Autowired
    public CutService(CutRepository cutRepository, ComponentRepository componentRepository) {
        this.cutRepository = cutRepository;
        this.componentRepository = componentRepository;
    }

    public Optional<CutSourceDTO> getCutForComponent(String componentName) {
        final ComponentEntity component = componentRepository.findById(componentName).orElseThrow();
        return cutRepository.findById(new ComponentKey(component)).map(CutSourceDTO::fromCutEntity);
    }

}
