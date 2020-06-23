--
-- CzechIdM 9 Flyway script 
-- BCV solutions s.r.o.
--
-- Password filter definition


CREATE TABLE acc_password_filter
(
  id bytea NOT NULL,
  created timestamp without time zone NOT NULL,
  creator character varying(255) NOT NULL,
  creator_id bytea,
  modified timestamp without time zone,
  modifier character varying(255),
  modifier_id bytea,
  original_creator character varying(255),
  original_creator_id bytea,
  original_modifier character varying(255),
  original_modifier_id bytea,
  realm_id bytea,
  transaction_id bytea,
  code character varying(255),
  description character varying(2000),
  disabled boolean NOT NULL,
  change_in_idm boolean NOT NULL,
  timeout integer,
  transformation_script text,
  CONSTRAINT acc_password_filter_pkey PRIMARY KEY (id),
  CONSTRAINT ux_acc_pass_filter_code UNIQUE (code)
);

CREATE TABLE acc_password_filter_a
(
  id bytea NOT NULL,
  rev bigint NOT NULL,
  revtype smallint,
  created timestamp without time zone,
  created_m boolean,
  creator character varying(255),
  creator_m boolean,
  creator_id bytea,
  creator_id_m boolean,
  modified timestamp without time zone,
  modified_m boolean,
  modifier character varying(255),
  modifier_m boolean,
  modifier_id bytea,
  modifier_id_m boolean,
  original_creator character varying(255),
  original_creator_m boolean,
  original_creator_id bytea,
  original_creator_id_m boolean,
  original_modifier character varying(255),
  original_modifier_m boolean,
  original_modifier_id bytea,
  original_modifier_id_m boolean,
  realm_id bytea,
  realm_id_m boolean,
  transaction_id bytea,
  transaction_id_m boolean,
  code character varying(255),
  code_m boolean,
  description character varying(2000),
  description_m boolean,
  disabled boolean,
  disabled_m boolean,
  change_in_idm boolean,
  change_in_idm_m boolean,
  timeout integer,
  timeout_m boolean,
  transformation_script text,
  transformation_script_m boolean,
  CONSTRAINT acc_password_filter_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_acc_password_filter_rev FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE TABLE acc_password_filter_system
(
  id bytea NOT NULL,
  created timestamp without time zone NOT NULL,
  creator character varying(255) NOT NULL,
  creator_id bytea,
  modified timestamp without time zone,
  modifier character varying(255),
  modifier_id bytea,
  original_creator character varying(255),
  original_creator_id bytea,
  original_modifier character varying(255),
  original_modifier_id bytea,
  realm_id bytea,
  transaction_id bytea,
  system_id bytea NOT NULL,
  password_filter_id bytea NOT NULL,
  CONSTRAINT acc_password_filter_sys_pkey PRIMARY KEY (id),
  CONSTRAINT ux_acc_pass_fil_id_sys_id UNIQUE (system_id,password_filter_id)
);
 
CREATE INDEX idx_sys_system_id
  ON acc_password_filter_system
  USING btree
  (system_id);

CREATE INDEX idx_acc_password_filter_id
  ON acc_password_filter_system
  USING btree
  (password_filter_id);

CREATE TABLE acc_password_filter_system_a
(
  id bytea NOT NULL,
  rev bigint NOT NULL,
  revtype smallint,
  created timestamp without time zone,
  created_m boolean,
  creator character varying(255),
  creator_m boolean,
  creator_id bytea,
  creator_id_m boolean,
  modified timestamp without time zone,
  modified_m boolean,
  modifier character varying(255),
  modifier_m boolean,
  modifier_id bytea,
  modifier_id_m boolean,
  original_creator character varying(255),
  original_creator_m boolean,
  original_creator_id bytea,
  original_creator_id_m boolean,
  original_modifier character varying(255),
  original_modifier_m boolean,
  original_modifier_id bytea,
  original_modifier_id_m boolean,
  realm_id bytea,
  realm_id_m boolean,
  transaction_id bytea,
  transaction_id_m boolean,
  system_id bytea,
  system_m boolean,
  password_filter_id bytea,
  password_filter_m boolean,
  CONSTRAINT acc_password_filter_sys_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_acc_password_filter_sys_rev FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
