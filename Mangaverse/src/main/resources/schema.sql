DROP TABLE IF EXISTS manga;

CREATE TABLE IF NOT EXISTS manga (
    id SERIAL PRIMARY KEY,
    title varchar(50) NOT NULL,
    author varchar(50) NOT NULL,
    genre varchar(50) NOT NULL,
    description varchar(500) NOT NULL
)
