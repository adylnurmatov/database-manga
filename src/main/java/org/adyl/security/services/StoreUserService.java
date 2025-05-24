package org.adyl.security.services;

import com.vaadin.flow.server.VaadinService;
import org.adyl.exceptions.ObjectAlreadyPresentException;
import org.adyl.exceptions.PasswordFormatException;
import org.adyl.mapper.abstraction.AbstractMapperImpl;
import org.adyl.model.Customer;
import org.adyl.repository.CustomerRepository;
import org.adyl.security.enums.ResetTokenStatus;
import org.adyl.security.models.PasswordResetToken;
import org.adyl.security.models.StoreUser;
import org.adyl.security.models.dto.StoreRegistrationUserDTO;
import org.adyl.security.models.dto.StoreUserDTO;
import org.adyl.security.repositories.PasswordResetTokenRepository;
import org.adyl.security.repositories.StoreUserRepository;
import org.adyl.service.DefaultService;
import org.adyl.service.MailService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
//@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class StoreUserService implements DefaultService<StoreUserDTO, StoreUser, Long> {
    private final StoreUserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final AbstractMapperImpl mapper;
    private final MailService mailService;
    private PasswordEncoder encoder;

    public StoreUserService(StoreUserRepository userRepository, CustomerRepository customerRepository, PasswordResetTokenRepository tokenRepository, AbstractMapperImpl mapper, MailService mailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.tokenRepository = tokenRepository;
        this.mapper = mapper;
        this.mailService = mailService;
        this.encoder = passwordEncoder;
    }

    @Override
    public List<StoreUserDTO> findAll() {
        return userRepository.findAll().stream().map(user -> mapper.toDTO(user, StoreUserDTO.class)).collect(Collectors.toList());
    }

    @Override
    public StoreUserDTO findByKey(Long key) {
        return mapper.toDTO(userRepository.findById(key).orElseThrow(null), StoreUserDTO.class);
    }

    public StoreUserDTO findByUsername(String username) {
        return mapper.toDTO(userRepository.findByUsername(username), StoreUserDTO.class);
    }

    private StoreUserDTO save(StoreUser user) {
        if (user.getCustomer() == null) {
            Customer customerForUser = customerRepository.save(new Customer(user.getUsername()));
            user.setCustomer(customerForUser);
        }
        user = userRepository.save(user);
        return mapper.toDTO(user, StoreUserDTO.class);
    }

    @Override
    public StoreUserDTO save(StoreUserDTO obj) {
        StoreUser user = mapper.toEntity(obj, StoreUser.class);
        return save(user);
    }

    public StoreUserDTO register(StoreRegistrationUserDTO registrationUserDTO) throws ObjectAlreadyPresentException {
        if(findByUsername(registrationUserDTO.getUsername()) != null) {
            throw new ObjectAlreadyPresentException("Choose another username!");
        }
        StoreUser bookStoreUser = mapper.toEntity(registrationUserDTO, StoreUser.class);
        bookStoreUser.setRoles("ROLE_USER");
        return save(bookStoreUser);
    }

    @Override
    public StoreUserDTO update(Long key, StoreUserDTO obj) {
        StoreUser user = mapper.toEntity(obj, StoreUser.class);
        user.setId(key);
        userRepository.save(user);
        customerRepository.save(user.getCustomer());//
        return mapper.toDTO(user, StoreUserDTO.class);
    }
    
    public StoreUserDTO update(Long key, StoreUser user) {
//        StoreUser user = mapper.toEntity(obj, StoreUser.class);
        user.setId(key);
        userRepository.save(user);
        customerRepository.save(user.getCustomer());//
        return mapper.toDTO(user, StoreUserDTO.class);
    }

    public StoreUserDTO update(String username, StoreUserDTO obj) {
        StoreUser user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Such user was not found!"));
        StoreUser updatedUser = mapper.toEntity(obj, StoreUser.class);
        updatedUser.setId(user.getId());
        userRepository.save(updatedUser);
        customerRepository.save(updatedUser.getCustomer());//
        return mapper.toDTO(updatedUser, StoreUserDTO.class);
    }

    public void updatePasswordFor(String password, StoreUser user) throws PasswordFormatException {
        if (password.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*\\_-]).{8,}$")) {
            user.setPassword(encoder.encode(password));
            userRepository.save(user);
        } else {
            throw new PasswordFormatException();
        }
    }

    @Override
    public void delete(Long key) {
        StoreUser user = userRepository.findById(key).orElseThrow(() -> new IllegalArgumentException("Such user does no exist!"));
        userRepository.deleteById(key);
        customerRepository.deleteById(user.getCustomer().getId());
    }

    @Transactional
    public ResetTokenStatus sendResetToken(String email) {
        Optional<StoreUser> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResetTokenStatus.USER_NOT_FOUND;
        }

        StoreUser user = optionalUser.get();
        Optional<PasswordResetToken> optionalToken = tokenRepository.findByUser(user);
        if (optionalToken.isPresent() && !optionalToken.get().isExpired()) {
            return ResetTokenStatus.TOKEN_ALREADY_SENT;
        }

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(1);
        tokenRepository.save(new PasswordResetToken(token, user, expiry));

        String link = "http://localhost:8081/reset-password?token=" + token;
        mailService.sendResetLink(user.getEmail(), link);

        return ResetTokenStatus.SUCCESS;
    }


    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> optionalToken = tokenRepository.findByToken(token);
        if (optionalToken.isEmpty() || optionalToken.get().isExpired()) {
            return false;
        }

        StoreUser user = optionalToken.get().getUser();
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(optionalToken.get());

        return true;
    }

    public boolean isResetTokenValid(String token) {
        return tokenRepository.findByToken(token)
                .filter(t -> !t.isExpired())
                .isPresent();
    }


}
