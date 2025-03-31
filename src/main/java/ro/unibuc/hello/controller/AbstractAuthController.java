package ro.unibuc.hello.controller;

import org.springframework.beans.factory.annotation.Autowired;
import ro.unibuc.hello.dto.UserDto;
import ro.unibuc.hello.service.implementation.UserServiceImpl;
import ro.unibuc.hello.exception.UnauthorizedException;


public abstract class AbstractAuthController {
    @Autowired
    protected UserServiceImpl userService;

    protected UserDto getAuthenticatedUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("User is not authenticated.");
        }
        String token = authHeader.substring(7); // extract token from header
        return userService.getUserFromToken(token);
    }
}
