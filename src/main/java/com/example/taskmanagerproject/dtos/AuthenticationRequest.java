package com.example.taskmanagerproject.dtos;

/**
 * Data Transfer Object (DTO) for authentication requests.
 */
public record AuthenticationRequest(String username, String password) {
}