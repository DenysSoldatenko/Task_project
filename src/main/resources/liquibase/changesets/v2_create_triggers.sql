CREATE OR REPLACE FUNCTION log_task_creation_status()
    RETURNS TRIGGER AS '
    BEGIN
        -- Log the task creation status change
        -- Assuming the initial status is "assigned" or any other default value
        -- This can be customized if needed
        IF NEW.task_status IS DISTINCT FROM NULL THEN
            INSERT INTO task_history (task_id, previous_value, new_value, updated_at)
            VALUES (NEW.id, NULL, NEW.task_status, NOW());
        END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION log_task_status_change()
    RETURNS TRIGGER AS '
    BEGIN
        -- If it''s an update and the task status has changed, log the change
        IF TG_OP = ''UPDATE'' AND OLD.task_status IS DISTINCT FROM NEW.task_status THEN
            INSERT INTO task_history (task_id, previous_value, new_value, updated_at)
            VALUES (NEW.id, OLD.task_status, NEW.task_status, NOW());
        END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

-- Trigger for task creation status
CREATE TRIGGER task_creation_status_trigger
    AFTER INSERT ON tasks
    FOR EACH ROW
EXECUTE FUNCTION log_task_creation_status();

-- Trigger for task status change
CREATE TRIGGER task_status_change_trigger
    AFTER UPDATE OF task_status ON tasks
    FOR EACH ROW
EXECUTE FUNCTION log_task_status_change();
