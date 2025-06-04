package com.nikogrid.backend.services;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.exceptions.ResourceNotFound;
import com.nikogrid.backend.repositories.ChargerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ChargerServiceImplTest {
    @Mock
    private ChargerRepository chargerRepository;

    @InjectMocks
    private ChargerServiceImpl chargerService;

    @Test
    @Requirement("NIK-12")
    void findChargerByIdOk() throws ResourceNotFound {
        final Location location = new Location();
        location.setId(1L);
        location.setLat(20f);
        location.setLon(30f);

        final Charger charger = new Charger();
        charger.setId(1L);
        charger.setName("AAA1");
        charger.setMaxPower(250);
        charger.setLocation(location);

        Mockito.when(chargerRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(charger));

        this.chargerService.findChargerById(1L);

        Mockito.verify(this.chargerRepository, Mockito.times(1)).findById(1L);
    }

    @Test
    @Requirement("NIK-12")
    void findChargerByIdNotFound() {
        Mockito.when(chargerRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.chargerService.findChargerById(1L))
                .isInstanceOf(ResourceNotFound.class);

        Mockito.verify(this.chargerRepository, Mockito.times(1)).findById(1L);
    }
}
