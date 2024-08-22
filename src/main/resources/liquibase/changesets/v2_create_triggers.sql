-- Logs the initial task status when a task is created, with the previous value set to NULL.
-- This helps track the status from task creation.
CREATE OR REPLACE FUNCTION log_task_creation_status()
    RETURNS TRIGGER AS
'
    BEGIN
        IF NEW.task_status IS DISTINCT FROM NULL THEN
            INSERT INTO task_history (task_id, previous_value, new_value, updated_at)
            VALUES (NEW.id, NULL, NEW.task_status, NOW());
        END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

-- Logs task status changes (old to new) when the status is updated.
-- Ensures task status transitions are properly recorded.
CREATE OR REPLACE FUNCTION log_task_status_change()
    RETURNS TRIGGER AS
'
    BEGIN
        IF TG_OP = ''UPDATE'' AND OLD.task_status IS DISTINCT FROM NEW.task_status THEN
            INSERT INTO task_history (task_id, previous_value, new_value, updated_at)
            VALUES (NEW.id, OLD.task_status, NEW.task_status, NOW());
        END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

-- Updates the 'is_resolved' field in task_comments when the task status is set to 'APPROVED'.
-- Ensures related comments are marked as resolved when the task is approved.
CREATE OR REPLACE FUNCTION update_task_comment_resolved()
    RETURNS TRIGGER AS
'
    BEGIN
        IF TG_OP = ''UPDATE'' AND NEW.task_status = ''APPROVED'' THEN
            UPDATE task_comments
            SET is_resolved = TRUE
            WHERE task_id = NEW.id;
        END IF;

        RETURN NEW;
    END;
' LANGUAGE plpgsql;

-- Trigger for task creation status
CREATE TRIGGER task_creation_status_trigger
    AFTER INSERT
    ON tasks
    FOR EACH ROW
EXECUTE FUNCTION log_task_creation_status();

-- Trigger for task status change
CREATE TRIGGER task_status_change_trigger
    AFTER UPDATE OF task_status
    ON tasks
    FOR EACH ROW
EXECUTE FUNCTION log_task_status_change();

-- Trigger for task status approved
CREATE TRIGGER task_status_approved_trigger
    AFTER UPDATE
    ON tasks
    FOR EACH ROW
EXECUTE FUNCTION update_task_comment_resolved();