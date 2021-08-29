package com.edu0988.lesson6.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.edu0988.lesson6.model.User;
import com.edu0988.lesson6.database.UserDBHelper;
import com.edu0988.lesson6.database.UserDBSchema;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserAPISqlLite implements UserAPI {

    public UserAPISqlLite(Context context) {
        UserDBHelper.init(context);
    }

    @Override
    public void add(User user) {
        SQLiteDatabase db = UserDBHelper.get().getWritableDatabase();
        db.insert(UserDBSchema.UserTable.NAME, null, getContentValues(user));
        db.close();
    }

    @Override
    public User get(String uuid) {
        List<User> userList = getAll();
        for (User user : userList) {
            if (user.getUuid().equals(uuid)) return user;
        }
        return null;
    }

    @Override
    public List<User> getAll() {
        List<User> userList = new ArrayList<>();

        Log.d("MY_TAG", "Users sss" + userList + " " + Thread.currentThread().toString());

        try (SQLiteDatabase db = UserDBHelper.get().getWritableDatabase();
             Cursor cursor = queryAll(db)) {

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                userList.add(get(cursor));
                cursor.moveToNext();
            }
        }
        Log.d("MY_TAG", "Users ssssss" + userList + " " + Thread.currentThread().toString());

        return userList;
    }

    @Override
    public void update(User updateUser) {
        User user = get(updateUser.getUuid().toString());
        if (user == null) {
            add(updateUser);
        } else {
            SQLiteDatabase db = UserDBHelper.get().getWritableDatabase();
            db.update(
                    UserDBSchema.UserTable.NAME,
                    getContentValues(updateUser),
                    "uuid = ?",
                    new String[]{user.getUuid().toString()}
            );
            db.close();
        }
    }

    @Override
    public void delete(String uuid) {
        User user = get(uuid);
        if (user != null) {
            SQLiteDatabase db = UserDBHelper.get().getWritableDatabase();
            db.delete(
                    UserDBSchema.UserTable.NAME,
                    "uuid = ?",
                    new String[]{uuid}
            );
            db.close();
        }
    }

    private static ContentValues getContentValues(User user) {
        ContentValues values = new ContentValues();
        values.put(UserDBSchema.Cols.UUID, user.getUuid().toString());
        values.put(UserDBSchema.Cols.USERNAME, user.getName());
        values.put(UserDBSchema.Cols.USERLASTNAME, user.getLastname());
        values.put(UserDBSchema.Cols.PHONE, user.getPhone());
        return values;
    }

    private static Cursor queryAll(SQLiteDatabase db) {
        return db.query(
                UserDBSchema.UserTable.NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private static User get(Cursor cursor) {
        String uuidString = cursor.getString(cursor.getColumnIndex(UserDBSchema.Cols.UUID));
        String userName = cursor.getString(cursor.getColumnIndex(UserDBSchema.Cols.USERNAME));
        String userLastName = cursor.getString(cursor.getColumnIndex(UserDBSchema.Cols.USERLASTNAME));
        String phone = cursor.getString(cursor.getColumnIndex(UserDBSchema.Cols.PHONE));
        User user = new User(uuidString);
        user.setName(userName);
        user.setLastname(userLastName);
        user.setPhone(phone);
        return user;
    }
}
