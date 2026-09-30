package org.acme.catalog.product.infrastructure.mapper;

import org.acme.catalog.product.application.dto.UserDTO;
import org.acme.catalog.product.application.dto.usecases.CreateUserRequest;
import org.acme.catalog.product.domain.User;
import org.acme.catalog.product.infrastructure.persistence.UserEntity;

public final class UserMapper {

    private UserMapper() {

    }


    public static UserDTO toDto(UserEntity entity) {
        return new UserDTO(
                entity.getId(),
                entity.getUsername(),
                entity.getDisplayName(),
                entity.getJoinedAt());
    }


    public static UserEntity toEntity(CreateUserRequest request) {
   
        var domain = User.of(
            request.username(),
            request.displayName());

        return new UserEntity(
            domain.id(),
            domain.username(),
            domain.displayName(),
            domain.joinedAt());
    
    }
}
