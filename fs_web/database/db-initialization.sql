insert into sys.parameters(id,available,description,label, "type", "value") values (1000, true, 'default language', 'default language', 0,'en');

insert into i18n.languages(id, iso_language, name) values (nextval('i18n.seq_languages'), 'en', 'English US');
-- Also add Spanish
insert into i18n.languages(id, iso_language, name) values (nextval('i18n.seq_languages'), 'es', 'Spanish');



