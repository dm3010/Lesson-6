package com.edu0988.lesson6.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edu0988.lesson6.model.User;
import com.edu0988.lesson6.services.UserAPI;
import com.edu0988.lesson6.services.UserAPIHttp;
import com.edu0988.lesson6.services.UserAPISqlLite;
import com.edu0988.lesson6.databinding.ActivityMainBinding;
import com.edu0988.lesson6.databinding.SingleItemBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    public static UserAPI USER_API;

    // Адаптер
    private UserAdapter userAdapter;
    private ActivityMainBinding binding;


    // Меню выбора подключения (локальная БД, удаленный сервер)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, "Локальная БД");
        menu.add(0, 1, 0, "Удаленная БД");
        menu.setGroupCheckable(0, true, true);
        menu.getItem(USER_API instanceof UserAPISqlLite ? 0 : 1).setChecked(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                USER_API = new UserAPISqlLite(this);
                break;
            case 1:
                USER_API = new UserAPIHttp();
                break;
        }
        userAdapter.refreshUsers();
        item.setChecked(true);
        return true;
    }
    //


    // Обновление списка происходит всегда,
    // необходима доработка (переход на фрагменты, создание и обработка ответов от сервиса)
    @Override
    protected void onResume() {
        super.onResume();
        userAdapter.refreshUsers();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle("Список пользователей");

        // USER_API статический
        if (USER_API == null) USER_API = new UserAPISqlLite(this);

        userAdapter = new UserAdapter();

        // SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener(() -> userAdapter.refreshUsers());
        // RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(userAdapter);
        // FloatinAcionButton
        binding.fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserEditActivity.class);
            startActivity(intent);
        });
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

        private List<User> users = new ArrayList<>();

        // Запуск активити инфо о пользователе
        private void showInfoActivity(View v, User u) {
            Intent intent = new Intent(v.getContext(), UserInfoActivity.class);
            intent.putExtra("user", u);
            v.getContext().startActivity(intent);
        }

        // Контекстное меню записей
        private void showOptionMenu(View v, User u) {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Удалить");
            popup.setOnMenuItemClickListener(item -> {
                        USER_API.exec(() -> USER_API.delete(u.getUuid()), this::refreshUsers);
                        return true;
                    }
            );
            popup.show();
        }

        // Обновление списка
        public void refreshUsers() {
            USER_API.exec(
                    () -> {
                        users.clear();
                        users.addAll(USER_API.getAll());
                    },
                    () -> {
                        notifyDataSetChanged();
                        binding.swipeRefreshLayout.setRefreshing(false);
                    }
            );
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            SingleItemBinding binding =
                    SingleItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.itemView.setOnClickListener(v -> showInfoActivity(v, users.get(position)));
            holder.binding.textViewOptions.setOnClickListener(v -> showOptionMenu(v, users.get((position))));
            holder.binding.itemTextView.setText(
                    users.get(position).getName() + "\n" + users.get(position).getLastname()
            );
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private final SingleItemBinding binding;
            public ViewHolder(SingleItemBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}


