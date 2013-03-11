CREATE TABLE cyclist_type
(
  id bigint NOT NULL,
  name character varying,
  title character varying,
  CONSTRAINT cyclist_type_pkey PRIMARY KEY (id )
)
WITH (
  OIDS=FALSE
);

CREATE SEQUENCE cyclist_type_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;