CREATE OR REPLACE FUNCTION log_task_status_change()
    RETURNS TRIGGER AS '
    BEGIN
        -- Write the status change only if it has actually changed
        IF OLD.task_status IS DISTINCT FROM NEW.task_status THEN
            INSERT INTO task_history (task_id, updated_by, previous_value, new_value, updated_at)
            VALUES (NEW.id, NEW.updated_by, OLD.task_status, NEW.task_status, NOW());
        END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER task_status_change_trigger
    AFTER UPDATE OF task_status ON tasks
    FOR EACH ROW
EXECUTE FUNCTION log_task_status_change();
