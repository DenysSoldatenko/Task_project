INSERT INTO roles (name, description)
VALUES ('ADMIN', 'Administrator with full access'),
       ('PRODUCT_OWNER', 'Product Owner responsible for product vision and requirements'),
       ('SCRUM_MASTER', 'Scrum Master ensuring agile practices and team coordination'),
       ('MANAGER', 'Manager with specific administrative permissions'),
       ('TEAM_LEAD', 'Team leader, overseeing team members'),
       ('SENIOR_DEVELOPER', 'Senior Developer with more responsibilities and oversight'),
       ('MIDDLE_DEVELOPER', 'Middle Developer with more experience than Junior Developer'),
       ('JUNIOR_DEVELOPER', 'Junior Developer with less experience and fewer responsibilities'),
       ('FULLSTACK_DEVELOPER', 'Developer working on both frontend and backend'),
       ('BACKEND_DEVELOPER', 'Developer focusing on the backend part of the application'),
       ('FRONTEND_DEVELOPER', 'Developer focusing on the frontend part of the application'),
       ('QA_ENGINEER', 'Quality Assurance engineer, specifically focused on testing'),
       ('TESTER', 'Tester responsible for verifying the tasks'),
       ('USER', 'Default user with the lowest access level');


INSERT INTO role_hierarchy (higher_role, lower_role)
VALUES
-- ADMIN має найвищий рівень доступу і може керувати всіма іншими ролями
((SELECT id FROM roles WHERE name = 'ADMIN'), (SELECT id FROM roles WHERE name = 'PRODUCT_OWNER')),
((SELECT id FROM roles WHERE name = 'ADMIN'), (SELECT id FROM roles WHERE name = 'SCRUM_MASTER')),
((SELECT id FROM roles WHERE name = 'ADMIN'), (SELECT id FROM roles WHERE name = 'MANAGER')),

-- PRODUCT_OWNER керує SCRUM_MASTER, TEAM_LEAD та MANAGER
((SELECT id FROM roles WHERE name = 'PRODUCT_OWNER'), (SELECT id FROM roles WHERE name = 'SCRUM_MASTER')),
((SELECT id FROM roles WHERE name = 'PRODUCT_OWNER'), (SELECT id FROM roles WHERE name = 'TEAM_LEAD')),
((SELECT id FROM roles WHERE name = 'PRODUCT_OWNER'), (SELECT id FROM roles WHERE name = 'MANAGER')),

-- SCRUM_MASTER координує TEAM_LEAD та QA_ENGINEER
((SELECT id FROM roles WHERE name = 'SCRUM_MASTER'), (SELECT id FROM roles WHERE name = 'TEAM_LEAD')),
((SELECT id FROM roles WHERE name = 'SCRUM_MASTER'), (SELECT id FROM roles WHERE name = 'QA_ENGINEER')),

-- MANAGER має контроль над QA_ENGINEER, TEAM_LEAD і TESTER
((SELECT id FROM roles WHERE name = 'MANAGER'), (SELECT id FROM roles WHERE name = 'QA_ENGINEER')),
((SELECT id FROM roles WHERE name = 'MANAGER'), (SELECT id FROM roles WHERE name = 'TEAM_LEAD')),
((SELECT id FROM roles WHERE name = 'MANAGER'), (SELECT id FROM roles WHERE name = 'TESTER')),

-- TEAM_LEAD делегує завдання
((SELECT id FROM roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM roles WHERE name = 'SENIOR_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM roles WHERE name = 'MIDDLE_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM roles WHERE name = 'JUNIOR_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM roles WHERE name = 'FULLSTACK_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM roles WHERE name = 'BACKEND_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM roles WHERE name = 'FRONTEND_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM roles WHERE name = 'QA_ENGINEER')),
((SELECT id FROM roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM roles WHERE name = 'TESTER')),

-- SENIOR_DEVELOPER делегує завдання
((SELECT id FROM roles WHERE name = 'SENIOR_DEVELOPER'), (SELECT id FROM roles WHERE name = 'MIDDLE_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'SENIOR_DEVELOPER'), (SELECT id FROM roles WHERE name = 'JUNIOR_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'SENIOR_DEVELOPER'), (SELECT id FROM roles WHERE name = 'FULLSTACK_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'SENIOR_DEVELOPER'), (SELECT id FROM roles WHERE name = 'BACKEND_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'SENIOR_DEVELOPER'), (SELECT id FROM roles WHERE name = 'FRONTEND_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'SENIOR_DEVELOPER'), (SELECT id FROM roles WHERE name = 'QA_ENGINEER')),
((SELECT id FROM roles WHERE name = 'SENIOR_DEVELOPER'), (SELECT id FROM roles WHERE name = 'TESTER')),

-- MIDDLE_DEVELOPER делегує завдання
((SELECT id FROM roles WHERE name = 'MIDDLE_DEVELOPER'), (SELECT id FROM roles WHERE name = 'JUNIOR_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'MIDDLE_DEVELOPER'), (SELECT id FROM roles WHERE name = 'FULLSTACK_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'MIDDLE_DEVELOPER'), (SELECT id FROM roles WHERE name = 'BACKEND_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'MIDDLE_DEVELOPER'), (SELECT id FROM roles WHERE name = 'FRONTEND_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'MIDDLE_DEVELOPER'), (SELECT id FROM roles WHERE name = 'QA_ENGINEER')),
((SELECT id FROM roles WHERE name = 'MIDDLE_DEVELOPER'), (SELECT id FROM roles WHERE name = 'TESTER')),

-- JUNIOR_DEVELOPER делегує завдання
((SELECT id FROM roles WHERE name = 'JUNIOR_DEVELOPER'), (SELECT id FROM roles WHERE name = 'USER')),

-- FULLSTACK_DEVELOPER делегує завдання
((SELECT id FROM roles WHERE name = 'FULLSTACK_DEVELOPER'), (SELECT id FROM roles WHERE name = 'SENIOR_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'FULLSTACK_DEVELOPER'), (SELECT id FROM roles WHERE name = 'MIDDLE_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'FULLSTACK_DEVELOPER'), (SELECT id FROM roles WHERE name = 'JUNIOR_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'FULLSTACK_DEVELOPER'), (SELECT id FROM roles WHERE name = 'BACKEND_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'FULLSTACK_DEVELOPER'), (SELECT id FROM roles WHERE name = 'FRONTEND_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'FULLSTACK_DEVELOPER'), (SELECT id FROM roles WHERE name = 'QA_ENGINEER')),
((SELECT id FROM roles WHERE name = 'FULLSTACK_DEVELOPER'), (SELECT id FROM roles WHERE name = 'TESTER')),

-- BACKEND_DEVELOPER делегує завдання
((SELECT id FROM roles WHERE name = 'BACKEND_DEVELOPER'), (SELECT id FROM roles WHERE name = 'SENIOR_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'BACKEND_DEVELOPER'), (SELECT id FROM roles WHERE name = 'MIDDLE_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'BACKEND_DEVELOPER'), (SELECT id FROM roles WHERE name = 'JUNIOR_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'BACKEND_DEVELOPER'), (SELECT id FROM roles WHERE name = 'FULLSTACK_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'BACKEND_DEVELOPER'), (SELECT id FROM roles WHERE name = 'FRONTEND_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'BACKEND_DEVELOPER'), (SELECT id FROM roles WHERE name = 'QA_ENGINEER')),
((SELECT id FROM roles WHERE name = 'BACKEND_DEVELOPER'), (SELECT id FROM roles WHERE name = 'TESTER')),

-- FRONTEND_DEVELOPER делегує завдання
((SELECT id FROM roles WHERE name = 'FRONTEND_DEVELOPER'), (SELECT id FROM roles WHERE name = 'SENIOR_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'FRONTEND_DEVELOPER'), (SELECT id FROM roles WHERE name = 'MIDDLE_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'FRONTEND_DEVELOPER'), (SELECT id FROM roles WHERE name = 'JUNIOR_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'FRONTEND_DEVELOPER'), (SELECT id FROM roles WHERE name = 'FULLSTACK_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'FRONTEND_DEVELOPER'), (SELECT id FROM roles WHERE name = 'BACKEND_DEVELOPER')),
((SELECT id FROM roles WHERE name = 'FRONTEND_DEVELOPER'), (SELECT id FROM roles WHERE name = 'QA_ENGINEER')),
((SELECT id FROM roles WHERE name = 'FRONTEND_DEVELOPER'), (SELECT id FROM roles WHERE name = 'TESTER')),

-- QA_ENGINEER керує TESTER
((SELECT id FROM roles WHERE name = 'QA_ENGINEER'), (SELECT id FROM roles WHERE name = 'TESTER'))
