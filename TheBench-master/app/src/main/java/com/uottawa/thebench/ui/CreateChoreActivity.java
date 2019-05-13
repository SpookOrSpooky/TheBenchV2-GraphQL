package com.uottawa.thebench.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.uottawa.thebench.R;
import com.uottawa.thebench.model.Chore;
import com.uottawa.thebench.model.User;
import com.uottawa.thebench.utils.DBHandler;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class CreateChoreActivity extends AppCompatActivity {
    private DBHandler mDBHandler;

    private int mUserID;
    private boolean mIsEdit = false;
    private Chore mChore;

    private EditText mChoreName;
    private EditText mDescription;
    private EditText mDueDate;
    private EditText mDueTime;
    private EditText mPerson;
    private EditText mPoints;
    private EditText mResources;
    private TextView mRecurringText;
    private Spinner mRecurring;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chore);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // show back button in toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDBHandler = new DBHandler(this);

        mChoreName = (EditText) findViewById(R.id.chore_name_text);
        mDescription = (EditText) findViewById(R.id.chore_description_text);
        mDueDate = (EditText) findViewById(R.id.due_date);
        mDueTime = (EditText) findViewById(R.id.time_due);
        mPerson = (EditText) findViewById(R.id.person_text);
        mPoints = (EditText) findViewById(R.id.points_text);
        mResources = (EditText) findViewById(R.id.resources_text);
        mRecurringText = (TextView) findViewById(R.id.recurrence_text);
        mRecurring = (Spinner) findViewById(R.id.recurring_selection);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            mUserID = b.getInt("user_id", -1);
            mIsEdit = b.getBoolean("edit", false);
            if (mIsEdit) {
                setTitle("Edit Chore");
                int choreId = b.getInt("chore_id", -1);
                mChore = mDBHandler.findChore(choreId);                                             //DB Usage, findChore
                if (mChore != null) {
                    mChoreName.setText(mChore.getName());
                    mDescription.setText(mChore.getDescription());

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    mDueDate.setText(dateFormat.format(mChore.getDeadline()));

                    if (!mChore.isAllDay()) {
                        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
                        mDueTime.setText(timeFormat.format(mChore.getDeadline()));
                    }

                    if (mChore.hasAssignee()) {
                        mPerson.setText(mChore.getAssignee().getName());
                    }

                    mPoints.setText(Integer.toString(mChore.getPoints()));

                    mResources.setVisibility(View.GONE);
                    mRecurringText.setVisibility(View.GONE);
                    mRecurring.setVisibility(View.GONE);
                }
                else {
                    finish();
                }
            }
        }

        //Has dialog pop up when due date EditText is clicked
        mDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                if (mChore != null) {
                    cal.setTime(mChore.getDeadline());
                }
                DatePickerDialog dialog = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        mDueDate.setText(day + "/" + (month+1) + "/" + year);
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        //Has dialog pop up when due time EditText is clicked
        mDueTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cal = Calendar.getInstance();
                if (mChore != null && !mChore.isAllDay()) {
                    cal.setTime(mChore.getDeadline());
                }
                TimePickerDialog dialog = new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        String time = "AM";
                        String min = String.valueOf(minute);
                        if (hour > 12) {
                            time = "PM";
                            hour -= 12;
                        }
                        if (minute < 10) {
                            min = "0" + min;
                        }
                        mDueTime.setText(hour + ":" + min + " " + time);
                    }
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false);
                dialog.show();
            }
        });

        mPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());
                dialog.setTitle("Pick a User");
                List<User> users = mDBHandler.getUsers();
                final String[] user_name = new String[users.size()];
                //user_name[0] = "None";
                for (int i = 0; i < user_name.length; i++) {
                    user_name[i] = users.get(i).getName();
                }
                dialog.setSingleChoiceItems(user_name, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPerson.setText(user_name[i]);
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alert = dialog.create();
                alert.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_create_chore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        else if (item.getItemId() == R.id.action_submit_chore) {
            try {

                String[] date = mDueDate.getText().toString().split("/");
                boolean isAllDay = false;
                int hour;
                int minute;
                if (mDueTime.getText().toString().isEmpty()) {
                    isAllDay = true;

                    // If it's an all day deadline, make it the time the last hour and minutes
                    hour = 23;
                    minute = 59;
                }
                else {
                    String[] time = mDueTime.getText().toString().split(":");
                    hour = Integer.parseInt(time[0]);
                    minute = Integer.parseInt(time[1].split(" ")[0]);

                    if (time[1].split(" ")[1].trim().equals("PM")) {
                        hour += 12;
                    }
                }

                int points = Integer.parseInt(mPoints.getText().toString());
                if (points < 0) {
                    throw new IllegalArgumentException("Points must be positive!");
                }

                Chore.RecurrencePattern recurrence = Chore.RecurrencePattern.NONE;

                if (mRecurring.getSelectedItemPosition() == 1) {
                    recurrence = Chore.RecurrencePattern.DAILY;
                } else if (mRecurring.getSelectedItemPosition() == 2) {
                    recurrence = Chore.RecurrencePattern.WEEKLY;
                } else if (mRecurring.getSelectedItemPosition() == 3) {
                    recurrence = Chore.RecurrencePattern.MONTHLY;
                }

                Intent returnIntent = new Intent();
                long choreId;
                
                Date deadline = new GregorianCalendar(Integer.parseInt(date[2]), Integer.parseInt(date[1])-1, Integer.parseInt(date[0]), hour, minute).getTime();

                if (mIsEdit) {
                    mChore.setName(mChoreName.getText().toString().trim());
                    mChore.setDescription(mDescription.getText().toString());
                    mChore.setDeadline(deadline);
                    mChore.setIsAllDay(isAllDay);
                    mChore.setPoints(points);
                    mChore.setAssignee(mDBHandler.findUserByName(mPerson.getText().toString()));

                    boolean updated = mDBHandler.updateChore(mChore);
                    if (updated){
                        returnIntent.putExtra("chore_id", mChore.getId());
                        setResult(RESULT_OK, returnIntent);
                    }
                    else {
                        setResult(RESULT_CANCELED, returnIntent);
                    }
                }
                else {
                    Chore chore = new Chore(-1, mChoreName.getText().toString().trim(), mDescription.getText().toString(), deadline, isAllDay, points, Chore.Status.ACTIVE, recurrence, null, mDBHandler.findUser(mUserID));

                    if (!mResources.getText().toString().isEmpty()) {
                        String[] resources = mResources.getText().toString().split(",");
                        for (String resource : resources) {
                            resource = resource.trim();
                            if (!resource.isEmpty()) {
                                chore.addResource(-1, resource, false);
                            }
                        }
                    }

                    chore.setAssignee(mDBHandler.findUserByName(mPerson.getText().toString()));
                    choreId = mDBHandler.addChore(chore);

                    if (choreId >-1){
                        setResult(RESULT_OK, returnIntent);
                    }
                    else {
                        setResult(RESULT_CANCELED, returnIntent);
                    }
                }

                finish();
                
                return true;
            }
            catch (Exception e) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Invalid Entry", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
