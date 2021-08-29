package com.edu0988.lesson6.services;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.edu0988.lesson6.activities.MainActivity;
import com.edu0988.lesson6.model.User;

public interface UserAPI {

    //Для запуска в отдельном потоке
    ExecutorService es = Executors.newSingleThreadExecutor();

    default void exec(Runnable before, Runnable after) {
        es.execute(() -> {
                    before.run();
                    new Handler(Looper.getMainLooper()).post(after);
                }
        );
    }
    ////


    //Работа с источником данных
    User get(String uuid);

    List<User> getAll();

    void add(User user);

    void update(User user);

    void delete(String toString);

}
