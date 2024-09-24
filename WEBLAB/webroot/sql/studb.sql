create database student_data;
use studentDB;
create table student
(
id int(10) primary key not null,
score int(3) not null,
name char(4) not null,
class int(8) not null
);
insert into student values(1,90,'张三',07111905);
insert into student values(2,89,'李四',07111905);
insert into student values(3,88,'王五',07111905);
insert into student values(4,87,'赵六',07111905);
select * from student_data.student;