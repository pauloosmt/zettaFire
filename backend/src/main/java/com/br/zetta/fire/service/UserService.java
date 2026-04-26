package com.br.zetta.fire.service;

import com.br.zetta.fire.data.dto.request.UserRequestDTO;
import com.br.zetta.fire.data.dto.response.UserResponseDTO;
import com.br.zetta.fire.data.entity.Address;
import com.br.zetta.fire.data.entity.User;
import com.br.zetta.fire.data.entity.enums.UserRole;
import com.br.zetta.fire.exceptions.custom.AlertProcessingException;
import com.br.zetta.fire.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.Email;
import org.springframework.cglib.core.Local;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


@Service
public class UserService {
    private final UserRepository userRepository;
    private final GeocodingService geocodingService;
    private final EmailService emailService;


    public UserService(UserRepository userRepository, GeocodingService geocodingService, EmailService emailService) {
        this.userRepository = userRepository;
        this.geocodingService = geocodingService;
        this.emailService = emailService;
    }

    private static final String SUBJECT = "️ \uD83D\uDD11 Código de Recuperação - Sistema Bem-Te-Vi";
    private static final String BODY = """
    Prezado Usuário,
    
    Recebemos uma solicitação para redefinir a senha da sua conta no sistema Bem-Te-Vi.
    
    Utilize o código de verificação abaixo para prosseguir:
    
    [ %s ]
    
    Este código é válido por 5 minutos.
    
    Este é um alerta automático. Não responda a este e-mail.
    """;

    public void generatePasswordResetToken(String email) {

        User user = (User) userRepository.findByEmail(email);
        if(user == null) throw new EntityNotFoundException("Não encontramos um usuário com este e-mail.");;

        //Gerando o codigo de troca de senha
        String code = String.format("%06d", new Random().nextInt(1000000));

        user.setResetToken(code);

        user.setTokenExpiration(LocalDateTime.now().plusMinutes(5)); //Adicionando o tempo que o codigo será válido

        userRepository.save(user);

        String formattedBody = String.format(BODY, code); //Formatando a mensagem do email, para ela conter o codigo

        emailService.sendEmail(user.getEmail(), null, SUBJECT, formattedBody);
    }

    public void validateAndChangePassword(String code, String newPassword) {
        User user = (User) userRepository.findByResetToken(code);

        if(user == null) throw new AlertProcessingException("Código de verificação inválido.");// Se não achar nenhum usuario com o codigo, significa que o codigo não existe no BD

        //Conferindo se o codigo ainda ta com o tempo válido
        if(LocalDateTime.now().isAfter(user.getTokenExpiration())) {
            user.setResetToken(null);
            userRepository.save(user);

            throw new AlertProcessingException("Este código expirou! Peça um novo.");
        }

        //Alteração da senha, e retirando os codigos do usuario (para melhor segurança e não gastar armazenamento)
        user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiration(null);

        userRepository.save(user);
    }



    public UserResponseDTO createUser(UserRequestDTO userRequestDTO, String password){
        Address address = new Address(userRequestDTO.address());
        BigDecimal[] coord = geocodingService.getCoordinates(
                userRequestDTO.address().street(),
                userRequestDTO.address().city(),
                userRequestDTO.address().state()
        ); //buscando coordenada do endereço

        address.setLatitude(coord[0]);
        address.setLongitude(coord[1]);

        User user = new User(userRequestDTO, password);
        user.setUserRole(UserRole.valueOf("USER"));
        user.setAddress(address);
        user.setPushToken(userRequestDTO.pushToken());

        userRepository.save(user);

        return new UserResponseDTO(user);
    }

    public String deleteUser(String email) {
        User user = (User) userRepository.findByEmail(email);

        if(user == null) throw new EntityNotFoundException("Usuário com o e-mail " + email + " não foi encontrado.");

        userRepository.delete(user);

        return "The user with email '"+ user.getEmail() + "' has been removed";
    }

    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream().map(UserResponseDTO::new).collect(Collectors.toList());
    }
}
