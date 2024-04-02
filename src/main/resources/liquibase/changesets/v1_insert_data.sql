-- Insert Users
INSERT INTO users (full_name, username, password, confirm_password)
VALUES
    ('John Doe', 'john.doe@gmail.com',
     '$2a$10$OPjq5KTJH8YfBpV1bbrY2ur/x2HNyfnokaeycts/WdHqBqKQxMRBi',
     '$2a$10$OPjq5KTJH8YfBpV1bbrY2ur/x2HNyfnokaeycts/WdHqBqKQxMRBi'),
    ('Jane Smith', 'jane.smith@gmail.com',
     '$2a$10$OPjq5KTJH8YfBpV1bbrY2ur/x2HNyfnokaeycts/WdHqBqKQxMRBi',
     '$2a$10$OPjq5KTJH8YfBpV1bbrY2ur/x2HNyfnokaeycts/WdHqBqKQxMRBi'),
    ('Mike Smith', 'mike.smith@gmail.com',
     '$2a$10$OPjq5KTJH8YfBpV1bbrY2ur/x2HNyfnokaeycts/WdHqBqKQxMRBi',
     '$2a$10$OPjq5KTJH8YfBpV1bbrY2ur/x2HNyfnokaeycts/WdHqBqKQxMRBi');

-- Insert Tasks
INSERT INTO tasks (title, description, task_status, expiration_date)
VALUES
    ('Task 1', 'Description 1', 'NOT_STARTED', '2024-02-15 12:00:00'),
    ('Task 2', 'Description 2', 'IN_PROGRESS', '2024-02-20 14:00:00'),
    ('Task 3', 'Description 3', 'NOT_STARTED', '2024-02-26 16:00:00'),
    ('Task 4', 'Description 4', 'IN_PROGRESS', '2024-02-28 19:00:00');

-- Assign Tasks to Users
insert into users_tasks (task_id, user_id)
values (1, 1),
       (2, 2),
       (3, 3),
       (4, 3);

-- Assign Roles to Users
insert into users_roles (user_id, role)
values (1, 'ROLE_ADMIN'),
       (1, 'ROLE_USER'),
       (2, 'ROLE_USER');