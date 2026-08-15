# Machawi Gerant App

Same idea as the kitchen app, but simpler — this one doesn't need the
printer library at all, since printing already happens from the kitchen
tablet, not from here. It's just your `/gerant` dashboard shown full-screen
in its own app instead of a browser tab.

## Before uploading — one edit

Open **`app/src/main/java/com/machawi/gerant/MainActivity.kt`** and change
this line to your site's real owner dashboard URL:
```kotlin
private val gerantUrl = "https://your-site.vercel.app/gerant"
```

## Steps (same as the kitchen app)

1. Create a new GitHub repository (e.g. `gerant-app`), empty, no README.
2. Upload this whole folder's **contents** (not the folder itself) to it —
   drag everything from inside this folder onto GitHub's upload page so
   `build.gradle`, `app`, `.github`, etc. land at the repo's root, same as
   we did for the kitchen app.
3. Go to the **Actions** tab — it should start building automatically. If
   not, click **"Build APK"** → **"Run workflow"**.
4. Wait for a green checkmark.
5. Click into that run, scroll to **Artifacts**, download
   **machawi-gerant-apk**, extract it to get `app-debug.apk`.
6. Get that file onto the owner's tablet/phone, install it (allow "install
   from unknown source" if prompted).
7. Open the app — it should show the owner dashboard full-screen, with the
   login screen exactly as it looks on the website.

## Daily use

Just open the app like any other app — logs in the same way as the
website, shows live orders, the "Aujourd'hui" tab, everything the owner
already uses, just without needing to open a browser first.
