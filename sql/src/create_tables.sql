-- Team 14
-- Darren Trang 861007779
-- Darrin Lin 860988334
DROP TABLE WORK_EXPR;
DROP TABLE EDUCATIONAL_DETAILS;
DROP TABLE MESSAGE;
DROP TABLE CONNECTION_USR;
DROP TABLE USR;
DROP SEQUENCE message_msgId_seq;

CREATE TABLE USR(
	userId varchar(50) UNIQUE NOT NULL,
	password varchar(50) NOT NULL,
	email text NOT NULL,
	name char(50),
	dateOfBirth date,
	Primary Key(userId));

CREATE TABLE WORK_EXPR(
	userId char(50) NOT NULL,
	company char(50) NOT NULL,
	role char(50) NOT NULL,
	location char(50),
	startDate date,
	endDate date,
	PRIMARY KEY(userId,company,role,startDate));

CREATE TABLE EDUCATIONAL_DETAILS(
	userId char(50) NOT NULL,
	instituitionName char(50) NOT NULL,
	major char(50) NOT NULL,
	degree char(50) NOT NULL,
	startdate date,
	enddate date,
	PRIMARY KEY(userId,major,degree));

CREATE SEQUENCE message_msgId_seq start with 27812 increment by 1 minvalue 0;
CREATE TABLE MESSAGE(
	msgId integer default nextval('message_msgId_seq')UNIQUE NOT NULL,
	senderId char(50) NOT NULL,
	receiverId char(50) NOT NULL,
	contents char(500) NOT NULL,
	sendTime timestamp,
	deleteStatus integer,
	status char(30) NOT NULL,
	PRIMARY KEY(msgId));

CREATE TABLE CONNECTION_USR(
	userId char(50) NOT NULL,
	connectionId char(50) NOT NULL,
	status char(30) NOT NULL,
	PRIMARY KEY(userId,connectionId));
