package com.br.zetta.fire.service;

import com.br.zetta.fire.data.dto.request.UserRequestDTO;
import com.br.zetta.fire.data.dto.response.UserResponseDTO;
import com.br.zetta.fire.data.entity.User;
import com.br.zetta.fire.data.entity.enums.UserRole;
import com.br.zetta.fire.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDTO createUser(UserRequestDTO userRequestDTO, String password){
        User user = new User(userRequestDTO, password);
        user.setUserRole(UserRole.valueOf("ADMIN"));

        userRepository.save(user);

        return new UserResponseDTO(user);
    }
}
