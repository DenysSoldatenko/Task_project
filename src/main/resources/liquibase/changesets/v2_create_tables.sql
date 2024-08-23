CREATE TABLE IF NOT EXISTS users
(
    id               BIGSERIAL PRIMARY KEY,
    full_name        VARCHAR(255) NOT NULL,
    username         VARCHAR(255) NOT NULL UNIQUE,
    slug             VARCHAR(255) NOT NULL UNIQUE,
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
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_users_roles_roles FOREIGN KEY (role_id) REFERENCES roles (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS teams
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE,
    description TEXT         NULL,
    creator_id  BIGINT       NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_teams_creator FOREIGN KEY (creator_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS teams_users
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
    creator_id  BIGINT       NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_projects_creator FOREIGN KEY (creator_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS projects_teams
(
    team_id    BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    PRIMARY KEY (team_id, project_id),
    CONSTRAINT fk_teams_projects_team FOREIGN KEY (team_id) REFERENCES teams (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_teams_projects_project FOREIGN KEY (project_id) REFERENCES projects (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS tasks
(
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT       NOT NULL,
    team_id         BIGINT       NOT NULL,
    title           VARCHAR(255) NOT NULL,
    description     TEXT         NULL,
    task_status     VARCHAR(50)  NOT NULL DEFAULT 'ASSIGNED',
    priority        VARCHAR(50)  NOT NULL DEFAULT 'MEDIUM',
    assigned_to     BIGINT       NULL,
    assigned_by     BIGINT       NULL,
    created_at      TIMESTAMP    NOT NULL,
    expiration_date TIMESTAMP    NULL,
    approved_at     TIMESTAMP    NULL,
    CONSTRAINT fk_tasks_project FOREIGN KEY (project_id) REFERENCES projects (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_tasks_team FOREIGN KEY (team_id) REFERENCES teams (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_tasks_assigned_to FOREIGN KEY (assigned_to) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE NO ACTION,
    CONSTRAINT fk_tasks_assigned_by FOREIGN KEY (assigned_by) REFERENCES users (id)
        ON DELETE SET NULL ON UPDATE NO ACTION
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
    id          BIGSERIAL PRIMARY KEY,
    slug        VARCHAR(255) NOT NULL,
    task_id     BIGINT       NOT NULL,
    sender_id   BIGINT       NOT NULL,
    receiver_id BIGINT       NOT NULL,
    message     TEXT         NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_resolved BOOLEAN   DEFAULT FALSE,
    CONSTRAINT fk_task_comments_task FOREIGN KEY (task_id) REFERENCES tasks (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_task_comments_sender FOREIGN KEY (sender_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_task_comments_receiver FOREIGN KEY (receiver_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS task_history
(
    id             BIGSERIAL PRIMARY KEY,
    task_id        BIGINT NOT NULL,
    previous_value TEXT,
    new_value      TEXT,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_history_task FOREIGN KEY (task_id) REFERENCES tasks (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS achievements
(
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL UNIQUE,
    description TEXT         NOT NULL,
    image_url   VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS users_achievements
(
    user_id        BIGINT NOT NULL,
    achievement_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, achievement_id),
    CONSTRAINT fk_users_achievements_users FOREIGN KEY (user_id) REFERENCES users (id)
        ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_users_achievements_achievements FOREIGN KEY (achievement_id) REFERENCES achievements (id)
        ON DELETE CASCADE ON UPDATE NO ACTION
);
