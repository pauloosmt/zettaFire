package com.br.zetta.fire.data.entity;


import com.br.zetta.fire.data.entity.enums.StatusFire;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "fire_event")
@NoArgsConstructor
public class FireEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_fire_event")
    private UUID idFireEvent;

    @Column(name = "id_foco_bdq", nullable = false, unique = true)
    private Long idFocoBdq;

    @Column(name = "foco_id")
    private UUID focoId;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "radius_of_risk", nullable = false)
    private Long radiusOfRisk;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "fire_risk")
    private Double fireRisk;

    @Column(name = "frp")
    private Double frp;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_fire", nullable = false)
    private StatusFire statusFire;


    @OneToMany(mappedBy = "fireEvent")
    private List<Alert> alert;

}
