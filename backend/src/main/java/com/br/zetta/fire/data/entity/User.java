package com.br.zetta.fire.data.entity;

import com.br.zetta.fire.data.dto.request.UserRequestDTO;
import com.br.zetta.fire.data.entity.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idUser;

    @Column(name="name", nullable = false)
    private String name;

    @Column(name="email", unique = true, nullable = false)
    private String email;

    @Column(name="password", nullable = false)
    private String password;

    @Column(name="phone", nullable = false)
    private String phone;

    @Column(name = "push_token", nullable = true)
    private String pushToken;

    @Column(name = "createdAt")
    private LocalDate createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name="userRole")
    private UserRole userRole;

    @ToString.Exclude
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idAddress", referencedColumnName = "idAddress")
    private Address address;

    @ManyToMany
    @JoinTable(
            name = "user_alert",
            joinColumns = @JoinColumn(name = "id_user"),
            inverseJoinColumns = @JoinColumn(name = "id_alert")
    )
    private List<Alert> alertList = new ArrayList<>();

    private String resetToken;
    private LocalDateTime tokenExpiration;

    @Builder
    public User(UserRequestDTO userRequestDTO, String encryptedPassword) {
        this.name = userRequestDTO.name();
        this.email = userRequestDTO.email();
        this.password = encryptedPassword;
        this.phone = userRequestDTO.phone();
        this.pushToken = userRequestDTO.pushToken();
        this.userRole = UserRole.USER;
        this.createdAt = LocalDate.now();

        Address addr = new Address();
        addr.setCep(userRequestDTO.address().cep());
        addr.setCity(userRequestDTO.address().city());
        addr.setStreet(userRequestDTO.address().street());
        addr.setNumber(userRequestDTO.address().number());
        addr.setDistrict(userRequestDTO.address().district());
        addr.setState(userRequestDTO.address().state());

        this.address = addr;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.userRole == UserRole.ADMIN) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        } else {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}