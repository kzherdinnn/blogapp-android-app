# 📱 Blog App — Android Client

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![API Level](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-blue?style=for-the-badge)
![Target SDK](https://img.shields.io/badge/Target%20SDK-36%20(Android%2016)-green?style=for-the-badge)

**Aplikasi Android untuk berbagi cerita dan berinteraksi dengan sesama pengguna.**  
*Terhubung dengan [Blog App Laravel API](https://github.com/kzherdinnn/blogapp-laravel-api) sebagai backend.*

</div>

---

## 🔗 Ekosistem Aplikasi

Proyek ini terdiri dari **dua repository** yang saling terhubung:

| Repository | Teknologi | Peran |
|---|---|---|
| **[blogapp-android-app](https://github.com/kzherdinnn/blogapp-android-app)** ← *Anda di sini* | Java, Android SDK | Client Mobile |
| **[blogapp-laravel-api](https://github.com/kzherdinnn/blogapp-laravel-api)** | Laravel 9, JWT Auth | REST API Backend |

> 💡 Android app **berkomunikasi** dengan Laravel API melalui HTTP request menggunakan Volley & OkHttp.  
> Base URL API: `https://blogapp-laravel-api-production.up.railway.app`

---

## ✨ Fitur Aplikasi

- 🔐 **Autentikasi** — Register & Login dengan JWT Token
- 📝 **Buat Post** — Upload foto + deskripsi dari galeri
- 🏠 **Feed Beranda** — Lihat semua post dari semua pengguna
- ❤️ **Like Post** — Like/unlike post
- 💬 **Komentar** — Tambah, edit, dan hapus komentar
- ✏️ **Edit Post** — Edit deskripsi post sendiri
- 🗑️ **Hapus Post** — Hapus post milik sendiri
- 👤 **Profil Pengguna** — Lihat dan edit profil (nama, foto)
- 🔄 **Pull-to-Refresh** — Refresh konten dengan swipe ke bawah
- 🔍 **Cari Post** — Filter post berdasarkan kata kunci
- ⏳ **Shimmer Loading** — Animasi loading yang halus
- 🔒 **Auto Logout** — Otomatis logout saat token JWT expired

---

## 🛠️ Tech Stack

| Kategori | Library | Versi |
|---|---|---|
| **HTTP Client** | Volley | 1.2.1 |
| **HTTP Client** | OkHttp3 | 4.12.0 |
| **Image Loading** | Picasso | 2.8 |
| **UI Animation** | Facebook Shimmer | 0.5.0 |
| **Image View** | CircleImageView | 3.1.0 |
| **UI Components** | Material Components | 1.13.0 |
| **Layout** | ConstraintLayout | 2.2.1 |
| **Security** | Conscrypt Android | 2.5.2 |

---

## 📋 Persyaratan

- Android Studio **Hedgehog** atau lebih baru
- JDK **17**
- Android SDK **36**
- Device/Emulator minimal **Android 7.0 (API 24)**

---

## 🚀 Cara Menjalankan

### 1. Clone Repository

```bash
git clone https://github.com/kzherdinnn/blogapp-android-app.git
cd blogapp-android-app
```

### 2. Pastikan Backend Berjalan

Aplikasi ini membutuhkan backend API. Pastikan [Blog App Laravel API](https://github.com/kzherdinnn/blogapp-laravel-api) sudah berjalan.

> **Default:** App sudah dikonfigurasi ke server Railway yang live:  
> `https://blogapp-laravel-api-production.up.railway.app`

Jika ingin menggunakan server lokal, ubah `BASE_URL` di:

```java
// app/src/main/java/com/kzherdin/blogapp/Constant.java
public static final String URL = "http://10.0.2.2"; // untuk emulator
// atau
public static final String URL = "http://192.168.x.x"; // untuk device fisik (IP lokal PC)
```

### 3. Buka di Android Studio

```
File → Open → pilih folder blogapp-android-app
```

### 4. Sync & Build

```
File → Sync Project with Gradle Files
```

### 5. Jalankan Aplikasi

```
Run → Run 'app' (Shift+F10)
```

---

## 📡 Koneksi ke API

Semua endpoint API didefinisikan di `Constant.java`:

```java
BASE_URL  = https://blogapp-laravel-api-production.up.railway.app
API       = BASE_URL/api

POST  /api/login              → Login
POST  /api/register           → Register
GET   /api/logout             → Logout
POST  /api/save_user_info     → Update profil

GET   /api/posts              → Semua post
GET   /api/posts/my_posts     → Post milik saya
POST  /api/posts/create       → Buat post baru
POST  /api/posts/posts/{id}   → Edit post
DEL   /api/posts/{id}         → Hapus post
POST  /api/posts/like         → Like/unlike post
POST  /api/posts/comments     → Komentar pada post

POST  /api/comments/create    → Tambah komentar
PUT   /api/comments/update    → Edit komentar
DEL   /api/comments/delete    → Hapus komentar
```

> 📖 Untuk dokumentasi API lengkap, lihat [blogapp-laravel-api](https://github.com/kzherdinnn/blogapp-laravel-api#-api-endpoints)

---

## 🗂️ Struktur Project

```
app/src/main/java/com/kzherdin/blogapp/
│
├── 📁 Adapters/
│   ├── AccountPostAdapter.java    # Adapter post di halaman akun
│   ├── CommentsAdapter.java       # Adapter daftar komentar
│   ├── PostsAdapter.java          # Adapter feed utama
│   └── ViewPagerAdapter.java      # Adapter view pager auth
│
├── 📁 Fragments/
│   ├── AccountFragment.java       # Tab profil pengguna
│   ├── HomeFragment.java          # Tab feed beranda
│   ├── SignInFragment.java        # Form login
│   └── SignUpFragment.java        # Form register
│
├── 📁 Models/
│   ├── Comment.java               # Model komentar
│   ├── Post.java                  # Model post
│   └── User.java                  # Model pengguna
│
├── AddPostActivity.java           # Halaman buat post baru
├── App.java                       # Application class (Conscrypt init)
├── AuthActivity.java              # Halaman autentikasi
├── CommentActivity.java           # Halaman komentar
├── Constant.java                  # Konstanta URL API
├── EditPostActivity.java          # Halaman edit post
├── EditUserInfoActivity.java      # Halaman edit profil
├── HomeActivity.java              # Halaman utama (Bottom Nav)
├── MainActivity.java              # Splash Screen
├── OnBoardActivity.java           # Onboarding screen
├── TokenManager.java              # Manajemen JWT Token
└── UserInfoActivity.java          # Halaman profil pengguna lain
```

---

## 📦 Info Aplikasi

| Properti | Nilai |
|---|---|
| **Package Name** | `com.kzherdin.blogapp` |
| **Version Name** | `1.3` |
| **Version Code** | `6` |
| **Min SDK** | `24` (Android 7.0 Nougat) |
| **Target SDK** | `36` (Android 16) |
| **Compile SDK** | `36` |
| **Language** | Java |

---

## 📸 Screenshot

> *Coming soon*

---

## 🤝 Kontribusi

Pull request sangat disambut! Untuk perubahan besar, harap buka *issue* terlebih dahulu.

---

## 👨‍💻 Developer

**Herdin Kz**  
GitHub: [@kzherdinnn](https://github.com/kzherdinnn)

---

## 📄 Lisensi

MIT License — bebas digunakan untuk keperluan belajar dan pengembangan.
