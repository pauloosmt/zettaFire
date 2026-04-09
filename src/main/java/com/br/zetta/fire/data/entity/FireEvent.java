package com.br.zetta.fire.data.entity;

import com.br.zetta.fire.data.dto.request.FireEventRequestDTO;
import com.br.zetta.fire.data.entity.enums.StatusFire;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name="fireEvent")
@NoArgsConstructor
public class FireEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idFireEvent;

    @Column(name="latitude", nullable = false)
    private Double latitude;

    @Column(name="city", nullable = false)
    private String city;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name="radiusOfRisk", nullable = false)
    private Long radiusOfRisk;

    @Column(name="startData", nullable = false)
    private LocalDateTime startTime;

    @Column(name="status", nullable = false)
    private StatusFire statusFire;

    @OneToMany(mappedBy = "fireEvent")
    private List<Alert> alert;

    public FireEvent(FireEventRequestDTO fireEventRequestDTO) {
        this.latitude = fireEventRequestDTO.latitude();
        this.longitude = fireEventRequestDTO.longitude();
        this.radiusOfRisk = fireEventRequestDTO.radiusOfDisk();
        this.statusFire = fireEventRequestDTO.statusFire();
        this.city = fireEventRequestDTO.city();
    }


}
