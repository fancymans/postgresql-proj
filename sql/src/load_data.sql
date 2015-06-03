DROP TABLE USR;
CREATE TABLE USR
(UserId text, Password text, email text, FullName text, dateofbirth text);
COPY USR FROM '/home/csmajs/dtran042/Desktop/postgresql-proj/data/USR.csv' WITH DELIMITER ',' CSV;
