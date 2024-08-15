package com.example.taskmanagerproject.entities.security;

/**
 * Enumeration representing the possible roles in the application.
 */
public enum RoleName {
    ADMIN,               // Administrator with full access
    PRODUCT_OWNER,       // Product Owner responsible for product vision and requirements
    SCRUM_MASTER,        // Scrum Master ensuring agile practices and team coordination
    MANAGER,             // Manager with specific administrative permissions
    TEAM_LEAD,           // Team leader, overseeing team members
    SENIOR_DEVELOPER,    // Senior Developer with more responsibilities and oversight
    MIDDLE_DEVELOPER,    // Middle Developer with more experience than Junior Developer
    JUNIOR_DEVELOPER,    // Junior Developer with less experience and fewer responsibilities
    FULLSTACK_DEVELOPER, // Developer working on both frontend and backend
    BACKEND_DEVELOPER,   // Developer focusing on the backend part of the application
    FRONTEND_DEVELOPER,  // Developer focusing on the frontend part of the application
    QA_ENGINEER,         // Quality Assurance engineer, specifically focused on testing
    TESTER,              // Tester responsible for verifying the tasks
    USER                 // Default user with the lowest access level
}

