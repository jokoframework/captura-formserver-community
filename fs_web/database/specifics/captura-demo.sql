-- Customizations updates the standard values

UPDATE sys.parameters set value = 'en' where id = 1000;
UPDATE sys.parameters set value = 'https://captura-forms.com/mf/' where id = 1002;
UPDATE sys.parameters set value = 'noreply@captura-forms.com' where id = 1003;
UPDATE sys.parameters set value = 'true' where id = 1004;
UPDATE sys.parameters set value = 'true' where id = 1015;--logout after an unexpected error
UPDATE sys.parameters set value = 'info@captura-forms.com' where id = 1019;
UPDATE sys.parameters set value = 'https://captura-forms.com:8443/mf/api/document/upload/file?handle={handle}', active = true where id = 1021;
