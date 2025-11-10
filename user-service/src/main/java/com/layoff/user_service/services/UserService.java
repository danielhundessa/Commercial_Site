package com.layoff.user_service.services;

import com.layoff.user_service.dtos.AddressDTO;
import com.layoff.user_service.dtos.UserRequest;
import com.layoff.user_service.dtos.UserResponse;
import com.layoff.user_service.models.Address;
import com.layoff.user_service.models.User;
import com.layoff.user_service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(this::convertToUserResponse).toList();
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());
        if (user.getAddress() != null) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setStreet(user.getAddress().getStreet());
            addressDTO.setCity(user.getAddress().getCity());
            addressDTO.setState(user.getAddress().getState());
            addressDTO.setZipCode(user.getAddress().getZipCode());
            addressDTO.setCountry(user.getAddress().getCountry());
            userResponse.setAddress(addressDTO);
        }
        return userResponse;
    }
}
