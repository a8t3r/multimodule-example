DO
$func$
    BEGIN
        -- change hash if function body changes
        if (SELECT md5(routine_definition) != '6e1171fb9ae87f21de3b7116dc7012b8' FROM information_schema.routines WHERE routine_name = 'notify_mbeans') then
            CREATE OR REPLACE FUNCTION notify_mbeans() RETURNS TRIGGER AS $$
            DECLARE
                old_value text := case when old is null then 'null' else old.value end;
            BEGIN
                PERFORM pg_notify('mbeans', json_build_object('name', new.name, 'property', new.property, 'value', new.value)::text);
                IF (TG_OP = 'INSERT' or old.value != new.value) THEN
                    EXECUTE 'insert into mbeans_audit_log(session_user_name, client_addr, application_name, name, property, old_value, new_value)' ||
                            ' values (''' || session_user::text || ''', inet_client_addr(), current_setting(''application_name''), ''' || new.name || ''',''' || new.property || ''',''' || old_value || ''',''' || new.value || ''')';
                END IF;
                RETURN new;
            END;
            $$ LANGUAGE plpgsql;

            DROP TRIGGER IF EXISTS mbeans_trigger ON mbeans;

            CREATE TRIGGER mbeans_trigger
                AFTER INSERT OR UPDATE
                ON mbeans
                FOR EACH ROW
            EXECUTE PROCEDURE notify_mbeans();
        end if;
END $func$;