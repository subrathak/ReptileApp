package com.reptile.nomad.changedReptile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.reptile.nomad.changedReptile.Models.Group;
import com.reptile.nomad.changedReptile.Adapters.GroupListRecyclerAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.socket.emitter.Emitter;

public class ManageGroups extends Activity {
    public RecyclerView groupListView;
    @Bind(R.id.addGroupButton)
    Button AddGroupButton;
    Activity mActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_groups);
        ButterKnife.bind(this);
        mActivity = this;
        groupListView = (RecyclerView) findViewById(R.id.groupList);
        final List<Group> groups = new ArrayList<>( Reptile.mUserGroups.values());
        final GroupListRecyclerAdapter GroupAdapter = new GroupListRecyclerAdapter(groups,this);
        groupListView.setAdapter(GroupAdapter);
        groupListView.setLayoutManager(new LinearLayoutManager(this));
        AddGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Reptile.mSocket.connected()==false)
                {
                    Reptile.mSocket.connect();
                    Toast.makeText(getApplicationContext(),"Unable to connect to Server\n Try Later",Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                final View createGroupView =mActivity.getLayoutInflater().inflate(R.layout.create_group_alert,null);
                builder.setView(createGroupView);

                final AlertDialog dialog = builder.create();

                final EditText GroupNameEditText = (EditText) createGroupView.findViewById(R.id.createGroupEditText);

                createGroupView.findViewById(R.id.createGroupButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Group newGroup = new Group(GroupNameEditText.getText().toString());
                        dialog.setCancelable(false);
                         Reptile.mSocket.emit("create-group",newGroup.getJSON());
                        Reptile.mSocket.on("create-group", new Emitter.Listener() {
                            @Override
                            public void call(Object... args) {
                                Reptile.mSocket.off("create-group");
                                try {
                                    JSONObject input =  (JSONObject)(args[0]);
                                    Log.d("Create Group Result",input.getString("result"));
                                    if(input.getString("result").equals("success"))
                                    {

                                        String id = input.getString("id");
                                        newGroup.id  = id;
                                        Reptile.mUserGroups.put(newGroup.id,newGroup);
                                        GroupAdapter.groups = new ArrayList<Group>(Reptile.mUserGroups.values());
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                GroupAdapter.notifyDataSetChanged();
                                            }
                                        });
                                        dialog.dismiss();
                                    }
                                    else
                                    {
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(),"Creating Task Failed",Toast.LENGTH_SHORT).show();
                                    }
                                }
                                catch (JSONException e){e.printStackTrace();
                                    dialog.dismiss();
                                    Log.d("CreateGroupFromServer",(String)args[0]);
                                    Toast.makeText(getApplicationContext(),"Creating Task Failed",Toast.LENGTH_SHORT).show();}


                            }
                        });


                    }
                });
                dialog.show();
            }
        });

    }
}
