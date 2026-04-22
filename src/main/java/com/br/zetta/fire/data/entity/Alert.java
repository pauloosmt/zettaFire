package com.br.zetta.fire.data.entity;

import com.br.zetta.fire.data.entity.enums.StatusAlert;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "alert")
@NoArgsConstructor
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idAlert;


    @Column(name="date", nullable = false)
    private LocalDate shippingDate;

    @Column(name="status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusAlert statusAlert;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_alert",
            joinColumns = @JoinColumn(name = "id_alert"),
            inverseJoinColumns = @JoinColumn(name = "id_user")
    )
    private List<User> userList = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name="idFireEvent")
    private FireEvent fireEvent;

    public void setUsers(List<User> user) {
        this.userList = user;
    }

    public void setStatus(StatusAlert status) {
        this.statusAlert = status;
    }

}
