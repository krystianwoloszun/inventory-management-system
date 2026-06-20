package com.krystianwoloszun.inv360.auth.user.dto;

import com.krystianwoloszun.inv360.auth.user.Role;

public record UserResponse(
        Long id,
        String email,
        Role role) {

}
