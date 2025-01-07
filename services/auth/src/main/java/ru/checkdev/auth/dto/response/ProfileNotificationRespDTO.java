package ru.checkdev.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.checkdev.auth.domain.Profile;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ProfileNotificationRespDTO {
    @EqualsAndHashCode.Include
    private Integer id;

    private String username;
    private String email;
    private boolean active;

    public ProfileNotificationRespDTO(Profile profile) {
        this.id = profile.getId();
        this.username = profile.getUsername();
        this.email = profile.getEmail();
        this.active = profile.isActive();
    }

    public ProfileNotificationRespDTO() {

    }
}
