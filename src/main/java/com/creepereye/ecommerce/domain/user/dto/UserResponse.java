package com.creepereye.ecommerce.domain.user.dto;

import com.creepereye.ecommerce.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserResponse {
    private String username;
    private String name;
    private String phone;
    private String address;

    public UserResponse(User user) {
        this.username = user.getAuth().getUsername();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.address = user.getAddress();
    }
}
