package org.adyl.security.services;

import org.adyl.exceptions.ObjectAlreadyPresentException;
import org.adyl.exceptions.PasswordFormatException;
import org.adyl.mapper.abstraction.AbstractMapperImpl;
import org.adyl.model.Costumer;
import org.adyl.repository.CostumerRepository;
import org.adyl.security.models.StoreUser;
import org.adyl.security.models.dto.StoreRegistrationUserDTO;
import org.adyl.security.models.dto.StoreUserDTO;
import org.adyl.security.repositories.StoreUserRepository;
import org.adyl.service.DefaultService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
//@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class StoreUserService implements DefaultService<StoreUserDTO, StoreUser, Long> {
    private final StoreUserRepository repository;
    private final CostumerRepository costumerRepository;
    private final AbstractMapperImpl mapper;
    private PasswordEncoder encoder;

    public StoreUserService(StoreUserRepository repository, CostumerRepository costumerRepository, AbstractMapperImpl mapper, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.costumerRepository = costumerRepository;
        this.mapper = mapper;
        this.encoder = passwordEncoder;
    }

    @Override
    public List<StoreUserDTO> findAll() {
        return repository.findAll().stream().map(user -> mapper.toDTO(user, StoreUserDTO.class)).collect(Collectors.toList());
    }

    @Override
    public StoreUserDTO findByKey(Long key) {
        return mapper.toDTO(repository.findById(key).orElseThrow(null), StoreUserDTO.class);
    }

    public StoreUserDTO findByUsername(String username) {
        return mapper.toDTO(repository.findByUsername(username), StoreUserDTO.class);
    }

    private StoreUserDTO save(StoreUser user) {
        if (user.getCostumer() == null) {
            Costumer costumerForUser = costumerRepository.save(new Costumer(user.getUsername()));
            user.setCostumer(costumerForUser);
        }
        user = repository.save(user);
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
        repository.save(user);
        costumerRepository.save(user.getCostumer());//
        return mapper.toDTO(user, StoreUserDTO.class);
    }
    
    public StoreUserDTO update(Long key, StoreUser user) {
//        StoreUser user = mapper.toEntity(obj, StoreUser.class);
        user.setId(key);
        repository.save(user);
        costumerRepository.save(user.getCostumer());//
        return mapper.toDTO(user, StoreUserDTO.class);
    }

    public StoreUserDTO update(String username, StoreUserDTO obj) {
        StoreUser user = repository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Such user was not found!"));
        StoreUser updatedUser = mapper.toEntity(obj, StoreUser.class);
        updatedUser.setId(user.getId());
        repository.save(updatedUser);
        costumerRepository.save(updatedUser.getCostumer());//
        return mapper.toDTO(updatedUser, StoreUserDTO.class);
    }

    public StoreUser updatePasswordFor(String password, StoreUser user) throws PasswordFormatException {
        if (password.matches("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*\\_-]).{8,}$")) {
            user.setPassword(encoder.encode(password));
            return repository.save(user);
        } else {
            throw new PasswordFormatException();
        }
    }

    @Override
    public void delete(Long key) {
        StoreUser user = repository.findById(key).orElseThrow(() -> new IllegalArgumentException("Such user does no exist!"));
        repository.deleteById(key);
        costumerRepository.deleteById(user.getCostumer().getId());
    }
}
