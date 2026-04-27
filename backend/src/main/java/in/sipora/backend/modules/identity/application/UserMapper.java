package in.sipora.backend.modules.identity.application;

import in.sipora.backend.modules.identity.api.UserSummary;
import in.sipora.backend.modules.identity.domain.Address;
import in.sipora.backend.modules.identity.domain.User;
import in.sipora.backend.modules.identity.web.IdentityDTOs.AddressResponse;
import in.sipora.backend.modules.identity.web.IdentityDTOs.UserResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Manual mapper from User entity to various DTOs.
 *
 * We use a hand-written mapper instead of MapStruct here because the
 * address list mapping requires index tracking (IntStream.range),
 * which MapStruct expressions handle awkwardly.
 *
 * For flat entity→DTO conversions in other modules, prefer MapStruct.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.isEnabled(),
                user.getCreatedAt(),
                toAddressResponses(user.getAddresses())
        );
    }

    public UserSummary toSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name()
        );
    }

    private List<AddressResponse> toAddressResponses(List<Address> addresses) {
        if (addresses == null) return List.of();
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