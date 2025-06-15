CREATE TABLE cities (
id BIGSERIAL PRIMARY KEY,
name VARCHAR(255) NOT NULL,
state VARCHAR(2) NOT NULL
);

CREATE TABLE articles (
    id BIGSERIAL PRIMARY KEY,
    source_id VARCHAR(255),
    source_name VARCHAR(255),
    author VARCHAR(255),
    title TEXT NOT NULL,
    description TEXT,
    url VARCHAR(2048),
    url_to_image VARCHAR(2048),
    published_at TIMESTAMP WITH TIME ZONE,
    content TEXT,
    is_local BOOLEAN,
    city_name VARCHAR(255),
    state_abbreviation VARCHAR(2)
);

CREATE INDEX idx_articles_city_id ON articles(city_name);
CREATE INDEX idx_cities_name ON cities(name);