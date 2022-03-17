CREATE TABLE bikes ( id int PRIMARY KEY, company varchar(8000), speed int);
CREATE TABLE cars ( id int PRIMARY KEY, company varchar(8000), model varchar(255));
INSERT INTO cars (id, company, model) VALUES (1, "tesla", "model-S");
INSERT INTO cars (id, company, model) VALUES (2, "bmw", "null");
CREATE TABLE people ( id int PRIMARY KEY, first_name varchar(64), last_name varchar(64), age int);
INSERT INTO people (id, first_name, last_name, age) VALUES (1, "pankti", "vyas", 21);
INSERT INTO people (id, first_name, last_name, age) VALUES (2, "test", "user", 21);
INSERT INTO people (id, first_name, last_name, age) VALUES (4, "macbook", "air", null);
