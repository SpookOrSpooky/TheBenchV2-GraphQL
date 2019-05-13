package com.uottawa.thebench.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.uottawa.thebench.R;
import com.uottawa.thebench.model.Chore;
import com.uottawa.thebench.model.Resource;
import com.uottawa.thebench.utils.DBHandler;
import com.uottawa.thebench.utils.Utils;

import org.joda.time.DateTime;

public class ViewChoreActivity extends AppCompatActivity {

    private DBHandler mDBHandler;

    private Chore mChore;
    private boolean mIsAdmin;
    private boolean mUpdated = false;

    private TextView mChoreNameText;
    private TextView mChoreDescription;
    private TextView mChoreDeadlineText;
    private TextView mRecurrenceText;
    private TextView mStatusText;
    private TextView mPointsText;
    private TextView mAssigneeNameText;
    private TextView mCreatorNameText;
    public ExpandableHeightGridView mResourcesGridView;
    public ImageButton mAddResourceImageButton;
    private ResourcesAdapter mResourcesAdapter;

    public static final int RESULT_EDIT_CHORE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_chore);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // show back button in toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Initializes class variables and sets text
        mDBHandler = new DBHandler(this);

        mIsAdmin = (boolean)getIntent().getExtras().get("admin");
        mChore = mDBHandler.findChore((int)getIntent().getExtras().get("chore_id"));

        mChoreNameText = (TextView) findViewById(R.id.chore_name_text);
        mChoreDescription = (TextView) findViewById(R.id.chore_description_text);
        mChoreDeadlineText = (TextView) findViewById(R.id.deadline_text);
        mRecurrenceText = (TextView) findViewById(R.id.recurrence_text);
        mStatusText = (TextView) findViewById(R.id.status_text);
        mPointsText = (TextView) findViewById(R.id.points_text);
        mAssigneeNameText = (TextView) findViewById(R.id.assignee_name_text);
        mCreatorNameText = (TextView) findViewById(R.id.creator_name_text);

        mResourcesGridView = (ExpandableHeightGridView) findViewById(R.id.resources_grid_view);
        mResourcesGridView.setExpanded(true);
        mAddResourceImageButton = (ImageButton) findViewById(R.id.add_resource_image_button);

        displayChoreDetails();

        // Set the click listener for the add resource button
        mAddResourceImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // help for following dialog code from
                // https://stackoverflow.com/a/10904665/2341126
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewChoreActivity.this);
                builder.setTitle("Enter Resource Name");

                FrameLayout container = new FrameLayout(ViewChoreActivity.this);
                final EditText input = new EditText(ViewChoreActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(30)});

                int margin = (int) Utils.convertDpToPixel(24, ViewChoreActivity.this);
                FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = margin;
                params.rightMargin = margin;
                input.setLayoutParams(params);
                container.addView(input);
                builder.setView(container);

                builder.setPositiveButton("Add Resource", null);

                final AlertDialog dialog = builder.create();

                // Show keyboard
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                // Show the dialog
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        String resourceName = input.getText().toString();
                        // Verify that the input is not empty
                        if(!resourceName.isEmpty()) {
                            // If the last resource in the list of resources is null, remove it
                            int lastResourceIndex = mChore.getResources().size() - 1;
                            if (lastResourceIndex > 0 && mChore.getResources().get(lastResourceIndex) == null) {
                                mChore.getResources().remove(lastResourceIndex);
                            }

                            DBHandler dbHandler = new DBHandler(ViewChoreActivity.this);
                            Resource resource = new Resource(-1, resourceName, false, mChore);
                            long resourceId = dbHandler.addResource(resource);

                            if (resourceId > 0) {
                                resource.setId((int)resourceId);

                                // If odd number of resources, add extra empty one to fill gridview
                                if((mChore.getResources().size() % 2) != 0) {
                                    mChore.getResources().add(null);
                                }

                                // Update the resources adapter for this chore
                                mResourcesAdapter.notifyDataSetChanged();

                                // Notify the user that the resource was added
                                Snackbar snackbar = Snackbar.make((ViewChoreActivity.this).findViewById(android.R.id.content),
                                        "\"" + resourceName + "\" resource added!", Snackbar.LENGTH_SHORT);
                                snackbar.show();
                            }
                        }
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        else if (item.getItemId() == R.id.action_edit_chore) {
            Intent intent = new Intent(this, CreateChoreActivity.class);
            intent.putExtra("edit", true);
            intent.putExtra("chore_id", mChore.getId());
            startActivityForResult(intent, RESULT_EDIT_CHORE);
        }
        else if (item.getItemId() == R.id.action_delete_chore) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Delete");
            alert.setMessage("Are you sure you want to delete?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean deleted = mDBHandler.deleteChore(mChore);
                    Intent returnIntent = new Intent();
                    if (deleted) {
                        setResult(RESULT_OK, returnIntent);
                    }
                    dialog.dismiss();
                    finish();
                }
            });

            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alert.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        if (mIsAdmin) {
            getMenuInflater().inflate(R.menu.menu_view_chore, menu);
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_EDIT_CHORE && resultCode == RESULT_OK) {
            mUpdated = true;
            int choreId = data.getIntExtra("chore_id", -1);
            mChore = mDBHandler.findChore(choreId);
            displayChoreDetails();

            Intent returnIntent = new Intent();
            if (mUpdated) {
                setResult(RESULT_OK, returnIntent);
            }
            else {
                setResult(RESULT_CANCELED, returnIntent);
            }
        }
    }

    private void displayChoreDetails() {
        mChoreNameText.setText(mChore.getName());
        if (mChore.getDescription().isEmpty()) {
            mChoreDescription.setText("No description");
        }
        else {
            mChoreDescription.setText(mChore.getDescription());
        }
        DateTime deadline = new DateTime(mChore.getDeadline());
        if (Utils.isPast(deadline)) { // A past deadline should be red
            mChoreDeadlineText.setTextColor(ContextCompat.getColor(this, R.color.red_text));
        }
        else {
            mChoreDeadlineText.setTextColor(ContextCompat.getColor(this, R.color.primaryText));
        }
        mChoreDeadlineText.setText(Utils.getDeadlineString(deadline, mChore.isAllDay()));

        switch (mChore.getRecurrencePattern()) {
            case NONE:
                mRecurrenceText.setText("Not Recurring");
                break;
            case DAILY:
                mRecurrenceText.setText("Recurring Daily");
                break;
            case WEEKLY:
                mRecurrenceText.setText("Recurring Weekly");
                break;
            case MONTHLY:
                mRecurrenceText.setText("Recurring Monthly");
                break;
        }

        switch (mChore.getStatus()) {
            case ACTIVE:
                mStatusText.setText("Active");
                break;
            case POSTPONED:
                mStatusText.setText("Postponed");
                break;
            case COMPLETED:
                mStatusText.setText("Completed");
                break;
        }

        mPointsText.setText(mChore.getPoints() + " Points");

        if (mChore.hasAssignee()) {
            mAssigneeNameText.setText(mChore.getAssignee().getName());
        }
        else {
            mAssigneeNameText.setText("-");
        }

        if (mChore.hasCreator()) {
            mCreatorNameText.setText(mChore.getCreator().getName());
        }
        else {
            mCreatorNameText.setText("-");
        }

        boolean editableResources = mChore.getStatus() != Chore.Status.COMPLETED;
        if (mResourcesAdapter == null) {
            mResourcesAdapter = new ResourcesAdapter(this, mChore.getResources(), ContextCompat.getColor(this, R.color.background), editableResources);
            mResourcesGridView.setAdapter(mResourcesAdapter);
        }

        if (!editableResources) {
            mAddResourceImageButton.setVisibility(View.INVISIBLE);
        }
    }

}
