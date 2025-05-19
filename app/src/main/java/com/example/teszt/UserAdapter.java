package com.example.teszt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<ProfileActivity.UserData> userList;

    public UserAdapter(List<ProfileActivity.UserData> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item_layout, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        ProfileActivity.UserData user = userList.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView userNameTextView;
        private TextView userDetailTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameItem);
            userDetailTextView = itemView.findViewById(R.id.userDetailItem);
        }

        public void bind(ProfileActivity.UserData user) {
            userNameTextView.setText(user.getUserName());
            if (user.getAge() > 0) {
                userDetailTextView.setText("Életkor: " + user.getAge() + " év");
            } else {
                userDetailTextView.setText(user.getUserEmail() != null ? user.getUserEmail() : "");
            }
        }
    }
}