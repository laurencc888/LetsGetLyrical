package edu.usc.csci310.project.controllers;

import edu.usc.csci310.project.requests.UserRequest;
import edu.usc.csci310.project.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
/*
status codes:
    HttpStatus.OK (200): success
    HttpStatus.BAD_REQUEST (400): invalid password
    HttpStatus.CONFLICT (409): username already exists
    HttpStatus.UNAUTHORIZED (401): incorrect username/password/username password combination
    HttpStatus.INTERNAL_SERVER_ERROR (500): database error
 */
@RestController
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequest request) {
        int code = userService.doesUsernameExist(request);
        if (code == -2) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("database error");
        }
        else if (code == 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("username does not exist");
        }

        code = userService.isValidUsernamePasswordCombination(request);
        if (code == 1) {
            return ResponseEntity.status(HttpStatus.OK).body("successfully logged in user");
        }
        else if (code == 0) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid username-password combination");
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("database error");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequest request) {
        if (!isValidPassword(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid password");
        }

        int code = userService.doesUsernameExist(request);
        if (code == -2) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("database error");
        }
        else if (code == 1) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("username already exists");
        }

        code = userService.addUser(request);
        if (code == 1) {
            return ResponseEntity.status(HttpStatus.OK).body("successfully created user");
        }
        else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("database error");
        }
    }

    private boolean isValidPassword(String input) {
        return input != null && input.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");
    }

}
