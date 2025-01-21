INSERT INTO task_list.roles (name, description)
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


INSERT INTO task_list.role_hierarchy (higher_role, lower_role)
VALUES
-- ADMIN has the highest level of access and can manage all other roles
((SELECT id FROM task_list.roles WHERE name = 'ADMIN'), (SELECT id FROM task_list.roles WHERE name = 'PRODUCT_OWNER')),
((SELECT id FROM task_list.roles WHERE name = 'ADMIN'), (SELECT id FROM task_list.roles WHERE name = 'SCRUM_MASTER')),
((SELECT id FROM task_list.roles WHERE name = 'ADMIN'), (SELECT id FROM task_list.roles WHERE name = 'MANAGER')),

-- PRODUCT_OWNER manages SCRUM_MASTER, TEAM_LEAD, and MANAGER
((SELECT id FROM task_list.roles WHERE name = 'PRODUCT_OWNER'), (SELECT id FROM task_list.roles WHERE name = 'SCRUM_MASTER')),
((SELECT id FROM task_list.roles WHERE name = 'PRODUCT_OWNER'), (SELECT id FROM task_list.roles WHERE name = 'TEAM_LEAD')),
((SELECT id FROM task_list.roles WHERE name = 'PRODUCT_OWNER'), (SELECT id FROM task_list.roles WHERE name = 'MANAGER')),

-- SCRUM_MASTER coordinates TEAM_LEAD and QA_ENGINEER
((SELECT id FROM task_list.roles WHERE name = 'SCRUM_MASTER'), (SELECT id FROM task_list.roles WHERE name = 'TEAM_LEAD')),
((SELECT id FROM task_list.roles WHERE name = 'SCRUM_MASTER'), (SELECT id FROM task_list.roles WHERE name = 'QA_ENGINEER')),

-- MANAGER has control over QA_ENGINEER, TEAM_LEAD, and TESTER
((SELECT id FROM task_list.roles WHERE name = 'MANAGER'), (SELECT id FROM task_list.roles WHERE name = 'QA_ENGINEER')),
((SELECT id FROM task_list.roles WHERE name = 'MANAGER'), (SELECT id FROM task_list.roles WHERE name = 'TEAM_LEAD')),
((SELECT id FROM task_list.roles WHERE name = 'MANAGER'), (SELECT id FROM task_list.roles WHERE name = 'TESTER')),

-- TEAM_LEAD delegates tasks
((SELECT id FROM task_list.roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM task_list.roles WHERE name = 'SENIOR_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM task_list.roles WHERE name = 'MIDDLE_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM task_list.roles WHERE name = 'JUNIOR_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM task_list.roles WHERE name = 'FULLSTACK_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM task_list.roles WHERE name = 'BACKEND_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM task_list.roles WHERE name = 'FRONTEND_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM task_list.roles WHERE name = 'QA_ENGINEER')),
((SELECT id FROM task_list.roles WHERE name = 'TEAM_LEAD'), (SELECT id FROM task_list.roles WHERE name = 'TESTER')),

-- SENIOR_DEVELOPER delegates tasks
((SELECT id FROM task_list.roles WHERE name = 'SENIOR_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'MIDDLE_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'SENIOR_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'JUNIOR_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'SENIOR_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'FULLSTACK_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'SENIOR_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'BACKEND_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'SENIOR_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'FRONTEND_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'SENIOR_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'QA_ENGINEER')),
((SELECT id FROM task_list.roles WHERE name = 'SENIOR_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'TESTER')),

-- MIDDLE_DEVELOPER delegates tasks
((SELECT id FROM task_list.roles WHERE name = 'MIDDLE_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'JUNIOR_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'MIDDLE_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'FULLSTACK_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'MIDDLE_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'BACKEND_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'MIDDLE_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'FRONTEND_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'MIDDLE_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'QA_ENGINEER')),
((SELECT id FROM task_list.roles WHERE name = 'MIDDLE_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'TESTER')),

-- JUNIOR_DEVELOPER delegates tasks
((SELECT id FROM task_list.roles WHERE name = 'JUNIOR_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'USER')),

-- FULLSTACK_DEVELOPER delegates tasks
((SELECT id FROM task_list.roles WHERE name = 'FULLSTACK_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'SENIOR_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'FULLSTACK_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'MIDDLE_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'FULLSTACK_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'JUNIOR_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'FULLSTACK_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'BACKEND_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'FULLSTACK_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'FRONTEND_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'FULLSTACK_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'QA_ENGINEER')),
((SELECT id FROM task_list.roles WHERE name = 'FULLSTACK_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'TESTER')),

-- BACKEND_DEVELOPER delegates tasks
((SELECT id FROM task_list.roles WHERE name = 'BACKEND_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'SENIOR_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'BACKEND_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'MIDDLE_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'BACKEND_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'JUNIOR_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'BACKEND_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'FULLSTACK_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'BACKEND_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'FRONTEND_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'BACKEND_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'QA_ENGINEER')),
((SELECT id FROM task_list.roles WHERE name = 'BACKEND_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'TESTER')),

-- FRONTEND_DEVELOPER delegates tasks
((SELECT id FROM task_list.roles WHERE name = 'FRONTEND_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'SENIOR_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'FRONTEND_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'MIDDLE_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'FRONTEND_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'JUNIOR_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'FRONTEND_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'FULLSTACK_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'FRONTEND_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'BACKEND_DEVELOPER')),
((SELECT id FROM task_list.roles WHERE name = 'FRONTEND_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'QA_ENGINEER')),
((SELECT id FROM task_list.roles WHERE name = 'FRONTEND_DEVELOPER'), (SELECT id FROM task_list.roles WHERE name = 'TESTER')),

-- QA_ENGINEER manages TESTER
((SELECT id FROM task_list.roles WHERE name = 'QA_ENGINEER'), (SELECT id FROM task_list.roles WHERE name = 'TESTER'));


INSERT INTO task_list.achievements (title, description, image_url)
VALUES ('First Milestone', 'Completed 10 tasks in total.', 'https://img.icons8.com/ios/452/medal.png'),
       ('Second Milestone', 'Completed 100 tasks in total.', 'https://img.icons8.com/ios/452/gold-medal.png'),
       ('Third Milestone', 'Completed 500 tasks in total.', 'https://img.icons8.com/ios/452/trophy.png'),
       ('Master of Tasks', 'Completed 1000+ tasks in total.', 'https://img.icons8.com/ios/452/cup.png'),
       ('Legendary Contributor', 'Surpassed 2000+ completed tasks.', 'https://img.icons8.com/ios/452/master.png'),

       ('Consistent Closer', 'Completed 30+ tasks in a month.', 'https://img.icons8.com/ios/452/goal.png'),
       ('Deadline Crusher', 'Completed 20 tasks before deadlines.', 'https://img.icons8.com/ios/452/clock.png'),
       ('Critical Thinker', 'Solved 20+ high-priority tasks.', 'https://img.icons8.com/ios/452/brain.png'),
       ('Stability Savior', 'Mitigated system crashes by resolving 40+ critical issues.', 'https://img.icons8.com/ios/452/shield.png'),
       ('Task Warrior', 'Completed 5+ tasks in a day.', 'https://img.icons8.com/ios/452/lightning-bolt.png'),
       ('Rejection Survivor', 'Revised and got 10+ rejected tasks approved.', 'https://img.icons8.com/ios/452/refresh.png'),

       ('Bug Slayer', 'Resolved 20+ critical bugs in a sprint.', 'https://img.icons8.com/ios/452/badge.png'),
       ('Code Doctor', 'Fixed 100+ bugs in total.', 'https://img.icons8.com/ios/452/stethoscope.png'),
       ('Bug Bounty Hunter', 'Reported 25+ critical issues.', 'https://img.icons8.com/ios/452/bug.png'),
       ('Quality Champion', 'Resolved 30+ review comments.', 'https://img.icons8.com/ios/452/good-quality.png'),

       ('Time Wizard', 'Completed 20+ tasks 10% faster than average.', 'https://img.icons8.com/ios/452/hourglass.png'),
       ('On-Time Achiever', 'Maintained a 90% on-time completion rate.', 'https://img.icons8.com/ios/452/clock.png'),
       ('Deadline Hero', 'Completed an critical task in less than 24 hours.', 'https://img.icons8.com/ios/452/speed.png'),
       ('Last-Minute Savior', 'Saved a project by resolving a task just before the deadline.', 'https://img.icons8.com/ios/452/timer.png'),

       ('Team Player', 'Collaborated with 5+ teams on cross-functional projects.', 'https://img.icons8.com/ios/452/teamwork.png'),
       ('Long-Term Strategist', 'Managed tasks for over 6 months continuously.', 'https://img.icons8.com/ios/452/calendar.png'),
       ('Marathon Worker', 'Completed 50+ long-duration tasks.', 'https://img.icons8.com/ios/452/patience.png'),
       ('Task Champion', 'Achieved 90%+ task completion consistency for 12 months.', 'https://img.icons8.com/ios/452/trophy.png');
