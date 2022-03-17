CREATE TABLE courses ( id int PRIMARY KEY, name varchar(64), description varchar(200));
INSERT INTO courses (id, name, description) VALUES (5408, "DataManagement", "ThiscourseisrelatedtoDatabases");
CREATE TABLE studentdata ( id int PRIMARY KEY, fname varchar(64), lname varchar(64), age int);
INSERT INTO studentdata (id, fname, lname, age) VALUES (11, "Pranav", "Chauhan", 24);
CREATE TABLE studentenrollment ( id int PRIMARY KEY, student_id int, FOREIGN KEY (student_id) REFERENCES studentdata(id), course_id int, FOREIGN KEY (course_id) REFERENCES courses(id));
INSERT INTO studentenrollment (id, student_id, course_id) VALUES (890693, 11, 5408);
