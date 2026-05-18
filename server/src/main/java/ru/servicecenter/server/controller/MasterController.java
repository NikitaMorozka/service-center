package ru.servicecenter.server.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.servicecenter.server.dto.user.UserResponse;
import ru.servicecenter.server.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/masters")
@RequiredArgsConstructor
public class MasterController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> findMasters() {
        return userService.findMasters();
    }
}
