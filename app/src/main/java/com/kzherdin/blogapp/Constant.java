package com.kzherdin.blogapp;

public class Constant {
    public static final String URL = "https://blogapp-laravel-api-production.up.railway.app";
    public static final String HOME = URL+"/api";
    public static final String LOGIN = HOME+"/login";
    public static final String LOGOUT = HOME+"/logout";
    public static final String REGISTER = HOME+"/register";
    public static final String SAVE_USER_INFO = HOME+"/save_user_info";
    public static final String POSTS = HOME+"/posts";
    public static final String ADD_POST = POSTS+"/create";
    public static final String UPDATE_POST = POSTS + "/update/%d";
    public static final String DELETE_POST = POSTS+"/%d";
    public static final String LIKE_POST = POSTS+"/like";
    public static final String COMMENTS = POSTS+"/comments";
    public static final String CREATE_COMMENT = HOME+"/comments/create";
    public static final String DELETE_COMMENT = HOME+"/comments/delete";
    public static final String EDIT_COMMENT = HOME+"/comments/update";
    public static final String MY_POST = POSTS+"/my_posts";
}
