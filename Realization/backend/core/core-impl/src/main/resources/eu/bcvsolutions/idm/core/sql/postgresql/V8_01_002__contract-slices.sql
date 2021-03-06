--
-- CzechIdM 8 Flyway script 
-- BCV solutions s.r.o.
--
-- Contract slices

CREATE TABLE idm_con_slice_form_value (
	id bytea NOT NULL,
	created timestamp NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id bytea NULL,
	modified timestamp NULL,
	modifier varchar(255) NULL,
	modifier_id bytea NULL,
	original_creator varchar(255) NULL,
	original_creator_id bytea NULL,
	original_modifier varchar(255) NULL,
	original_modifier_id bytea NULL,
	realm_id bytea NULL,
	transaction_id bytea NULL,
	boolean_value bool NULL,
	byte_value bytea NULL,
	confidential bool NOT NULL,
	date_value timestamp NULL,
	double_value numeric(38,4) NULL,
	long_value int8 NULL,
	persistent_type varchar(45) NOT NULL,
	seq int2 NULL,
	short_text_value varchar(2000) NULL,
	string_value text NULL,
	uuid_value bytea NULL,
	attribute_id bytea NOT NULL,
	owner_id bytea NOT NULL,
	CONSTRAINT idm_con_slice_form_value_pkey PRIMARY KEY (id),
	CONSTRAINT idm_con_slice_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_con_slice_form_a ON idm_con_slice_form_value USING btree (owner_id) ;
CREATE INDEX idx_idm_con_slice_form_a_def ON idm_con_slice_form_value USING btree (attribute_id) ;
CREATE INDEX idx_idm_con_slice_form_stxt ON idm_con_slice_form_value USING btree (short_text_value) ;
CREATE INDEX idx_idm_con_slice_form_uuid ON idm_con_slice_form_value USING btree (uuid_value) ;


CREATE TABLE idm_con_slice_form_value_a (
	id bytea NOT NULL,
	rev int8 NOT NULL,
	revtype int2 NULL,
	created timestamp NULL,
	created_m bool NULL,
	creator varchar(255) NULL,
	creator_m bool NULL,
	creator_id bytea NULL,
	creator_id_m bool NULL,
	modifier varchar(255) NULL,
	modifier_m bool NULL,
	modifier_id bytea NULL,
	modifier_id_m bool NULL,
	original_creator varchar(255) NULL,
	original_creator_m bool NULL,
	original_creator_id bytea NULL,
	original_creator_id_m bool NULL,
	original_modifier varchar(255) NULL,
	original_modifier_m bool NULL,
	original_modifier_id bytea NULL,
	original_modifier_id_m bool NULL,
	realm_id bytea NULL,
	realm_id_m bool NULL,
	transaction_id bytea NULL,
	transaction_id_m bool NULL,
	boolean_value bool NULL,
	boolean_value_m bool NULL,
	byte_value bytea NULL,
	byte_value_m bool NULL,
	confidential bool NULL,
	confidential_m bool NULL,
	date_value timestamp NULL,
	date_value_m bool NULL,
	double_value numeric(38,4) NULL,
	double_value_m bool NULL,
	long_value int8 NULL,
	long_value_m bool NULL,
	persistent_type varchar(45) NULL,
	persistent_type_m bool NULL,
	seq int2 NULL,
	seq_m bool NULL,
	short_text_value varchar(2000) NULL,
	short_text_value_m bool NULL,
	string_value text NULL,
	string_value_m bool NULL,
	uuid_value bytea NULL,
	uuid_value_m bool NULL,
	attribute_id bytea NULL,
	form_attribute_m bool NULL,
	owner_id bytea NULL,
	owner_m bool NULL,
	CONSTRAINT idm_con_slice_form_value_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_pq67ujqv1s2xmdkl80j36dvhp FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_contract_slice (
	id bytea NOT NULL,
	created timestamp NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id bytea NULL,
	modified timestamp NULL,
	modifier varchar(255) NULL,
	modifier_id bytea NULL,
	original_creator varchar(255) NULL,
	original_creator_id bytea NULL,
	original_modifier varchar(255) NULL,
	original_modifier_id bytea NULL,
	realm_id bytea NULL,
	transaction_id bytea NULL,
	code varchar(255) NULL,
	contract_code varchar(255) NULL,
	contract_valid_from date NULL,
	contract_valid_till date NULL,
	description varchar(2000) NULL,
	disabled bool NOT NULL,
	externe bool NOT NULL,
	main bool NOT NULL,
	"position" varchar(255) NULL,
	state varchar(45) NULL,
	using_as_contract bool NOT NULL,
	valid_from date NULL,
	valid_till date NULL,
	identity_id bytea NOT NULL,
	parent_contract_id bytea NULL,
	work_position_id bytea NULL,
	CONSTRAINT idm_contract_slice_pkey PRIMARY KEY (id)
);


CREATE TABLE idm_contract_slice_a (
	id bytea NOT NULL,
	rev int8 NOT NULL,
	revtype int2 NULL,
	created timestamp NULL,
	created_m bool NULL,
	creator varchar(255) NULL,
	creator_m bool NULL,
	creator_id bytea NULL,
	creator_id_m bool NULL,
	modifier varchar(255) NULL,
	modifier_m bool NULL,
	modifier_id bytea NULL,
	modifier_id_m bool NULL,
	original_creator varchar(255) NULL,
	original_creator_m bool NULL,
	original_creator_id bytea NULL,
	original_creator_id_m bool NULL,
	original_modifier varchar(255) NULL,
	original_modifier_m bool NULL,
	original_modifier_id bytea NULL,
	original_modifier_id_m bool NULL,
	realm_id bytea NULL,
	realm_id_m bool NULL,
	transaction_id bytea NULL,
	transaction_id_m bool NULL,
	code varchar(255) NULL,
	code_m bool NULL,
	contract_code varchar(255) NULL,
	contract_code_m bool NULL,
	contract_valid_from date NULL,
	contract_valid_from_m bool NULL,
	contract_valid_till date NULL,
	contract_valid_till_m bool NULL,
	description varchar(2000) NULL,
	description_m bool NULL,
	disabled bool NULL,
	disabled_m bool NULL,
	externe bool NULL,
	externe_m bool NULL,
	main bool NULL,
	main_m bool NULL,
	"position" varchar(255) NULL,
	position_m bool NULL,
	state varchar(45) NULL,
	state_m bool NULL,
	using_as_contract bool NULL,
	using_as_contract_m bool NULL,
	valid_from date NULL,
	valid_from_m bool NULL,
	valid_till date NULL,
	valid_till_m bool NULL,
	identity_id bytea NULL,
	identity_m bool NULL,
	parent_contract_id bytea NULL,
	parent_contract_m bool NULL,
	work_position_id bytea NULL,
	work_position_m bool NULL,
	CONSTRAINT idm_contract_slice_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_fdq20kiyy3x2ly1meht7exxd1 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);


CREATE TABLE idm_contract_slice_guarantee (
	id bytea NOT NULL,
	created timestamp NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id bytea NULL,
	modified timestamp NULL,
	modifier varchar(255) NULL,
	modifier_id bytea NULL,
	original_creator varchar(255) NULL,
	original_creator_id bytea NULL,
	original_modifier varchar(255) NULL,
	original_modifier_id bytea NULL,
	realm_id bytea NULL,
	transaction_id bytea NULL,
	contract_slice_id bytea NOT NULL,
	guarantee_id bytea NOT NULL,
	CONSTRAINT idm_contract_slice_guarantee_pkey PRIMARY KEY (id)
);
CREATE INDEX idm_contract_slice_guar_contr ON idm_contract_slice_guarantee USING btree (contract_slice_id) ;
CREATE INDEX idx_contract_slice_guar_idnt ON idm_contract_slice_guarantee USING btree (guarantee_id) ;


CREATE TABLE idm_contract_slice_guarantee_a (
	id bytea NOT NULL,
	rev int8 NOT NULL,
	revtype int2 NULL,
	created timestamp NULL,
	created_m bool NULL,
	creator varchar(255) NULL,
	creator_m bool NULL,
	creator_id bytea NULL,
	creator_id_m bool NULL,
	modifier varchar(255) NULL,
	modifier_m bool NULL,
	modifier_id bytea NULL,
	modifier_id_m bool NULL,
	original_creator varchar(255) NULL,
	original_creator_m bool NULL,
	original_creator_id bytea NULL,
	original_creator_id_m bool NULL,
	original_modifier varchar(255) NULL,
	original_modifier_m bool NULL,
	original_modifier_id bytea NULL,
	original_modifier_id_m bool NULL,
	realm_id bytea NULL,
	realm_id_m bool NULL,
	transaction_id bytea NULL,
	transaction_id_m bool NULL,
	contract_slice_id bytea NULL,
	contract_slice_m bool NULL,
	guarantee_id bytea NULL,
	guarantee_m bool NULL,
	CONSTRAINT idm_contract_slice_guarantee_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk_nbb01g3t4g9o0hlcxxq7e75ci FOREIGN KEY (rev) REFERENCES idm_audit(id)
);
