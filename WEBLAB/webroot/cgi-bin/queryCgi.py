import pymysql
import sys

ini = sys.argv[1]

student_id = ini.split("=")[1]

db = pymysql.connect(host="localhost",
                                     user="root",
                                     password="123456",
                                     database="Student_data",
                                     charset='utf8')
cursor = db.cursor()

sql = "SELECT * from student where id = " + student_id +";"
cursor.execute(sql)

data = cursor.fetchall()
res = ""
with open("webroot/query_model.html", "r", encoding="utf-8") as f:
    for line in f:
        res += line
    student_score = ""
    for student in data:
        student_score = str(student[1])
    res = res.replace("$score", student_score)
    res = res.replace("$id", student_id)
print(res)