package com.pam.blogapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;

/**
 * TokenManager - Mengelola deteksi JWT token expired dan force logout.
 *
 * Cara pakai di error handler Volley:
 *   }, error -> {
 *       if (TokenManager.isTokenExpired(error)) {
 *           TokenManager.forceLogout(getContext());
 *       } else {
 *           // handle error lain
 *       }
 *   })
 */
public class TokenManager {

    /**
     * Cek apakah error dari Volley disebabkan oleh token expired (HTTP 401).
     */
    public static boolean isTokenExpired(VolleyError error) {
        if (error == null) return false;
        NetworkResponse response = error.networkResponse;
        return response != null && response.statusCode == 401;
    }

    /**
     * Tampilkan dialog notifikasi sesi expired, lalu redirect ke halaman login.
     * Membersihkan semua data SharedPreferences "user".
     */
    public static void forceLogout(Context context) {
        if (context == null) return;

        // Bersihkan data user yang tersimpan
        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // Tampilkan dialog lalu redirect ke AuthActivity
        new AlertDialog.Builder(context)
                .setTitle("Sesi Berakhir")
                .setMessage("Sesi login kamu telah berakhir. Silakan login kembali.")
                .setCancelable(false)
                .setPositiveButton("Login", (dialog, which) -> {
                    Intent intent = new Intent(context, AuthActivity.class);
                    // Clear back stack agar user tidak bisa kembali ke halaman sebelumnya
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                })
                .show();
    }
}
