package com.example.imirror.videoActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imirror.R;

import java.util.ArrayList;
import java.util.List;

/* 用來處理視訊list */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{

    private List<User> users;
    private List<User> selectedUsers;
    private UsersListener usersListener;

    public UsersAdapter(List<User> users, UsersListener usersListener){
        this.users = users;
        this.usersListener = usersListener;
        selectedUsers = new ArrayList<>();
    }
    public List<User> getSelectedUsers(){
        return selectedUsers;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_user,
                        parent,
                        false
                )
        );
    }
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textFirstChar, textUsername, textEmail;
        ImageView imageAudioMeeting, imageVideoMeeting, imageSelected;
        ConstraintLayout userContainer;


        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            textFirstChar = itemView.findViewById(R.id.textFirstChar);
            textUsername = itemView.findViewById(R.id.textUsername);
            textEmail = itemView.findViewById(R.id.textEmail);
            imageAudioMeeting = itemView.findViewById(R.id.imageAudioMeeting);
            imageVideoMeeting = itemView.findViewById(R.id.imageVideoMeeting);
            userContainer = itemView.findViewById(R.id.userContainer);
            imageSelected = itemView.findViewById(R.id.imageSelected);
        }
        // 放置文字到list上
        void setUserData(User user){
            textFirstChar.setText(user.Name.substring(0,1));
            textUsername.setText(user.Name);
            textEmail.setText(user.email);
            imageAudioMeeting.setOnClickListener(v -> usersListener.initiateAudioMeeting(user));
            imageVideoMeeting.setOnClickListener(v -> usersListener.initiateVideoMeeting(user));

            // 設定長按動作
            userContainer.setOnLongClickListener(v -> {
                if (imageSelected.getVisibility() != View.VISIBLE) {
                    selectedUsers.add(user);
                    imageSelected.setVisibility(View.VISIBLE);
                    imageVideoMeeting.setVisibility(View.GONE);
                    imageAudioMeeting.setVisibility(View.GONE);
                    usersListener.onMultipleUsersAction(true);
                }
                return true;
            });
            // 設置點擊動作
            userContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (imageSelected.getVisibility() == View.VISIBLE){
                        selectedUsers.remove(user);
                        imageSelected.setVisibility(View.GONE);
                        imageVideoMeeting.setVisibility(View.VISIBLE);
                        imageAudioMeeting.setVisibility(View.VISIBLE);
                        if (selectedUsers.size() == 0){
                            usersListener.onMultipleUsersAction(false);
                        }
                    } else {
                        if (selectedUsers.size() > 0){
                            selectedUsers.add(user);
                            imageSelected.setVisibility(View.VISIBLE);
                            imageVideoMeeting.setVisibility(View.GONE);
                            imageAudioMeeting.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
    }
}
