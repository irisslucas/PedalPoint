package tqs.ua.pedalpoint.backend.hub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit test: no Spring context, no database. */
@ExtendWith(MockitoExtension.class)
class HubServiceTest {

    @Mock
    private HubRepository hubRepository;

    @InjectMocks
    private HubService hubService;

    @Test
    void findAll_mapsHubsToDtos() {
        when(hubRepository.findAllByOrderByNameAsc())
                .thenReturn(List.of(new Hub("Biblioteca", 15), new Hub("Residências", 20)));

        List<HubDto> result = hubService.findAll();

        assertThat(result)
                .extracting(HubDto::name, HubDto::capacity)
                .containsExactly(
                        tuple("Biblioteca", 15),
                        tuple("Residências", 20));
    }

    @Test
    void findAll_returnsEmptyListWhenNoHubs() {
        when(hubRepository.findAllByOrderByNameAsc()).thenReturn(List.of());

        assertThat(hubService.findAll()).isEmpty();
    }
}
