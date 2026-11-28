# 🚀 Hyper Universal Space Tactics

**Hyper Universal Space Tactics (HUST)** là game bắn phi thuyền 2D góc nhìn từ trên xuống, được viết bằng Java Swing. Đây là bài tập lớn môn Lập trình hướng đối tượng của Nhóm 2, tập trung vào kiến trúc phân tầng, các mẫu thiết kế và vòng lặp game thời gian thực.

---

## 📸 Demo

[Xem video gameplay](https://www.youtube.com/watch?v=7XMH-ewkGqI).

---

## 🎮 Lối chơi

Điều khiển phi thuyền, né đạn và sống sót qua các đợt kẻ địch liên tiếp. Hạ kẻ địch để ghi điểm. Đợt thứ `n` có `5 + 2 × n` kẻ địch. Kẻ địch đầu tiên xuất hiện khi đợt bắt đầu, sau đó cứ 2 giây xuất hiện thêm một chiếc. Boss xuất hiện đầu tiên ở mỗi đợt thứ ba.

### Điều khiển

| Hành động | Phím hoặc chuột |
| --- | --- |
| Di chuyển | `W` `A` `S` `D` |
| Ngắm | Di chuyển chuột |
| Bắn plasma | Giữ chuột trái |
| Tăng tốc (Boost) | `Shift`, hồi sau 10 giây. HUD hiển thị thời gian hồi. |
| Tạm dừng và chỉnh âm lượng | `Esc` |

### Các loại kẻ địch

| Loại | Máu | Sát thương khi va chạm | Điểm | Hành vi |
| --- | ---: | ---: | ---: | --- |
| Basic | 100 | 50 | 100 | Đuổi theo người chơi và bắn đạn tự dẫn |
| Orbit | 150 | 60 | 150 | Bay vòng quanh người chơi và bắn đạn thường |
| Drone | 200 | 200 | 200 | Lao thẳng vào người chơi để gây sát thương khi va chạm |
| Boss | 5000 | 200 | 5000 | Tiến chậm về phía người chơi và bắn đạn thường |

---

## 🧠 Trọng tâm kỹ thuật

### Kiến trúc

| Tầng | Vai trò |
| --- | --- |
| `presentation` | Nhận phím và chuột, điều phối các màn hình, vẽ cảnh chơi và HUD bằng Swing |
| `application` | Chạy vòng lặp game, cập nhật camera, phiên chơi và các đợt kẻ địch |
| `domain` | Mô hình hóa phi thuyền, kẻ địch, vũ khí, đạn, di chuyển và hiệu ứng |
| `infrastructure` | Cung cấp cấu hình, âm thanh, object pool và bộ nạp tài nguyên |


### Mẫu thiết kế

| Mẫu | Cách áp dụng |
| --- | --- |
| State | `StateManager` quản lý `Menu`, `Playing`, `PausedOverlay` và `GameOver` |
| Strategy | `MovementBehavior` cho phép chọn di chuyển thẳng, đuổi mục tiêu hoặc bay vòng |
| Template Method | `Weapon.fire` kiểm tra hồi chiêu và gọi phương thức tạo đạn của từng loại súng |
| Factory | `StateFactory` tạo màn hình. `ProjectileFactory` cung cấp đạn theo loại. |
| Object Pool | `ObjectPool` và `PoolManager` tái sử dụng kẻ địch, đạn và hiệu ứng |
| Observer | `GameEventListener` nhận thông báo về các sự kiện chiến đấu |
| Singleton | `SoundManager` cung cấp một bộ quản lý âm thanh dùng chung |

---

## ⚙️ Công nghệ

- Java 25, Swing, Java2D và Java Sound
- Maven để biên dịch, kiểm thử và đóng gói JAR
- JUnit 5 để kiểm thử đơn vị
- `jpackage` và PowerShell để tạo gói chạy trên Windows

---

## 📁 Cấu trúc dự án

```text
src/main/java/com/game/spaceshooter/
├── Main.java
├── presentation/
├── application/
├── domain/
└── infrastructure/

src/main/resources/         # Ảnh, font và âm thanh
src/test/java/              # Unit test JUnit 5
scripts/package-windows.ps1 # Script tạo ZIP cho Windows
docs/                       # Sơ đồ thiết kế
pom.xml                     # Cấu hình Maven
```

Các thông số như chỉ số kẻ địch, tần suất xuất hiện, tốc độ phi thuyền, thời gian hồi Boost và kích thước cửa sổ nằm trong `infrastructure/config`.

---

## ▶️ Cách chạy

### Cách 1. Chạy từ gói ZIP Windows

[**Tải Shooting2DGame-windows.zip**](https://github.com/quanghien1712/hyper-universal-space-tactics/releases/latest/download/Shooting2DGame-windows.zip)

Giải nén ZIP rồi mở `Shooting2DGame\Shooting2DGame.exe`. Gói có sẵn Java runtime nên máy chơi không cần cài Java. Giữ hai thư mục `app` và `runtime` cạnh file EXE.


### Cách 2. Build và chạy JAR bằng Maven

Cần Maven và JDK 25 trở lên. Chạy trong terminal.

```sh
git clone https://github.com/quanghien1712/hyper-universal-space-tactics.git
cd hyper-universal-space-tactics
mvn package
java -jar target/Shooting2DGame-1.0-SNAPSHOT.jar
```

JAR chứa mã chương trình và các tài nguyên cần để chạy game trên Windows, macOS hoặc Linux có môi trường đồ họa.

### Cách 3. Chạy từ IDE

Mở `pom.xml` dưới dạng dự án Maven, chọn JDK 25 trở lên và chạy lớp `com.game.spaceshooter.Main`.

### Chạy kiểm thử

```sh
mvn test
```

---

## 🚧 Hướng phát triển

- Thêm các giai đoạn và kiểu tấn công cho boss.
- Bổ sung kỹ năng và nâng cấp phi thuyền.
- Cải thiện hoạt ảnh, giao diện và âm thanh.
- Mở rộng kiểm thử tự động cho các luồng chơi.

---

## 👤 Tác giả

**Trần Quang Hiển** và **Ngô Trường Phúc**, sinh viên **Đại học Bách khoa Hà Nội**.

---

## ⭐ Ghi nhận

Dự án được thực hiện cho mục đích học tập và lấy cảm hứng từ *Hyperspace Striker*.
