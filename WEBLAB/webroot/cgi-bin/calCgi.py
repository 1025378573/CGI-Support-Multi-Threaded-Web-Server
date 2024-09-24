import sys

ini = sys.argv[1]
ini = ini.split("&")
a = ini[0].split("=")[1]
b = ini[1].split("=")[1]

res = ""
with open("webroot/cal_model.html", "r", encoding="utf-8") as f:
    for line in f:
        res += line
res = res.replace("$a", a)
res = res.replace("$b", b)
res = res.replace("$res", str(float(a) + float(b)))
print(res)