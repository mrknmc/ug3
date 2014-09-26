CREATE TABLE Movies (
    Title varchar(64),
    Year integer,
    Director varchar(64),
    Country varchar(64),
    Rating real,
    Genre varchar(64),
    Gross real,
    Producer varchar(64),
    PRIMARY KEY (Title, Year)
);

CREATE TABLE Actors (
    Title varchar(64),
    Year integer,
    Character_name varchar(64),
    Actor varchar(64),
    PRIMARY KEY (Title, Year, Character_name),
    FOREIGN KEY (Title, Year) REFERENCES Movies(Title, Year)
);

CREATE TABLE Awards (
    Title varchar(64),
    Year integer,
    Award varchar(64),
    Result varchar(32),
    PRIMARY KEY (Title, Year, Award),
    FOREIGN KEY (Title, Year) REFERENCES Movies(Title, Year)
);

INSERT INTO Movies (Title, Year, Director, Country, Rating, Genre, Gross, Producer)
VALUES ('The Dark Knight', 2012, 'Christopher Nolan', 'USA', 9.0, 'Action', 533316061, 'Kevin De La Noy'),
       ('The Dark Knight Rises', 2012, 'Christopher Nolan', 'USA', 8.5, 'Action', 548130642, 'Kevin De La Noy'),
       ('The Godfather', 1999, 'Francis Ford Coppola', 'USA', 9.2, 'Comedy', 134821952, 'Gray Frederickson'),
       ('Star Wars: Episode V - The Empire Strikes Back', 1980, 'Irvin Kershner', 'USA', 8.8, 'Action', 290475067, 'Gary Kurtz'),
       ('Fight Club', 1999, 'David Fincher', 'USA', 8.8, 'Drama', 63000000, 'Ross Grayson Bell'),
       ('Schindlers List', 1993, 'Steven Spielberg', 'USA', 8.9, 'Drama', 96045248, 'Branko Lustig'),
       ('Saving Private Ryan', 1993, 'Steven Spielberg', 'USA', 9.5, 'Comedy', 216119491, 'Ian Bryce'),
       ('Google', 2012, 'Maca', 'USA', 9, 'Drama', 1000, 'Ksfaihfa'),
       ('Esa Pesa Knight', 2011, 'Ladas Vadas', 'France', 2, 'Comedy', 1231241, 'Isafs Fsfa');

INSERT INTO Actors (Title, Year, Actor, Character_name)
VALUES ('The Godfather', 1999, 'Marlon Brando', 'Don Vito Corleone'),
       ('The Godfather', 1999, 'Al Pacino', 'Michael Corleone'),
       ('The Godfather', 1999, 'James Caan', 'Sonny Corleone'),
       ('The Dark Knight', 2012, 'Christian Bale', 'Bruce Wayne'),
       ('Esa Pesa Knight', 2011, 'Loco Moco', 'Bruce Wayne'),
       ('The Dark Knight', 2012, 'Heath Ledger', 'Joker'),
       ('The Dark Knight', 2012, 'Aaron Eckhart', 'Harvey Dent'),
       ('The Dark Knight', 2012, 'Aaron Eckhart', 'Harvey Two Face'),
       ('Google', 2012, 'Aaron Eckhart', 'Bobobo'),
       ('Star Wars: Episode V - The Empire Strikes Back', 1980, 'Mark Hamill', 'Luke Skywalker'),
       ('Star Wars: Episode V - The Empire Strikes Back', 1980, 'Harrison Ford', 'Han Solo');

  INSERT INTO Awards (Title, Year, Award, Result)
  VALUES ('Star Wars: Episode V - The Empire Strikes Back', 1980, 'Oscar, Best Sound', 'won'),
         ('Star Wars: Episode V - The Empire Strikes Back', 1980, 'Special Achievement Award', 'won'),
         ('The Godfather', 1999, 'Oscar, Best Picture', 'won'),
         ('The Godfather', 1999, 'Oscar, Best Actor', 'nominated'),
         ('The Godfather', 1999, 'BAFTA, Best Actor', 'won'),
         ('Schindlers List', 1993, 'Oscar, Best Director', 'won'),
         ('Saving Private Ryan', 1993, 'Oscar, Best Director', 'nominated'),
         ('The Dark Knight', 2012, 'Oscar, Best Performance by an Actor in a Supporting Role', 'won'),
         ('The Dark Knight', 2012, 'Oscar, Best Achievement in Sound Editing', 'won'),
         ('The Dark Knight', 2012, 'Oscar, Best Achievement in Cinematography', 'nominated'),
         ('Esa Pesa Knight', 2011, 'Oscar, Best Director', 'won');


-- (1) List actors who played more than one character in the same movie.
SELECT DISTINCT A.Actor FROM Actors A, Actors B
WHERE A.Title = B.Title AND A.Year = B.Year AND A.Actor = B.Actor AND A.Character_name <> B.Character_name;


-- (2) Find the average gross for movies directed by a director who won an Oscar.
SELECT AVG(M2.Gross) FROM Movies M1, Movies M2, Awards A
WHERE M1.Title = A.Title AND M1.Year = A.Year AND M1.Director = M2.Director AND A.Award = 'Oscar, Best Director' AND A.Result = 'won';


-- (3) List producers, and the total amount of money their movies made.
SELECT M.Producer, SUM(M.Gross) FROM Movies M
GROUP BY M.Producer;


-- (4) List producers who produced at least two movies that grossed more than $50 million in a single year.
-- Assuming that this means that each one grossed more than 50 million
SELECT DISTINCT M1.Producer FROM Movies M1, Movies M2
WHERE M1.Producer = M2.Producer AND M1.Title <> M2.Title AND M1.Year = M2.Year AND M1.Gross > 50000000 AND M2.Gross > 50000000;


-- (5) For each director, select his/her movies rated higher than 8, ordered by their gross.
-- Assuming that movies means (Title, Year)
SELECT M.Director, M.Title, M.Year FROM Movies M
WHERE M.Rating > 8 ORDER BY M.Gross;


-- (6) Find movies made in the 90s that were nominated for at least two different awards (e.g, Oscar and Golden Globe) in the best actor category.
-- Assuming that movies means (Title, Year) and that Result='won' implicitly implies they were nominated.
SELECT DISTINCT M.Title, M.Year FROM Movies M, Awards A1, Awards A2
WHERE M.Year >= 1990 AND
      M.Year < 2000 AND
      M.Year = A1.Year AND
      M.Title = A1.Title AND
      A1.Year = A2.Year AND
      A1.Title = A2.Title AND
      A1.Award <> A2.Award AND
      A1.Award LIKE '%, Best Actor' AND
      A2.Award LIKE '%, Best Actor';


-- (7) Find movies made in the 90s that won every award they were nominated for.
-- Assuming that movies means (Title, Year) and Result='won' implicitly implies they were nominated.
SELECT M.Title, M.Year FROM Movies M
WHERE M.Year >= 1990 AND
      M.Year < 2000 AND
      NOT EXISTS (
        SELECT * FROM Awards A WHERE A.Title = M.Title AND A.Year = M.Year AND A.Result = 'nominated'
      );


-- (8) List all comedies that won major Oscars (best film or director) before 1960 and after 1990.
-- Assuming we should return (Title, Year) tuples and the year is before 1960 OR after 1990
SELECT M.Title, M.Year FROM Movies M, Awards A
WHERE M.Genre = 'Comedy' AND
      M.Title = A.Title AND
      M.Year = A.Year AND
      (A.Award LIKE 'Oscar, Best Picture' OR Award LIKE 'Oscar, Best Director') AND
      (M.Year < 1960 OR M.Year > 1990) AND
      A.Result = 'won';


-- (9) Find directors who directed a comedy and a drama, with the comedy having a higher rating than the drama.
SELECT DISTINCT M1.Director FROM Movies M1, Movies M2
WHERE M1.Director = M2.Director AND
      M1.Genre = 'Comedy' AND
      M2.Genre = 'Drama' AND
      M1.Rating > M2.Rating;


-- (10) Find actors who only act in high grossing (more than $50 million) movies.
SELECT DISTINCT A.Actor FROM Actors A, Movies M
WHERE M.Gross > 50000000 AND
      M.Title = A.Title AND
      M.Year = A.Year
  EXCEPT
SELECT A.Actor FROM Actors A, Movies M
WHERE M.Gross <= 50000000 AND
      M.Title = A.Title AND
      M.Year = A.Year;


-- (11) For each award category, find the average rating of movies that won that award.
-- Assuming that award category is a value of the Award column
SELECT A.Award, AVG(M.Rating) FROM Awards A, Movies M
WHERE A.Title = M.Title AND A.Year = M.Year AND A.Result = 'won' GROUP BY A.Award;


-- (12) Find the award category whose winners have the highest average rating.
-- Assuming that award category is a value of the Award column
SELECT A.Award FROM Movies M, Awards A
WHERE A.Title = M.Title AND A.Year = M.Year AND A.Result = 'won' GROUP BY A.Award ORDER BY AVG(M.Rating) DESC LIMIT 1;


-- (13) Find all pairs of movies (m1,m2) nominated for the same award, such that m1 has higher rating than m2, but m2 won the award.
-- Assuming that movies means the whole tuple
SELECT M1.*, M2.* FROM Movies M1, Movies M2, Awards A1, Awards A2
WHERE M1.Title = A1.Title AND
      M1.Year = A1.Year AND
      M2.Title = A2.Title AND
      M2.Year = A2.Year AND
      A1.Award = A2.Award AND
      M1.Rating > M2.Rating AND
      A2.Result = 'won' AND
      A1.Result = 'nominated';


-- (14) Find character names that appear in two movies produced in two different countries.
SELECT DISTINCT A1.Character_name FROM Movies M1, Movies M2, Actors A1, Actors A2
WHERE A1.Character_name = A2.Character_name AND
      M1.Title = A1.Title AND
      M1.Year = A1.Year AND
      M2.Title = A2.Title AND
      M2.Year = A2.Year AND
      M1.Country <> M2.Country AND
      M1.Title <> M2.Title AND
      M1.Year <> M2.Year;


-- (15) For every decade starting with 1950-59, calculate the percentage of all the awards won by US movies.
WITH us_count AS (
  SELECT M.Year / 10 AS Decade, COUNT(A.Award) AS Count
  FROM Movies M, Awards A
  WHERE M.Country = 'USA' AND
        M.Title = A.Title AND
        M.Year = A.Year AND
        M.Year >= 1950 AND
        A.Result = 'won'
  GROUP BY (M.Year / 10)
)
SELECT CONCAT((A.Year / 10), '0 - ', (A.Year / 10), '9') AS Decade,
        100 * US.Count / COUNT(A.Award) AS Percentage
  FROM Awards A, us_count US
  WHERE A.Year >= 1950 AND
        A.Result = 'won' AND
        (A.Year / 10) = US.Decade
  GROUP BY (A.Year / 10), US.COUNT;

