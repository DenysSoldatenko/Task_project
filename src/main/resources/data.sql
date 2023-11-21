insert into users (full_name, username, password)
values ('Alice Johnson', 'alice@example.com', '$2a$12$1Ulh5V3H4XrT9vbaYiDOUOTc0Bq98d/vSprDyaY39a2mwuv3i9.ei'),
       ('Bob Brown', 'bob@example.com', '$2a$12$1Ulh5V3H4XrT9vbaYiDOUOTc0Bq98d/vSprDyaY39a2mwuv3i9.ei');

insert into tasks (title, description, status, expiration_date)
values ('Go grocery shopping', 'Milk, bread, eggs', 'TODO', '2024-02-08 18:00:00'),
       ('Write report', 'Quarterly financial report', 'IN_PROGRESS', '2024-02-15 12:00:00'),
       ('Exercise', '30 minutes cardio', 'TODO', null),
       ('Send email', 'To client regarding project status', 'IN_PROGRESS', '2024-02-10 09:00:00');

insert into users_tasks (task_id, user_id)
values (1, 1),
       (2, 1),
       (3, 2),
       (4, 2);

insert into users_roles (user_id, role)
values (1, 'ROLE_USER'),
       (1, 'ROLE_ADMIN'),
       (2, 'ROLE_USER');
