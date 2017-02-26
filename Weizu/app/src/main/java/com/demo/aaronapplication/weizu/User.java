package com.demo.aaronapplication.weizu;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by Aaron on 2016/3/24.
 */
public class User implements Serializable{

    public static boolean login = false;
    private String username;
    private Bitmap head;

    public User() {}
    public User(String name) { username = name; }

    public String getUsername() { return username; }
}
