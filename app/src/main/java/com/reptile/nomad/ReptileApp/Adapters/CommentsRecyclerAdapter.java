package com.reptile.nomad.ReptileApp.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.reptile.nomad.ReptileApp.Models.Comment;
import com.reptile.nomad.ReptileApp.R;
import com.reptile.nomad.ReptileApp.Reptile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by nomad on 22/5/16.
 */
public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.CommentsViewHolder> {

    public List<Comment> taskComments;
    ImageLoader imageLoader = Reptile.getInstance().getImageLoader();

    public CommentsRecyclerAdapter(List<Comment> comments) {
        try {
            taskComments = comments;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public CommentsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_card,parent,false);
        return new CommentsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CommentsViewHolder holder, int position) {
        Comment thisComment = taskComments.get(position);
        String userName = thisComment.getAuthorname();
        Calendar deadline = thisComment.getCreated();
        String deadlineString = new SimpleDateFormat("E , d MMM yy", Locale.UK).format(deadline.getTime());
        holder.name.setText(userName);
        holder.comment.setText(thisComment.getComment());
        if(imageLoader == null)
            imageLoader = Reptile.getInstance().getImageLoader();
        String imageURL = thisComment.author.imageURI;
        holder.commenterPicture.setImageUrl(imageURL,imageLoader);
        holder.creadTimeComment.setText(deadlineString);

    }


    @Override
    public int getItemCount() {
        return taskComments.size();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView comment;
        public NetworkImageView commenterPicture;
        public TextView creadTimeComment;

        public CommentsViewHolder(View itemView) {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.commentNameTextView);
            comment = (TextView)itemView.findViewById(R.id.commentTextView);
            commenterPicture = (NetworkImageView)itemView.findViewById(R.id.commenterProfileImageView);
            creadTimeComment = (TextView)itemView.findViewById(R.id.createdTimeComment);
        }
    }
}