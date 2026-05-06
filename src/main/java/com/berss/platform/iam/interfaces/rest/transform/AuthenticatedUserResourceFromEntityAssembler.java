package com.berss.platform.iam.interfaces.rest.transform;

import com.berss.platform.iam.domain.model.aggregates.User;
import com.berss.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;

public class AuthenticatedUserResourceFromEntityAssembler {
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        return new AuthenticatedUserResource(
                user.getId(),
                user.getUsername(),
                user.getManagerId().getValue(),
                user.getCompanyId().getValue(),
                token
        );
    }
}
