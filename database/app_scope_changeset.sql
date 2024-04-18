-- init
-- to be sure there's at least one app


-- changeset 01
alter table core.users add column default_application bigint;
alter table core.users add constraint "users_default_application" foreign key (default_application)
	references applications.applications (id);
update core.users set default_application = (select id from applications.applications order by id limit 1);
-- alter table core.users alter column default_application set not null;
-- alter table core.users alter column default_application drop not null;

-- changeset 02
ALTER TABLE applications.application_users DROP CONSTRAINT fk181ba59cea68119;
ALTER TABLE applications.application_users DROP CONSTRAINT fk181ba59f2d988b6;
ALTER TABLE applications.application_users
  ADD CONSTRAINT fk_applications_users_applications FOREIGN KEY (application_id)
      REFERENCES applications.applications (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE applications.application_users
  ADD CONSTRAINT fk_applications_users_users FOREIGN KEY (user_id)
      REFERENCES core.users (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
	  
-- changeset 03
ALTER TABLE core.authorizations ADD COLUMN application_id BIGINT;
ALTER TABLE core.authorizations 
ADD CONSTRAINT fk_authorizations_applications FOREIGN KEY(application_id)
	REFERENCES applications.applications (id) MATCH SIMPLE
	ON UPDATE NO ACTION ON DELETE NO ACTION;
--UPDATE core.authorizations SET application_id=(select id from applications.applications order by id limit 1);

ALTER TABLE core.authorizations ADD COLUMN editable boolean;
UPDATE core.authorizations SET editable = false;
ALTER TABLE core.authorizations ALTER COLUMN editable SET NOT NULL;
ALTER TABLE core.authorizations ALTER COLUMN editable SET DEFAULT true;

-- changeset 04
ALTER TABLE core.groups ADD COLUMN application_id BIGINT;
ALTER TABLE core.groups 
ADD CONSTRAINT fk_groups_applications FOREIGN KEY(application_id)
	REFERENCES applications.applications (id) MATCH SIMPLE
	ON UPDATE NO ACTION ON DELETE NO ACTION;
	
UPDATE core.groups SET application_id=(select id from applications.applications order by id limit 1);

-- changeset 05
CREATE TABLE applications.applications_auth
(
  authorization_name bigint NOT NULL,
  application_id bigint NOT NULL,
  authorizable_entity_id bigint NOT NULL,
  CONSTRAINT projects_auth_pkey PRIMARY KEY (authorization_name , authorizable_entity_id , application_id),
  CONSTRAINT fk_applications_auth_authorizations FOREIGN KEY (authorization_name)
      REFERENCES core.authorizations (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_applications_auth_authorizable_entities FOREIGN KEY (authorizable_entity_id)
      REFERENCES core.authorizable_entities (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_applications_auth_applications FOREIGN KEY (application_id)
      REFERENCES applications.applications (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);

--ALTER TABLE applications.applications_auth
--  OWNER TO postgres;
  
-- changeset 06
ALTER TABLE forms.pools ADD COLUMN application_id BIGINT;
ALTER TABLE forms.pools 
ADD CONSTRAINT fk_pools_applications FOREIGN KEY(application_id)
	REFERENCES applications.applications (id) MATCH SIMPLE
	ON UPDATE NO ACTION ON DELETE NO ACTION;
	
UPDATE forms.pools SET application_id=(select id from applications.applications order by id limit 1);

--DELETE FROM core.role_grants_authorization;

--INSERT INTO core.role_grants_authorization(grants_authorization_id, role_id) VALUES 
--(1, 5),
--(2, 5),
--(3, 5),
--(4, 5);
