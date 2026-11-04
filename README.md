# ไมล์ GPS (moto-gps)

แอปไมล์ GPS สำหรับมอเตอร์ไซค์ เป็นเว็บแอปไฟล์เดียว (`app/src/main/assets/www/index.html`)
ห่อด้วย Android WebView เพื่อให้ติดตั้งเป็น APK ได้

## ดาวน์โหลด APK (ง่ายที่สุด)

เปิดลิงก์นี้บนมือถือ Android แล้วแตะติดตั้งได้เลย ไม่ต้องแตก zip:
https://github.com/Bankoio/Bankoio-moto-gps/releases/latest/download/moto-gps.apk

## ดาวน์โหลดจากหน้า Actions (ทางเลือก)

1. เปิดแท็บ **Actions** ของรีโปนี้ แล้วเลือกรันล่าสุดของ workflow **Build APK**
   (หรือกด **Run workflow** เพื่อสั่ง build เอง)
2. เลื่อนลงไปที่ **Artifacts** แล้วดาวน์โหลด `moto-gps-debug-apk`
3. แตก zip จะได้ `app-debug.apk` นำไปติดตั้งบนมือถือ Android 8.0 ขึ้นไป
   (ต้องอนุญาต "ติดตั้งแอปจากแหล่งที่ไม่รู้จัก")

APK เป็นแบบ debug ซึ่งเซ็นด้วยกุญแจที่สร้างใหม่ในแต่ละรอบ build ถ้าจะติดตั้งทับเวอร์ชันเก่า
ให้ถอนการติดตั้งตัวเก่าก่อน

## แก้ไขแอป

แก้ `app/src/main/assets/www/index.html` แล้ว push ขึ้น `main` ระบบจะ build APK ใหม่ให้เอง
