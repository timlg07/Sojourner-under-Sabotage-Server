package de.tim_greller.susserver.service;

import java.util.Optional;

import de.tim_greller.susserver.dto.UserDTO;
import de.tim_greller.susserver.exception.UserAlreadyExistException;
import de.tim_greller.susserver.persistence.entity.UserEntity;
import de.tim_greller.susserver.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserService(@Autowired UserRepository repository, @Autowired PasswordEncoder encoder) {
        this.userRepository = repository;
        this.encoder = encoder;
    }

    public UserEntity registerNewUserAccount(UserDTO userDto) throws UserAlreadyExistException {
        if (emailExists(userDto.getEmail())) {
            throw new UserAlreadyExistException("There is an account with that email address: "
                    + userDto.getEmail());
        }

        UserEntity user = new UserEntity();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEncodedPassword(encoder.encode(userDto.getPassword()));
        user.setEmail(userDto.getEmail());

        return userRepository.save(user);
    }

    private boolean emailExists(String email) {
        return userRepository.existsById(email);
    }

    public Optional<UserEntity> loadUserByEmail(String username) {
        return userRepository.findById(username);
    }
}
