-- This is an Application parameter. It defines whether to send or not an e-mail with login information to a new user 
-- insert into applications.parameters(id, active, deleted, parameter_id, description, label, type, value, application_id)
-- values(nextval('applications.seq_applications_parameters'), true, false, 5000, 'send mail to new user', 'mail new user', 0, 'true', 1);

-- This parameter is used to overwrite the default values set in business-applicationContext
-- INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
-- VALUES (1001, true, false, 'smtp server', 'smtp server', 0, '{"host":"theMailServer.com", "port":"108"}'); 

DELETE FROM sys.parameters;

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES(1000, true, false, 'Default system language','default language',0,'en');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1002, true, false, 'This is used to create external URIs', 'CONTEXT_PATH', 0, 'http://localhost:8080/mobileforms-web/');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1003, true, false, 'This is used to create external URIs', 'SYSTEM_MAIL_ADDRESS', 0, 'sodep.mf@gmail.com');  

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1004, true, false, 'This is used to disable or enable the user registration (false for enable registration)', 'SYS_REGISTRATION_DISABLED', 2, 'false');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1005, true, false, 'Number of data to show during the import of a CSV file', 'SYS_DATAIMPORT_ROWSTOSHOW', 1, '10');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1006, true, false, 'Max Number of rows to send on each http iteration to download data ', 'SYS_SYNCHRONIZATION_ROWSPERITERATION', 1, '100');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1008, true, false, 'Create a test application on startup', 'CREATE_TEST_APP', 2, 'true');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1009, true, false, 'Default role assigned to an application owner', 'DEFAULT_ROLE_APP_OWNER', 0, 'ROLE_APP_OWNER');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1010, true, false, 'Default role assigned to a project owner', 'DEFAULT_ROLE_PROJECT_OWNER', 0, 'ROLE_PROJECT_OWNER');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1011, true, false, 'Default role assigned to a form owner', 'DEFAULT_ROLE_FORM_OWNER', 0, 'ROLE_FORM_OWNER');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1012, true, false, 'Default role assigned to a pool owner', 'DEFAULT_ROLE_POOL_OWNER', 0, 'ROLE_POOL_OWNER');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1013, true, false, 'Default role assigned to a member of an app', 'DEFAULT_ROLE_APP_MEMBER', 0, 'ROLE_APP_MEMBER');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1014, true, false, 'Maximum Attempts to send a Email', 'MAX_ATTEMPTS_EMAIL_SEND', 1, 2);

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1015, true, false, 'Logout after an unexpected error', 'LOGOUT_AFTER_ERROR', 2, 'false');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1016, true, false, 'Whether to send the activation mail or not', 'SEND_ACTIVATION_MAIL_AFTER_REGISTRATION', 2, 'true');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1017, true, false, 'Max number of rows on a rest API that query lookup tables', 'REST_LOOKUP_DATA_MAXROWS', 1, '1000');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1018, true, false, 'The device''s polling time in seconds', 'DEVICE_POLLING_TIME_IN_SECONDS', 1, '1800');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1019, true, false, 'Email to which a notification will be sent when a user registers', 'REGISTRATION_NOTIFICATION_MAIL', 0, 'sodep.mf@gmail.com');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1020, true, false, 'A user with authorization to see the application''s settings will see "the license is about to expire", N days before the expiration date. N is defined by this parameter', 
'ABOUT_TO_EXPIRE_NOTIFICATION', 1, '15');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1021, false, false, 'URL for uploading files', 'UPLOAD_URL', 0, 'http://host/mf/api/upload');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1022, true, false, 'Allows to enable or disable error mail notifications, i.e. sending an email when a document was not uploaded.', 'SYS_NOTIFICATION_ERROR_DISABLED', 2, 'false');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1023, true, false, 'Support email address. Used by the system to notify events inside the application', 'SYS_NOTIFICATION_SUPPORT_MAIL_ADDRESS', 0, 'soporte@captura-forms.com');

INSERT INTO sys.parameters(id, active, deleted, description, label, type, value)
VALUES (1024, true, false, 'Installation id (incremented every time liquibase is run) ', 'SYS_DEPLOY', 0, '0');  
  

