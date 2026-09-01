--
-- PostgreSQL database dump UPDATED BY VLD on 2023-07-21 to create only used tables in GUM
--

-- Dumped from database version 10.19 (Ubuntu 10.19-0ubuntu0.18.04.1)
-- Dumped by pg_dump version 10.19 (Ubuntu 10.19-0ubuntu0.18.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner:
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner:
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET default_tablespace = '';

SET default_with_oids = false;



--
-- Name: usr_users; Type: TABLE; Schema: public; Owner: gazelle
--

CREATE TABLE public.usr_users (
                                  id integer NOT NULL,
                                  last_changed timestamp with time zone,
                                  last_modifier_id character varying(255),
                                  activated boolean,
                                  activation_code character varying(255),
                                  change_password_code character varying(255),
                                  creation_date timestamp without time zone,
                                  email character varying(255) NOT NULL,
                                  firstname character varying(128),
                                  last_login timestamp without time zone,
                                  lastname character varying(128),
                                  counter_logins integer,
                                  password character varying(128) NOT NULL,
                                  username character varying(16) NOT NULL,
                                  institution_id integer NOT NULL
);


ALTER TABLE public.usr_users OWNER TO gazelle;

--
-- Name: usr_users_id_seq; Type: SEQUENCE; Schema: public; Owner: gazelle
--

CREATE SEQUENCE public.usr_users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.usr_users_id_seq OWNER TO gazelle;

--
-- Name: usr_role; Type: TABLE; Schema: public; Owner: gazelle
--

CREATE TABLE public.usr_role (
                                 id integer NOT NULL,
                                 last_changed timestamp with time zone,
                                 last_modifier_id character varying(255),
                                 description character varying(1024),
                                 name character varying(64) NOT NULL
);


ALTER TABLE public.usr_role OWNER TO gazelle;

--
-- Name: usr_role_id_seq; Type: SEQUENCE; Schema: public; Owner: gazelle
--

CREATE SEQUENCE public.usr_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.usr_role_id_seq OWNER TO gazelle;



--
-- Name: usr_user_role; Type: TABLE; Schema: public; Owner: gazelle
--

CREATE TABLE public.usr_user_role (
                                      user_id integer NOT NULL,
                                      role_id integer NOT NULL
);


ALTER TABLE public.usr_user_role OWNER TO gazelle;

--
-- Name: usr_institution; Type: TABLE; Schema: public; Owner: gazelle
--

CREATE TABLE public.usr_institution (
                                        id integer NOT NULL,
                                        last_changed timestamp with time zone,
                                        last_modifier_id character varying(255),
                                        activated boolean,
                                        integration_statements_repository_url character varying(512),
                                        keyword character varying(16) NOT NULL,
                                        name character varying(255) NOT NULL,
                                        note character varying(1024),
                                        url character varying(512) NOT NULL,
                                        institution_type_id integer NOT NULL,
                                        mailing_address_id integer
);

ALTER TABLE public.usr_institution OWNER TO gazelle;

--
-- Name: usr_delegated_organization; Type: TABLE; Schema: public; Owner: gazelle
--

create table public.usr_delegated_organization (
    external_id    varchar(255),
    idp_id         varchar(255),
    organization_id int not null,
    primary key (organization_id)
);

ALTER TABLE public.usr_delegated_organization OWNER TO gazelle;

--
-- Name: tm_user_preferences; Type: TABLE; Schema: public; Owner: gazelle
--

CREATE TABLE public.tm_user_preferences (
                                            id integer NOT NULL,
                                            last_changed timestamp with time zone,
                                            last_modifier_id character varying(255),
                                            is_email_displayed boolean,
                                            is_tooltips_displayed boolean,
                                            number_of_results_per_page_id integer,
                                            connectathon_table character varying(255),
                                            email_notification boolean,
                                            show_sequence_diagram boolean,
                                            skype character varying(255),
                                            spoken_languages character varying(255),
                                            username character varying(255) NOT NULL,
                                            selected_testing_session_id integer,
                                            userphoto_id integer
);


ALTER TABLE public.tm_user_preferences OWNER TO gazelle;

--
-- Name: tm_user_preferences_id_seq; Type: SEQUENCE; Schema: public; Owner: gazelle
--

CREATE SEQUENCE public.tm_user_preferences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tm_user_preferences_id_seq OWNER TO gazelle;

--
-- Name: tm_user_photo; Type: TABLE; Schema: public; Owner: gazelle
--

CREATE TABLE public.tm_user_photo (
                                      id integer NOT NULL,
                                      photo_bytes bytea
);


ALTER TABLE public.tm_user_photo OWNER TO gazelle;

--
-- Name: tm_user_photo_id_seq; Type: SEQUENCE; Schema: public; Owner: gazelle
--

CREATE SEQUENCE public.tm_user_photo_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tm_user_photo_id_seq OWNER TO gazelle;


--
-- Name: usr_institution uk_an3krxejvje497n5bqhw3pyew; Type: CONSTRAINT; Schema: public; Owner: gazelle
--

ALTER TABLE ONLY public.usr_institution
    ADD CONSTRAINT uk_an3krxejvje497n5bqhw3pyew UNIQUE (name);

--
-- Name: usr_institution uk_jqjrsonmmlfnnp3ta4aaqu3ut; Type: CONSTRAINT; Schema: public; Owner: gazelle
--

ALTER TABLE ONLY public.usr_institution
    ADD CONSTRAINT uk_jqjrsonmmlfnnp3ta4aaqu3ut UNIQUE (keyword);

--
-- Name: usr_institution usr_institution_pkey; Type: CONSTRAINT; Schema: public; Owner: gazelle
--

ALTER TABLE ONLY public.usr_institution
    ADD CONSTRAINT usr_institution_pkey PRIMARY KEY (id);

--
-- Name: usr_users fk_nyqv47jtq7254b9y0kq55g0p4; Type: FK CONSTRAINT; Schema: public; Owner: gazelle
--

ALTER TABLE ONLY public.usr_users
    ADD CONSTRAINT fk_nyqv47jtq7254b9y0kq55g0p4 FOREIGN KEY (institution_id) REFERENCES public.usr_institution(id);

--
-- Name: usr_users uk_g0jloiasku8a7gat4lu7866r6; Type: CONSTRAINT; Schema: public; Owner: gazelle
--

ALTER TABLE ONLY public.usr_users
    ADD CONSTRAINT uk_g0jloiasku8a7gat4lu7866r6 UNIQUE (email);

--
-- Name: tm_user_preferences tm_user_preferences_pkey; Type: CONSTRAINT; Schema: public; Owner: gazelle
--

ALTER TABLE ONLY public.tm_user_preferences
    ADD CONSTRAINT tm_user_preferences_pkey PRIMARY KEY (id);

--
-- Name: tm_user_photo tm_user_photo_pkey; Type: CONSTRAINT; Schema: public; Owner: gazelle
--

ALTER TABLE ONLY public.tm_user_photo
    ADD CONSTRAINT tm_user_photo_pkey PRIMARY KEY (id);


--
-- Name: tm_user_preferences fk_ks4okv9ariahgb5n0k3d3esw9; Type: FK CONSTRAINT; Schema: public; Owner: gazelle
--

ALTER TABLE ONLY public.tm_user_preferences
    ADD CONSTRAINT fk_ks4okv9ariahgb5n0k3d3esw9 FOREIGN KEY (userphoto_id) REFERENCES public.tm_user_photo(id);

--
-- Name: usr_delegated_organization FK1n0aw2s9ebayp18mco1hr083; Type: FK CONSTRAINT; Schema: public; Owner: gazelle
--
alter table if exists usr_delegated_organization
    add constraint FK1n0aw2s9ebayp18mco1hr083 foreign key (organization_id) references usr_institution;