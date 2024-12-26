DROP TABLE IF EXISTS manga;

CREATE TABLE IF NOT EXISTS manga (
    id SERIAL PRIMARY KEY,
    title varchar(50) NOT NULL,
    author varchar(50) NOT NULL,
    genre varchar(50) NOT NULL,
    description varchar(500) NOT NULL,
    imageUrl varchar(500) NOT NULL
);

INSERT INTO manga (title, author, genre, description, imageurl) VALUES ('Naruto', 'Masashi Kishimoto',
                                                                        'Shonen', 'Naruto Uzumaki is a young ninja who seeks recognition from his peers and dreams of becoming the Hokage, the leader of his village.', 'http://localhost:8080/images/manga1.jpg');
INSERT INTO manga (title, author, genre, description, imageurl) VALUES ('One Piece', 'Eiichiro Oda',
                                                                        'Shonen', 'Monkey D. Luffy is a young pirate with a dream: to prove the legend true and become the Pirate King.', 'http://localhost:8080/images/manga2.jpg');
