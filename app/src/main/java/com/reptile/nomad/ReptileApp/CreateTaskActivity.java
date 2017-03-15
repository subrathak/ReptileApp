package com.reptile.nomad.ReptileApp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.reptile.nomad.ReptileApp.Models.Group;
import com.reptile.nomad.ReptileApp.Models.Task;

import org.json.JSONObject;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import info.hoang8f.android.segmented.SegmentedGroup;
import io.socket.emitter.Emitter;


public class CreateTaskActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    SegmentedGroup deadlineOptions;

    Toolbar toolbar ;
    Activity mActivity;
    @Bind(R.id.createTaskButton)
    ImageButton createTaskButton;
    @Bind(R.id.createTaskStringEditText)
    EditText createTaskStringEditText;
    @Bind(R.id.createTaskDeadlineTimeTextView)
    TextView createTaskDeadlineTimeTextView;
    @Bind(R.id.createTaskDeadlineDateTextView)
    TextView createTaskDeadlineDateTextView;
    @Bind(R.id.customRadioButton)
    RadioButton customRadioButton;
    @Bind(R.id.publicRadioButton)
    RadioButton publicRadioButton;

    @Bind(R.id.taskDeadlineRadioGroup)
            SegmentedGroup deadlineRadioGroup;
    Boolean publicTask=true;
    Calendar deadline;
    public static final String TAG ="CreateTaskActivity";
    Boolean m24HourView = true;
    List<Group> selectedGroups;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        ButterKnife.bind(this);
        deadlineOptions = (SegmentedGroup)findViewById(R.id.taskDeadlineRadioGroup);
        deadlineOptions.setOnCheckedChangeListener(this);
        mActivity = this;

        deadline = Calendar.getInstance();
        toolbar = (Toolbar) findViewById(R.id.createTaskToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        publicRadioButton.setChecked(true);

        final Calendar now = Calendar.getInstance();
        createTaskDeadlineDateTextView.setText(new SimpleDateFormat("E , d MMM yy", Locale.UK).format(deadline.getTime()));
        deadline.add(Calendar.HOUR,2);
        deadlineRadioGroup.check(R.id.twoHours);
        createTaskDeadlineTimeTextView.setText(new SimpleDateFormat("h:m a",Locale.UK).format(deadline.getTime()));
        createTaskDeadlineTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog mTimePicker = new TimePickerDialog(mActivity, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        deadline.set(Calendar.MINUTE,minute);
                        deadline.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        createTaskDeadlineTimeTextView.setText(new SimpleDateFormat("h:m a",Locale.UK).format(deadline.getTime()));
                    }
                },deadline.get(Calendar.HOUR),deadline.get(Calendar.MINUTE),m24HourView);
                mTimePicker.show();
            }
        });
        createTaskDeadlineDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog mDatePicker = new DatePickerDialog(mActivity, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        deadline.set(Calendar.YEAR,year);
                        deadline.set(Calendar.MONTH,monthOfYear);
                        deadline.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                        createTaskDeadlineDateTextView.setText(new SimpleDateFormat("E , d MMM yy", Locale.UK).format(deadline.getTime()));
                    }
                },deadline.get(Calendar.YEAR),deadline.get(Calendar.MONTH),deadline.get(Calendar.DAY_OF_MONTH));
                mDatePicker.show();
            }
        });

        createTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("CreateTaskLog",deadline.toString());
                if(!Reptile.mSocket.connected())
                {
                    AlertDialog notConnectedDialog = new AlertDialog.Builder(mActivity)
                            .setTitle("Unable to Connected to Server")
                            .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    finish();
                                }
                            }).create();
                    notConnectedDialog.show();
                }
                String TaskString = createTaskStringEditText.getText().toString();
                if(TaskString.replace(" ","") .length()<3)
                {
                    Toast.makeText(getApplicationContext(),"Empty Task String",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(deadline.before(now))
                {
                    Toast.makeText(getApplicationContext(),"Please enable time travelling to create deadlines in the past",Toast.LENGTH_LONG).show();
                    return;
                }
                Task newTask = new Task(Reptile.mUser,TaskString,now,deadline);

                newTask.publictask = publicTask;
                newTask.visibleTo = selectedGroups;
                newTask.status = "active";
                JSONObject taskJson = newTask.getJSON();
                Reptile.mSocket.emit("createtask",taskJson);
                createTaskButton.setEnabled(false);
                Reptile.mSocket.on("createtask", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        String reply = (String)args[0];
                        Log.d(TAG,"Reply From Server = "+reply);
                        switch (reply)
                        {
                            case "success":
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),"Task Created Successfully",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Reptile.mSocket.emit("addtasks");
                                TimerTask finishActivity = new TimerTask() {
                                    @Override
                                    public void run() {
                                        finish();
                                    }
                                };
                                Timer timer = new Timer();
                                timer.schedule(finishActivity,1000);
                                Reptile.mSocket.off("createtask");
                                break;
                            case "error":
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),"Error Creating Task",Toast.LENGTH_LONG).show();
                                        createTaskButton.setEnabled(true);
                                    }
                                });


                                break;
                        }
                    }
                });
            }
        });
        publicRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publicTask = true;

            }
        });
        customRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                publicTask = false;
            }
        });
//        customRadioButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                publicTask=false;
//                AlertDialog.Builder builder = new AlertDialog.Builder(
//                        CreateTaskActivity.this);
//                selectedGroups = new ArrayList<>();
//                builder.setTitle("Select a group :");
//                final List<String> groups = new ArrayList<String>();
//                for (Group group : Reptile.mUserGroups.values())
//                {
//                    groups.add(group.name);
//                }
//                builder.setMultiChoiceItems(groups.toArray(new CharSequence[groups.size()]), null, new DialogInterface.OnMultiChoiceClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                        if(isChecked)
//                        {
//                            selectedGroups.add(Reptile.mUserGroups.get(which));
//                        }
//                    }
//                });
//
//                builder.show();
//            }
//        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!Reptile.mSocket.connected())
        {
            Reptile.mSocket.connect();
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.fiveMinutes:
                deadline = Calendar.getInstance();
                deadline.add(Calendar.MINUTE,5);
                createTaskDeadlineTimeTextView.setText(new SimpleDateFormat("h:m a",Locale.UK).format(deadline.getTime()));
                createTaskDeadlineDateTextView.setText(new SimpleDateFormat("E , d MMM yy", Locale.UK).format(deadline.getTime()));
                break;
            case R.id.fifteenMinutes:
                deadline = Calendar.getInstance();
                deadline.add(Calendar.MINUTE,15);
                createTaskDeadlineTimeTextView.setText(new SimpleDateFormat("h:m a",Locale.UK).format(deadline.getTime()));
                createTaskDeadlineDateTextView.setText(new SimpleDateFormat("E , d MMM yy", Locale.UK).format(deadline.getTime()));
                break;
            case R.id.fortyfiveMinutes:
                deadline = Calendar.getInstance();
                deadline.add(Calendar.MINUTE,45);
                createTaskDeadlineTimeTextView.setText(new SimpleDateFormat("h:m a",Locale.UK).format(deadline.getTime()));
                createTaskDeadlineDateTextView.setText(new SimpleDateFormat("E , d MMM yy", Locale.UK).format(deadline.getTime()));
                break;
            case R.id.twoHours:
                deadline = Calendar.getInstance();
                deadline.add(Calendar.HOUR_OF_DAY,2);
                createTaskDeadlineTimeTextView.setText(new SimpleDateFormat("h:m a",Locale.UK).format(deadline.getTime()));
                createTaskDeadlineDateTextView.setText(new SimpleDateFormat("E , d MMM yy", Locale.UK).format(deadline.getTime()));
                break;
            case R.id.oneDay:
                deadline = Calendar.getInstance();
                deadline.add(Calendar.DATE,1);
                createTaskDeadlineTimeTextView.setText(new SimpleDateFormat("h:m a",Locale.UK).format(deadline.getTime()));
                createTaskDeadlineDateTextView.setText(new SimpleDateFormat("E , d MMM yy", Locale.UK).format(deadline.getTime()));
                break;

        }

    }
}