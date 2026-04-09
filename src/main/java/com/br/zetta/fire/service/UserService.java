package com.br.zetta.fire.service;

import com.br.zetta.fire.data.dto.request.UserRequestDTO;
import com.br.zetta.fire.data.dto.response.UserResponseDTO;
import com.br.zetta.fire.data.entity.Address;
import com.br.zetta.fire.data.entity.User;
import com.br.zetta.fire.data.entity.enums.UserRole;
import com.br.zetta.fire.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final GeocodingService geocodingService;

    public UserService(UserRepository userRepository, GeocodingService geocodingService) {
        this.userRepository = userRepository;
        this.geocodingService = geocodingService;
    }

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO, String password){
        Address address = new Address(userRequestDTO.address());
        BigDecimal[] coord = geocodingService.getCoordinates(
                userRequestDTO.address().street(),
                userRequestDTO.address().number(),
                userRequestDTO.address().city(),
                userRequestDTO.address().state(),
                userRequestDTO.address().cep()
        ); //buscando coordenada do endereço

        System.out.println("Lat: " + coord[0] + " Lon: " + coord[1]);

        address.setLatitude(coord[0]);
        address.setLongitude(coord[1]);


        User user = new User(userRequestDTO, password);
        user.setUserRole(UserRole.valueOf("ADMIN"));
        user.setAddress(address);

        userRepository.save(user);

        return new UserResponseDTO(user);
    }
}
