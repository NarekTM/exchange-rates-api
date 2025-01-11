CREATE TABLE IF NOT EXISTS currency
(
    id         BIGINT,
    code       VARCHAR(3) NOT NULL,
    rates      JSONB      NOT NULL,
    created_on TIMESTAMP  NOT NULL DEFAULT NOW(),
    updated_on TIMESTAMP  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_currency__id PRIMARY KEY (id),
    CONSTRAINT uk_currency__code UNIQUE (code)
);

CREATE SEQUENCE IF NOT EXISTS currency_id_seq
    CACHE 10
    OWNED BY currency.id;

ALTER TABLE currency
    ALTER COLUMN id SET DEFAULT nextval('currency_id_seq')
