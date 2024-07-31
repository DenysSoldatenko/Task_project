CREATE TABLE IF NOT EXISTS users
(
    id               BIGSERIAL PRIMARY KEY,
    full_name        VARCHAR(255) NOT NULL,
    username         VARCHAR(255) NOT NULL UNIQUE,
    password         VARCHAR(255) NOT NULL,
    confirm_password VARCHAR(255) NOT NULL,
    CONSTRAINT chk_password_length CHECK (LENGTH(password) >= 6),
    CONSTRAINT chk_confirm_password_length CHECK (LENGTH(confirm_password) >= 6)
);

CREATE TABLE IF NOT EXISTS roles
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS role_hierarchy
(
    id          BIGSERIAL PRIMARY KEY,
    higher_role BIGINT NOT NULL,
    lower_role  BIGINT NOT NULL,
    CONSTRAINT fk_higher_role FOREIGN KEY (higher_role) REFERENCES roles (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_lower_role FOREIGN KEY (lower_role) REFERENCES roles (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS users_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_users_roles_users FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS teams
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT         NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users_teams
(
    user_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, team_id),
    CONSTRAINT fk_users_teams_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_users_teams_team FOREIGN KEY (team_id) REFERENCES teams (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_users_teams_role FOREIGN KEY (role_id) REFERENCES roles (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS projects
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT         NULL,
    team_id     BIGINT       NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_projects_team FOREIGN KEY (team_id) REFERENCES teams (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS tasks
(
    id             BIGSERIAL PRIMARY KEY,
    project_id     BIGINT       NOT NULL,
    parent_task_id BIGINT       NULL,
    team_id        BIGINT       NOT NULL,
    title          VARCHAR(255) NOT NULL,
    description    TEXT         NULL,
    task_status    VARCHAR(255) NOT NULL DEFAULT 'Assigned',
    priority       VARCHAR(50)  NOT NULL DEFAULT 'Medium',
    created_at     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_tasks_parent_task FOREIGN KEY (parent_task_id) REFERENCES tasks (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_tasks_team FOREIGN KEY (team_id) REFERENCES teams (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS users_tasks
(
    user_id       BIGINT   NOT NULL,
    task_id       BIGINT   NOT NULL,
    assigned_by   BIGINT   NULL,
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    time_spent    INTERVAL NULL,
    PRIMARY KEY (user_id, task_id),
    CONSTRAINT fk_users_tasks_users FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_users_tasks_tasks FOREIGN KEY (task_id) REFERENCES tasks (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_users_tasks_assigned_by FOREIGN KEY (assigned_by) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS task_dependencies
(
    id           BIGSERIAL PRIMARY KEY,
    task_id      BIGINT NOT NULL,
    dependent_on BIGINT NOT NULL,
    CONSTRAINT fk_task_dependencies_task FOREIGN KEY (task_id) REFERENCES tasks (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_task_dependencies_dependent FOREIGN KEY (dependent_on) REFERENCES tasks (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS tasks_images
(
    task_id BIGINT       NOT NULL,
    image   VARCHAR(255) NOT NULL,
    CONSTRAINT fk_tasks_images_tasks FOREIGN KEY (task_id) REFERENCES tasks (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS task_comments
(
    id         BIGSERIAL PRIMARY KEY,
    task_id    BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    comment    TEXT   NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_comments_task FOREIGN KEY (task_id) REFERENCES tasks (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_task_comments_user FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS task_history
(
    id             BIGSERIAL PRIMARY KEY,
    task_id        BIGINT      NOT NULL,
    updated_by     BIGINT      NOT NULL,
    update_type    VARCHAR(50) NOT NULL,
    previous_value TEXT,
    new_value      TEXT,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_history_task FOREIGN KEY (task_id) REFERENCES tasks (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_task_history_user FOREIGN KEY (updated_by) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);
