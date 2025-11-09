package com.layoff.user_service.services;

import com.layoff.user_service.dtos.AddressDTO;
import com.layoff.user_service.dtos.UserRequest;
import com.layoff.user_service.models.Address;
import com.layoff.user_service.models.User;
import com.layoff.user_service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final KeyCloakAdminService keyCloakAdminService;
    private final UserRepository userRepository;
    
    public void addUser(UserRequest userRequest) {
        String token = keyCloakAdminService.getAdminAccessToken();
        String keyCloakUserId = keyCloakAdminService.createUser(token, userRequest);

        User user = new User();
        user.setId(keyCloakUserId);
        user.setKeycloakId(keyCloakUserId);
        updateUserFromRequest(user, userRequest);

        // Create and set address if provided
        if (userRequest.getAddress() != null) {
            Address address = createAddressFromDTO(userRequest.getAddress(), user);
            user.setAddress(address);
        }

        keyCloakAdminService.assignRealmRoleToUser(userRequest.getUsername(), "CUSTOMER", keyCloakUserId);
        userRepository.save(user);
    }

    private void updateUserFromRequest(User user, UserRequest userRequest) {
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setPhone(userRequest.getPhone());
    }

    private Address createAddressFromDTO(AddressDTO addressDTO, User user) {
        Address address = new Address();
        address.setUser(user);
        address.setStreet(addressDTO.getStreet());
        address.setCity(addressDTO.getCity());
        address.setState(addressDTO.getState());
        address.setZipCode(addressDTO.getZipCode());
        address.setCountry(addressDTO.getCountry());
        return address;
    }
}
