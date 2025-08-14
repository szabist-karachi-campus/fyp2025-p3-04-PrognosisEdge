package com.example.prognosisedge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prognosisedge.models.User;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private OnToggleListener toggleListener;
    private OnEditListener editListener;

    public interface OnToggleListener {
        void onToggle(int position, boolean isActive);
    }

    public interface OnEditListener {
        void onEdit(User user);
    }

    // Updated constructor to include the new edit listener
    public UserAdapter(List<User> userList, OnToggleListener toggleListener, OnEditListener editListener) {
        this.userList = userList;
        this.toggleListener = toggleListener;
        this.editListener = editListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.nameTextView.setText(user.getName());
        holder.emailTextView.setText(user.getEmail());
        holder.roleTextView.setText(user.getRole());
        holder.statusSwitch.setChecked(user.isActive());

        // Toggle logic
        holder.statusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) { // Ensure it's a user-triggered action
                toggleListener.onToggle(position, isChecked);
            }
        });

        // Edit user logic (opens dialog when clicked)
        holder.itemView.setOnClickListener(v -> {
            if (editListener != null) {
                editListener.onEdit(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, emailTextView, roleTextView;
        Switch statusSwitch;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.user_name);
            emailTextView = itemView.findViewById(R.id.user_email);
            roleTextView = itemView.findViewById(R.id.user_role);
            statusSwitch = itemView.findViewById(R.id.user_status_switch);
        }
    }
}
