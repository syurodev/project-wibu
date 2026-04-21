package com.syuro.wibusystem.user.controller.v1;

import com.syuro.wibusystem.user.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
public class UserController {
    final UserService userService;

    public UserController(
            UserService userService
    ) {
        this.userService = userService;
    }
}
