--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- Name: deposit(text, text, numeric); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION deposit(n text, p text, amt numeric) RETURNS numeric
    LANGUAGE plpgsql
    AS $$
DECLARE bal numeric;
BEGIN
    -- This holds a row lock until the end of the transaction.
    UPDATE account
      SET  balance = balance + amt;

    SELECT balance INTO STRICT bal
    FROM   account
    WHERE  num = n and pin = p;

    RETURN bal;
END;
$$;


--
-- Name: get_balance(text, text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION get_balance(n text, p text) RETURNS numeric
    LANGUAGE plpgsql
    AS $$
DECLARE bal numeric;
BEGIN
    SELECT balance INTO STRICT bal
    FROM    account
    WHERE   num = n and pin = p;

    RETURN bal;
END;
$$;


--
-- Name: verify_pin(text, text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION verify_pin(n text, p text) RETURNS boolean
    LANGUAGE plpgsql
    AS $$
DECLARE verified boolean;
BEGIN
    SELECT EXISTS(
	SELECT *
	FROM   account
	WHERE  num = n and pin = p)
    INTO STRICT verified;
    
    RETURN verified;
END;
$$;


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: account; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE account (
    id integer NOT NULL,
    name text NOT NULL,
    num text NOT NULL,
    pin text NOT NULL,
    balance numeric NOT NULL
);


--
-- Name: account_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE account_id_seq OWNED BY account.id;


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY account ALTER COLUMN id SET DEFAULT nextval('account_id_seq'::regclass);


--
-- Data for Name: account; Type: TABLE DATA; Schema: public; Owner: -
--

COPY account (id, name, num, pin, balance) FROM stdin;
1	Bob	1234	4321	100.00
\.


--
-- Name: account_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('account_id_seq', 2, true);


--
-- Name: account_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY account
    ADD CONSTRAINT account_pkey PRIMARY KEY (id);


--
-- Name: account_num_pin_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX account_num_pin_idx ON account USING btree (num, pin);


--
-- PostgreSQL database dump complete
--

