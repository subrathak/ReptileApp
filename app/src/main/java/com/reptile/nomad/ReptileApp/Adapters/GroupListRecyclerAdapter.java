package com.reptile.nomad.ReptileApp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.reptile.nomad.ReptileApp.EditGroup;
import com.reptile.nomad.ReptileApp.Models.Group;
import com.reptile.nomad.ReptileApp.R;

import java.util.List;

/**
 * Created by sankarmanoj on 04/06/16.
 */
public class GroupListRecyclerAdapter extends RecyclerView.Adapter<GroupListRecyclerAdapter.GroupListViewHolder> {
    public List<Group> groups;
    public Context mContext;
    public final static  String TAG = "GroupListRecylerAdapter";
    public GroupListRecyclerAdapter(List<Group> groups,Context mContext) {
        this.mContext = mContext;
        this.groups = groups;
    }

    public class GroupListViewHolder extends RecyclerView.ViewHolder
    {
        public TextView Name;
        public View Line;
        public GroupListViewHolder(View itemView) {
            super(itemView);
            Name = (TextView)itemView.findViewById(R.id.groupNameListTextView);
            Line = itemView.findViewById(R.id.groupSeperatingLine);
            if(Name==null)
            {
                throw new AssertionError("Name can't be null");
            }
        }
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    @Override
    public GroupListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_card,parent,false);
        return new GroupListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(GroupListViewHolder holder, final int position) {

     holder.Name.setText(groups.get(position).name);
        if(position==groups.size()-1)
        {
            holder.Line.setVisibility(View.INVISIBLE);
        }
        else {
            holder.Line.setVisibility(View.VISIBLE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showDetailedGroup = new Intent(mContext, EditGroup.class);
                showDetailedGroup.putExtra("group",position);
                mContext.startActivity(showDetailedGroup);
            }
        });
    }
}
