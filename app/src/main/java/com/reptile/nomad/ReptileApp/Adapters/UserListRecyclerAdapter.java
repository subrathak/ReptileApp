package com.reptile.nomad.ReptileApp.Adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.reptile.nomad.ReptileApp.Models.User;
import com.reptile.nomad.ReptileApp.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sankarmanoj on 04/06/16.
 */
public class UserListRecyclerAdapter extends RecyclerView.Adapter<UserListRecyclerAdapter.UserListViewHolder> {
    LinkedHashMap<String, User> users;
    Activity mActivity;
    OnDeleteUser onDeleteUser;
    public interface OnDeleteUser
    {
        public void onDelete(User user);
    }

    public final String TAG = "UserList Recycler";
    @Override
    public int getItemCount() {
        return users.size();
    }

    public UserListRecyclerAdapter(LinkedHashMap<String, User> users,Activity mActivity,OnDeleteUser onDeleteUser) {
        this.mActivity = mActivity;
        this.onDeleteUser = onDeleteUser;
        this.users = users;
    }

    @Override
    public UserListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_user_card,null);
        return new UserListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final UserListViewHolder holder, final int position) {
       final User thisUser = new ArrayList<>(users.values()).get(position);
        holder.nameTextView.setText(thisUser.userName);
        Log.d(TAG,"Position "+ position + " User "+thisUser.userName);
        holder.deleteUserTextView.setVisibility(View.GONE);
        holder.deleteUserTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                users.remove(thisUser.id);
                holder.deleteUserTextView.setOnClickListener(null);
                notifyItemRemoved(position);
                onDeleteUser.onDelete(thisUser);
                Timer updated = new Timer();
                updated.schedule(new TimerTask() {
                    @Override
                    public void run() {
                     mActivity.runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             notifyDataSetChanged();
                         }
                     });
                    }
                },500);


            }
        });
        holder.itemView.setLongClickable(true);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                holder.deleteUserTextView.setVisibility(View.VISIBLE);
                return true;
            }
        });

    }

    public class UserListViewHolder extends RecyclerView.ViewHolder
    {     TextView nameTextView;
        View thisView;
        TextView deleteUserTextView;
        public UserListViewHolder(View itemView) {
            super(itemView);
            thisView = itemView;
            deleteUserTextView = (TextView)itemView.findViewById(R.id.deleteUserTextView);
            nameTextView = (TextView)itemView.findViewById(R.id.searchUserNameTextView);
        }
    }
}
