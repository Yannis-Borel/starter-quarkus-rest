package org.acme.catalog.product.application;

import org.acme.catalog.product.application.dto.UserDTO;
import org.acme.catalog.product.application.dto.usecases.CreateUserRequest;
import org.acme.catalog.product.infrastructure.mapper.UserMapper;
import org.acme.catalog.product.infrastructure.persistence.UserEntity;
import org.acme.catalog.product.infrastructure.persistence.UserRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@ApplicationScoped
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<UserDTO> getAll() {
        return repository.listAll().stream()
                .map(UserMapper::toDto)
                .toList();
    }

 
    public Optional<UserDTO> getByUsername(String username) {
        return repository.findByUsername(username)
                .map(UserMapper::toDto);
    }

    
    @Transactional
    public UserDTO create(CreateUserRequest request) {

        UserEntity entity = UserMapper.toEntity(request);


        if (repository.findByUsername(entity.getUsername()).isPresent()) {
            throw new IllegalStateException("User already exists: " + entity.getUsername());
        }


        repository.persist(entity);

        return UserMapper.toDto(entity);
    }

  
    @Transactional
    public void delete(String username) {
        UserEntity p = repository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        repository.delete(p);
    }


    @Transactional
    public void clearAll() {
        repository.deleteAll();
    }
}
