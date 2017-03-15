package com.reptile.nomad.ReptileApp.Adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.reptile.nomad.ReptileApp.Models.User;
import com.reptile.nomad.ReptileApp.R;

import java.util.List;

/**
 * Created by sankarmanoj on 25/05/16.
 */
public class SearchUserRecyclerAdapter extends RecyclerView.Adapter<SearchUserRecyclerAdapter.SearchUserViewHolder> {
    public List<User> userList;
    ItemClickEvent itemClickEvent;
    public SearchUserRecyclerAdapter(List<User> userList,ItemClickEvent itemClick) {
        this.userList = userList;
        itemClickEvent = itemClick;
    }

    public interface ItemClickEvent
    {
        void onItemClick(User selectedUser);
    }
    @Override
    public int getItemCount() {
       // Log.d("Search Adapter",String.valueOf(userList.size()));
        return userList.size();
    }

    @Override
    public SearchUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_user_card,null);
        return new SearchUserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final SearchUserViewHolder holder, final int position) {
        holder.nameTextView.setText(userList.get(position).userName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.nameTextView.setBackgroundColor(Color.parseColor("#CCCCCC"));
                itemClickEvent.onItemClick(userList.get(position));


            }
        });
    }

    public class SearchUserViewHolder extends RecyclerView.ViewHolder
    {
        TextView nameTextView;
        View thisView;
        public SearchUserViewHolder(View itemView) {
            super(itemView);
            thisView = itemView;
            nameTextView = (TextView)itemView.findViewById(R.id.searchUserNameTextView);
        }
    }
}
