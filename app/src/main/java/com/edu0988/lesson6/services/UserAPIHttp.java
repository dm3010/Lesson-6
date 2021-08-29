package com.edu0988.lesson6.services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.edu0988.lesson6.database.UserDBHelper;
import com.edu0988.lesson6.database.UserDBSchema;
import com.edu0988.lesson6.model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class UserAPIHttp implements UserAPI {

    private final String HOST = "http://0988.vozhzhaev.ru/";
    private final String host1 = "http://q90313c1.beget.tech/handlerAddUser.php";
    private final String ADD = "handlerAddUser.php";
    private final String GET = "handlerGetUser.php";
    private final String GET_ALL = "handlerGetUsers.php";
    private final String UPDATE = "handlerUpdateUser.php";
    private final String DELETE = "handlerDeleteUser.php";
    private final String PARAM_UUID = "?uuid=%s";
    private final String PARAM_FULL = "?name=%s&lastname=%s&phone=%s&uuid=%s";

    private String connect(URL url) {
        String response = "";
        try {
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.addRequestProperty("User-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36");
            BufferedReader br = new BufferedReader(new InputStreamReader(uc.getInputStream()));
            response = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public void add(User user) {
        try {
            URL url = new URL(HOST + ADD + getUrlEncodedParamsFull(user));
            connect(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public User get(String uuid) {
        try {
            URL url = new URL(HOST + GET + getUrlEncodedParamsUUID(uuid));

            String json = connect(url);
            JSONObject jsonObject = new JSONObject(json);
            User user = new User(jsonObject.getString("uuid"));
            user.setName(jsonObject.getString("username"));
            user.setLastname(jsonObject.getString("lastname"));
            user.setPhone(jsonObject.getString("phone"));
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<User> getAll() {

        Log.d("MY_TAG", "GetALL " + Thread.currentThread().toString());

        List<User> userList = new ArrayList<>();
        try {
            URL url = new URL(HOST + GET_ALL);

            String json = connect(url);
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                User user = new User(jsonObject.getString("uuid"));
                user.setName(jsonObject.getString("username"));
                user.setLastname(jsonObject.getString("lastname"));
                user.setPhone(jsonObject.getString("phone"));
                userList.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }

    @Override
    public void update(User updateUser) {

        Log.d("MY_TAG", "UpdateUser " + Thread.currentThread().toString());

        User user = get(updateUser.getUuid());

        if (user == null) {
            add(updateUser);
        } else {

            try {
                URL url = new URL(HOST + UPDATE + getUrlEncodedParamsFull(updateUser));
                connect(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void delete(String uuid) {

        Log.d("MY_TAG", "DeleteUser " + Thread.currentThread().toString());

        User user = get(uuid);

        if (user != null) {
            try {
                URL url = new URL(HOST + DELETE + getUrlEncodedParamsUUID(user.getUuid()));
                connect(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getUrlEncodedParamsFull(User user) throws UnsupportedEncodingException {
        return String.format(PARAM_FULL,
                URLEncoder.encode(user.getName(), "UTF-8"),
                URLEncoder.encode(user.getLastname(), "UTF-8"),
                URLEncoder.encode(user.getPhone(), "UTF-8"),
                URLEncoder.encode(user.getUuid(), "UTF-8")
        );
    }

    private String getUrlEncodedParamsUUID(String uuid) throws UnsupportedEncodingException {
        return String.format(PARAM_UUID, URLEncoder.encode(uuid, "UTF-8"));
    }

}
