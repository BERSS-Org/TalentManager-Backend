package com.berss.platform.iam.interfaces.rest.resources;

import java.util.Date;

public record ProfileResource(
        Long id,
        String username,
        Long managerId,
        Long companyId,
        String companyName,
        Date memberSince
) {}
