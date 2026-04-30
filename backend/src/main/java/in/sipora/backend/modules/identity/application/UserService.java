package in.sipora.backend.modules.identity.application;

import in.sipora.backend.modules.identity.domain.Address;
import in.sipora.backend.modules.identity.domain.User;
import in.sipora.backend.modules.identity.domain.UserRepository;
import in.sipora.backend.modules.identity.web.IdentityDTOs.AddressRequest;
import in.sipora.backend.modules.identity.web.IdentityDTOs.AddressResponse;
import in.sipora.backend.modules.identity.web.IdentityDTOs.ChangePasswordRequest;
import in.sipora.backend.modules.identity.web.IdentityDTOs.UpdateProfileRequest;
import in.sipora.backend.modules.identity.web.IdentityDTOs.UserResponse;
import in.sipora.backend.shared.exception.DomainException;
import in.sipora.backend.shared.exception.ResourceNotFoundException;
import in.sipora.backend.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // Profile ==>

    @Transactional(readOnly = true)
    public UserResponse getProfile(UUID userId) {
        return userMapper.toResponse(findOrThrow(userId));
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findOrThrow(userId);

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    // Password ==>

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findOrThrow(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new DomainException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new ValidationException("New password must be different from current password");
        }

        userRepository.updatePassword(userId, passwordEncoder.encode(request.newPassword()));
    }

    // Addresses ==>

    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(UUID userId) {
        User user = findOrThrow(userId);
        return toAddressResponses(user.getAddresses());
    }

    @Transactional
    public List<AddressResponse> addAddress(UUID userId, AddressRequest request) {
        User user = findOrThrow(userId);

        if (user.getAddresses().size() >= 5) {
            throw new ValidationException("You can save a maximum of 5 addresses");
        }

        user.getAddresses().add(Address.builder()
                .line1(request.line1())
                .line2(request.line2())
                .city(request.city())
                .state(request.state())
                .pinCode(request.pinCode())
                .label(request.label())
                .build());

        return toAddressResponses(userRepository.save(user).getAddresses());
    }

    @Transactional
    public List<AddressResponse> updateAddress(UUID userId, int index, AddressRequest request) {
        User user = findOrThrow(userId);
        List<Address> addresses = user.getAddresses();

        if (index < 0 || index >= addresses.size()) {
            throw new ResourceNotFoundException("Address not found at index: " + index);
        }

        Address updated = Address.builder()
                .line1(request.line1())
                .line2(request.line2())
                .city(request.city())
                .state(request.state())
                .pinCode(request.pinCode())
                .label(request.label())
                .build();
        addresses.set(index, updated);

        return toAddressResponses(userRepository.save(user).getAddresses());
    }

    @Transactional
    public void deleteAddress(UUID userId, int index) {
        User user = findOrThrow(userId);
        List<Address> addresses = user.getAddresses();

        if (index < 0 || index >= addresses.size()) {
            throw new ResourceNotFoundException("Address not found at index: " + index);
        }

        addresses.remove(index);
        userRepository.save(user);
    }

    // Helpers ==>

    private User findOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));
    }

    private List<AddressResponse> toAddressResponses(List<Address> addresses) {
        return IntStream.range(0, addresses.size())
                .mapToObj(i -> {
                    Address a = addresses.get(i);
                    return new AddressResponse(
                            i, a.getLine1(), a.getLine2(), a.getCity(),
                            a.getState(), a.getPinCode(), a.getCountry(), a.getLabel());
                })
                .toList();
    }
}