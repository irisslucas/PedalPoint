package tqs.ua.pedalpoint.backend.hub;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HubService {

    private final HubRepository hubRepository;

    public HubService(HubRepository hubRepository) {
        this.hubRepository = hubRepository;
    }

    @Transactional(readOnly = true)
    public List<HubDto> findAll() {
        return hubRepository.findAllByOrderByNameAsc().stream()
                .map(HubDto::from)
                .toList();
    }
}
