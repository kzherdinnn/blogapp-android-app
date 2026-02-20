package com.kzherdin.blogapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kzherdin.blogapp.Fragments.HomeFragment;
import com.kzherdin.blogapp.Models.Post;
import com.kzherdin.blogapp.Models.User;
import com.kzherdin.blogapp.TokenManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {
    private Button btnPost;
    private ImageView imgPost;
    private EditText txtDesc;
    private Bitmap bitmap = null;
    private static final int GALLERY_CHANGE_POST = 3;
    private ProgressDialog dialog;
    private SharedPreferences preferences;

    // FIX DOUBLE POST: flag untuk mencegah double submit
    private boolean isPosting = false;

    // Singleton RequestQueue
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        init();
    }

    private void init() {
        preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        btnPost = findViewById(R.id.btnAddPost);
        imgPost = findViewById(R.id.imgAddPost);
        txtDesc = findViewById(R.id.txtDescAddPost);

        // Singleton queue
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading post...");

        imgPost.setImageURI(getIntent().getData());
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), getIntent().getData());
        } catch (IOException e) {
            e.printStackTrace();
        }

        btnPost.setOnClickListener(v -> {
            // FIX DOUBLE POST: cek flag isPosting
            if (isPosting) return;

            if (txtDesc.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Post description is required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (bitmap == null) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
                return;
            }

            post();
        });
    }

    private void post() {
        // FIX DOUBLE POST: set flag dan disable tombol
        isPosting = true;
        btnPost.setEnabled(false);
        btnPost.setText("Uploading...");
        dialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, Constant.ADD_POST, response -> {
            dialog.dismiss();
            isPosting = false;
            btnPost.setEnabled(true);
            btnPost.setText("Publish Post");

            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")) {
                    JSONObject postObject = object.getJSONObject("post");
                    JSONObject userObject = postObject.getJSONObject("user");

                    User user = new User();
                    user.setId(userObject.getInt("id"));
                    user.setUserName(userObject.getString("name") + " " + userObject.getString("lastname"));
                    user.setPhoto(userObject.getString("photo"));

                    Post post = new Post();
                    post.setUser(user);
                    post.setId(postObject.getInt("id"));
                    post.setSelfLike(false);
                    post.setPhoto(postObject.getString("photo"));
                    post.setDesc(postObject.getString("desc"));
                    post.setComments(0);
                    post.setLikes(0);
                    post.setDate(postObject.getString("created_at"));

                    // FIX DOUBLE POST: cek dulu apakah post sudah ada di list
                    boolean alreadyExists = false;
                    if (HomeFragment.arrayList != null) {
                        for (Post p : HomeFragment.arrayList) {
                            if (p.getId() == post.getId()) {
                                alreadyExists = true;
                                break;
                            }
                        }
                    }

                    if (!alreadyExists && HomeFragment.arrayList != null && HomeFragment.recyclerView != null) {
                        HomeFragment.arrayList.add(0, post);
                        HomeFragment.recyclerView.getAdapter().notifyItemInserted(0);
                        HomeFragment.recyclerView.scrollToPosition(0);
                    }

                    Toast.makeText(this, "Post uploaded! 🎉", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Upload failed, please try again", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }, error -> {
            dialog.dismiss();
            isPosting = false;
            btnPost.setEnabled(true);
            btnPost.setText("Publish Post");
            error.printStackTrace();
            // Handle token expired: redirect ke login jika 401
            if (TokenManager.isTokenExpired(error)) {
                TokenManager.forceLogout(AddPostActivity.this);
            } else {
                Toast.makeText(this, "Upload failed. Check your connection.", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = preferences.getString("token", "");
                HashMap<String, String> map = new HashMap<>();
                map.put("Authorization", "Bearer " + token);
                return map;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<>();
                map.put("desc", txtDesc.getText().toString().trim());
                // FIX UPLOAD LAMBAT: compress ke 70% quality dan resize max 1024px
                map.put("photo", bitmapToString(bitmap));
                return map;
            }
        };

        // FIX UPLOAD LAMBAT: timeout lebih panjang (60 detik), retry 0x (jangan retry otomatis - cegah double post)
        request.setRetryPolicy(new DefaultRetryPolicy(
                60000,   // 60 detik timeout
                0,       // NO retry - cegah double post akibat retry otomatis
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        // Tag request agar bisa di-cancel jika activity di-destroy
        request.setTag("add_post");
        requestQueue.add(request);
    }

    /**
     * FIX UPLOAD LAMBAT: Resize bitmap ke max 1024px dan compress ke 70%
     * Ini mengurangi ukuran file dari ~3MB menjadi ~200KB
     */
    private String bitmapToString(Bitmap bitmap) {
        if (bitmap != null) {
            // Resize bitmap jika terlalu besar
            Bitmap resized = resizeBitmap(bitmap, 1024);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            // Compress ke 70% - cukup bagus secara visual tapi jauh lebih kecil
            resized.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] array = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(array, Base64.DEFAULT);
        }
        return "";
    }

    /**
     * Resize bitmap agar sisi terpanjang tidak melebihi maxSize
     */
    private Bitmap resizeBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxSize && height <= maxSize) {
            return bitmap; // Tidak perlu resize
        }

        float ratio = (float) width / height;
        if (width > height) {
            width = maxSize;
            height = (int) (width / ratio);
        } else {
            height = maxSize;
            width = (int) (height * ratio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    public void cancelPost(View view) {
        // Cancel request yang sedang berjalan jika ada
        if (requestQueue != null) {
            requestQueue.cancelAll("add_post");
        }
        super.onBackPressed();
    }

    public void changePhoto(View view) {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, GALLERY_CHANGE_POST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CHANGE_POST && resultCode == RESULT_OK) {
            Uri imgUri = data.getData();
            imgPost.setImageURI(imgUri);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel semua request saat activity di-destroy
        if (requestQueue != null) {
            requestQueue.cancelAll("add_post");
        }
    }
}
