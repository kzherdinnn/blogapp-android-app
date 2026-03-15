package com.kzherdin.blogapp.Fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kzherdin.blogapp.AuthActivity;
import com.kzherdin.blogapp.Constant;
import com.kzherdin.blogapp.R;
import com.kzherdin.blogapp.UserInfoActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUpFragment extends Fragment {
    private View view;
    private TextInputLayout layoutEmail,layoutPassword,layoutConfirm;
    private TextInputEditText txtEmail,txtPassword,txtConfirm;
    private TextView txtSignIn;
    private Button btnSignUp;
    private ProgressDialog dialog;

    public SignUpFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_sign_up,container,false);
        init();
        return view;
    }

    private void init() {
        layoutPassword = view.findViewById(R.id.txtLayoutPasswordSignUp);
        layoutEmail = view.findViewById(R.id.txtLayoutEmailSignUp);
        layoutConfirm = view.findViewById(R.id.txtLayoutConfrimSignUp);
        txtPassword = view.findViewById(R.id.txtPasswordSignUp);
        txtConfirm = view.findViewById(R.id.txtConfirmSignUp);
        txtSignIn = view.findViewById(R.id.txtSignIn);
        txtEmail = view.findViewById(R.id.txtEmailSignUp);
        btnSignUp = view.findViewById(R.id.btnSignUp);
        dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);

        txtSignIn.setOnClickListener(v->{
            //change fragments
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameAuthContainer,new SignInFragment()).commit();
        });

        btnSignUp.setOnClickListener(v->{
            //validate fields first
            if (validate()){
                register();
            }
        });


        txtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!txtEmail.getText().toString().isEmpty()){
                    layoutEmail.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtPassword.getText().toString().length()>7){
                    layoutPassword.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        txtConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtConfirm.getText().toString().equals(txtPassword.getText().toString())){
                    layoutConfirm.setErrorEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private boolean validate (){
        if (txtEmail.getText().toString().isEmpty()){
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Email is Required");
            return false;
        }
        if (txtPassword.getText().toString().length()<8){
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError("Required at least 8 characters");
            return false;
        }
        if (!txtConfirm.getText().toString().equals(txtPassword.getText().toString())){
            layoutConfirm.setErrorEnabled(true);
            layoutConfirm.setError("Password does not match");
            return false;
        }


        return true;
    }


    private void register(){
        dialog.setMessage("Registering");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, Constant.REGISTER, response -> {
            //we get response if connection success
            try {
                JSONObject object = new JSONObject(response);
                if (object.getBoolean("success")){
                    JSONObject user = object.getJSONObject("user");
                    //make shared preference user
                    SharedPreferences userPref = getActivity().getApplicationContext().getSharedPreferences("user",getContext().MODE_PRIVATE);
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.putString("token",object.getString("token"));
                    editor.putString("name", user.isNull("name") ? "" : user.getString("name"));
                    editor.putInt("id",user.getInt("id"));
                    editor.putString("lastname", user.isNull("lastname") ? "" : user.getString("lastname"));
                    editor.putString("photo", user.isNull("photo") ? "" : user.getString("photo"));
                    editor.putBoolean("isLoggedIn",true);
                    editor.apply();
                    //if success — Toast dulu sebelum finish()
                    Toast.makeText(getContext(), "Register Success", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(((AuthActivity)getContext()), UserInfoActivity.class));
                    ((AuthActivity) getContext()).finish();
                } else {
                    String msg = object.optString("message", "Register gagal");
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            dialog.dismiss();

        }, error -> {
            error.printStackTrace();
            dialog.dismiss();

            String message;
            if (error instanceof TimeoutError) {
                message = "Request timeout. Cek koneksi internet.";
            } else if (error instanceof NoConnectionError) {
                message = "Tidak ada koneksi internet.";
            } else if (error instanceof NetworkError) {
                message = "Network error. Cek koneksi.";
            } else if (error instanceof ServerError) {
                NetworkResponse response = error.networkResponse;
                if (response != null) {
                    String body = new String(response.data);
                    android.util.Log.e("REGISTER_ERROR", "Status: " + response.statusCode + " Body: " + body);
                    try {
                        JSONObject errObj = new JSONObject(body);
                        message = "Server error " + response.statusCode + ": " +
                                errObj.optString("message", body);
                    } catch (JSONException ex) {
                        message = "Server error " + response.statusCode + ": " + body;
                    }
                } else {
                    message = "Server error (no response).";
                }
            } else {
                NetworkResponse response = error.networkResponse;
                if (response != null) {
                    String body = new String(response.data);
                    android.util.Log.e("REGISTER_ERROR", "Status: " + response.statusCode + " Body: " + body);
                    message = "Error " + response.statusCode + ": " + body;
                } else {
                    message = "Unknown error: " + error.getMessage();
                }
            }
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }) {

            // add parameters


            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<>();
                map.put("email",txtEmail.getText().toString().trim());
                map.put("password",txtPassword.getText().toString());
                return map;
            }
        };

        //add this request to requestqueue
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }


}
