package de.tim_greller.susserver.service.execution;

import java.util.Optional;

import de.tim_greller.susserver.dto.CutSourceDTO;
import de.tim_greller.susserver.persistence.repository.CutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CutService {

    private final CutRepository cutRepository;

    @Autowired
    public CutService(CutRepository cutRepository) {
        this.cutRepository = cutRepository;
    }

    public Optional<CutSourceDTO> getCutForComponent(String componentName) {
       return cutRepository.findById(componentName).map(CutSourceDTO::fromCutEntity);
    }

}
