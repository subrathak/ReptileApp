package com.reptile.nomad.ReptileApp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.reptile.nomad.ReptileApp.DetailedViewActivity;
import com.reptile.nomad.ReptileApp.Models.Comment;
import com.reptile.nomad.ReptileApp.Models.Task;
import com.reptile.nomad.ReptileApp.QuickPreferences;
import com.reptile.nomad.ReptileApp.R;
import com.reptile.nomad.ReptileApp.Reptile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.socket.emitter.Emitter;

/**
 * Created by nomad on 8/8/16.
 */

public class CommentsRecycerAdapterFooter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int FOOTER_VIEW = 1;
    public Task thisTask;
    public Comment thisComment;
    public List<Comment> taskComments;
    ImageLoader imageLoader = Reptile.getInstance().getImageLoader();
    public Context context;

    public CommentsRecycerAdapterFooter(List<Comment> comments, Task task, Context contxt) {
        thisTask = task;
        context = contxt;
        try {
            taskComments = comments;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

// Define a view holder for Footer view

    public class FooterViewHolder extends ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Do whatever you want on clicking the item
                }
            });
        }
    }

    // Now define the viewholder for Normal list item
    public class NormalViewHolder extends ViewHolder {
        public NormalViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Do whatever you want on clicking the normal items
                }
            });
        }
    }

// And now in onCreateViewHolder you have to pass the correct view
// while populating the list item.

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v;

        if (viewType == FOOTER_VIEW) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_card_hybrid, parent, false);

            FooterViewHolder vh = new FooterViewHolder(v);

            return vh;
        }

        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_card, parent, false);

        NormalViewHolder vh = new NormalViewHolder(v);

        return vh;
    }

    // Now bind the viewholders in onBindViewHolder
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        try {
            if (holder instanceof NormalViewHolder) {
                NormalViewHolder vh = (NormalViewHolder) holder;

                Comment thisComment = taskComments.get(position);
                String userName = thisComment.getAuthorname();
                Calendar commentCreationTime = thisComment.getCreated();
                Log.d("timeZone",commentCreationTime.toString()+ " of " + thisComment.getComment());
//                commentCreationTime.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
                TimeZone timeZone= TimeZone.getTimeZone("IST");
                commentCreationTime.setTimeZone(TimeZone.getDefault());
                String deadlineString = new SimpleDateFormat("E , d MMM yy", Locale.UK).format(commentCreationTime.getTime());
                vh.name.setText(userName);
                vh.comment.setText(thisComment.getComment());
                if(imageLoader == null)
                    imageLoader = Reptile.getInstance().getImageLoader();
                String imageURL = thisComment.author.imageURI;
                vh.commenterPicture.setImageUrl(imageURL,imageLoader);
                vh.creadTimeComment.setReferenceTime(commentCreationTime.getTimeInMillis());
//                vh.bindView(position);
            } else if (holder instanceof FooterViewHolder) {
                final FooterViewHolder vh = (FooterViewHolder) holder;
//                Comment thisComment = taskComments.get(position);
//                String userName = thisComment.getAuthorname();
                String userName = Reptile.mUser.getUserName();
                vh.name.setText(userName);
//                vh.writeComment.setText("Write comment...");
                vh.saveCommentButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String newComment = vh.writeComment.getText().toString();
                        if(newComment.replace(" ","").length()<3)
                        {
                            Toast.makeText(context,"Comment is too small", Toast.LENGTH_LONG).show();
                            return;
                        }
                        thisComment = new Comment(newComment,Reptile.mUser,thisTask);
                        taskComments.add(thisComment);
                        Reptile.mSocket.emit("createcomment",thisComment.getCreationJSON());
//                postComment.setEnabled(false);
                        vh.writeComment.setText("");
//                        vh.itemView.setBackgroundResource(R.color.material_grey500);
                        Reptile.mSocket.on("createcomment", new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                String reply = (String)args[0];
                                switch (reply)
                                {
                                    case "success":
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Toast.makeText(getApplicationContext(),"Successfully Created Comment",Toast.LENGTH_SHORT).show();
//                                        postComment.setEnabled(true);

//                                        Reptile.mSocket.emit("loadcomments",thisTask.id);
                                        thisTask.commentCount+=1;
                                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(vh.writeComment.getWindowToken(), 0);


                                        Reptile.mSocket.emit("loadcomments",thisTask.id);
//                                    }
//                                });
//                                loadComments();
                                        Reptile.mSocket.off("createcomment");
                                        break;
                                    case "error":
//
                                        break;
                                }
                            }

                        });
                    }
                });
                if(imageLoader == null)
                    imageLoader = Reptile.getInstance().getImageLoader();
                String imageURL = Reptile.mUser.imageURI;
                vh.commenterPicture.setImageUrl(imageURL,imageLoader);
//        if(position == taskComments.size()-1){
//            holder.comment.setVisibility(View.GONE);
//            holder.creadTimeComment.setVisibility(View.GONE);
//        }else {
//            holder.writeComment.setVisibility(View.GONE);
//            holder.saveCommentButton.setVisibility(View.GONE);
//        }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

// Now the critical part. You have return the exact item count of your list
// I've only one footer. So I returned taskComments.size() + 1
// If you've multiple headers and footers, you've to return total count
// like, headers.size() + taskComments.size() + footers.size()

    @Override
    public int getItemCount() {
        if (taskComments == null) {
            return 0;
        }

        if (taskComments.size() == 0) {
            //Return 1 here to show nothing
            return 1;
        }

        // Add extra view to show the footer view
        return taskComments.size() + 1;
    }

// Now define getItemViewType of your own.

    @Override
    public int getItemViewType(int position) {
        if (position == taskComments.size()) {
            // This is where we'll add footer.
            return FOOTER_VIEW;
        }

        return super.getItemViewType(position);
    }

// So you're done with adding a footer and its action on onClick.
// Now set the default ViewHolder for NormalViewHolder

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Define elements of a row here
        public TextView name;
        public TextView comment;
        public NetworkImageView commenterPicture;
        public RelativeTimeTextView creadTimeComment;
        public ImageButton saveCommentButton;
        public EditText writeComment;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView)itemView.findViewById(R.id.commentNameTextView);
            comment = (TextView)itemView.findViewById(R.id.commentTextView);
            commenterPicture = (NetworkImageView)itemView.findViewById(R.id.commenterProfileImageView);
            creadTimeComment = (RelativeTimeTextView)itemView.findViewById(R.id.createdTimeComment);
            saveCommentButton = (ImageButton)itemView.findViewById(R.id.button_submit_comment);
            writeComment = (EditText)itemView.findViewById(R.id.writeCommentEditText);
        }

        public void bindView(int position) {
            // bindView() method to implement actions
        }
    }
}
