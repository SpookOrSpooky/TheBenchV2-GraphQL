package com.uottawa.thebench.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.rohit.recycleritemclicksupport.RecyclerItemClickSupport;
import com.uottawa.thebench.R;
import com.uottawa.thebench.model.Admin;
import com.uottawa.thebench.model.Household;
import com.uottawa.thebench.model.User;
import com.uottawa.thebench.utils.DBHandler;
import com.uottawa.thebench.utils.Utils;

import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.uottawa.thebench.ui.MainActivity.RESULT_ADD_PERSON;
import static com.uottawa.thebench.ui.MainActivity.RESULT_EDIT_PERSON;

public class PeopleFragment extends Fragment {

    protected Activity mActivity;

    private DBHandler mDBHandler;
    private User mCurrentUser;

    private TextView mTitleText;
    private ImageButton mEditHouseholdImageButton;
    private RecyclerView mRecyclerView;
    private UsersAdapter mAdapter;
    private FloatingActionButton mAddPersonFab;

    public PeopleFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_people, container, false);

        mTitleText = (TextView) v.findViewById(R.id.people_title_text);
        mEditHouseholdImageButton = (ImageButton) v.findViewById(R.id.edit_household_image_button);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.people_recycler_view);
        mAddPersonFab = v.findViewById(R.id.add_person_fab);

        // Get the household object from the main activity and set the title text to it's name
        final Household household = ((MainActivity)mActivity).getHousehold(false);
        mTitleText.setText(household.getName() + " Household");

        // Create and set the layout manager for the recycler view
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(mActivity);
        mRecyclerView.setLayoutManager(layoutManager);

        // Add a line separator for the recycler view
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL));

        mDBHandler = new DBHandler(mActivity);

        // Get the list of users of the household
        List<User> users = mDBHandler.getUsers();

        // Get the current user from the Main Activity
        mCurrentUser = ((MainActivity)mActivity).getCurrentUser();

        // Create a new adapter with the list of users, and set the adapter to the recycler view
        mAdapter = new UsersAdapter(mActivity, users, mCurrentUser);
        mRecyclerView.setAdapter(mAdapter);

        if (mCurrentUser != null && mCurrentUser.isAdmin()) {
            // Show an edit button for the household name and an add person button
            mEditHouseholdImageButton.setVisibility(View.VISIBLE);
            mAddPersonFab.setVisibility(View.VISIBLE);

            // If the current user is an admin, make the recycler view clickable for edit and delete
            RecyclerItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new RecyclerItemClickSupport.OnItemClickListener() {
                @Override
                public void onItemClicked(RecyclerView recyclerView, final int position, View v) {
                    final User user = mAdapter.getUsers().get(position);
                    CharSequence options[];
                    if (mCurrentUser.getId() == user.getId()) {
                        options = new CharSequence[]{"Edit"};
                    }
                    else {
                        options = new CharSequence[]{"Edit", "Delete"};
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setTitle(user.getName());
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) { // edit selected, open activity
                                Intent intent = new Intent(mActivity, AddPersonActivity.class);
                                Bundle b = new Bundle();
                                b.putBoolean("edit", true);
                                b.putInt("userId", user.getId());
                                intent.putExtras(b);
                                mActivity.startActivityForResult(intent, RESULT_EDIT_PERSON);
                            }
                            else if (which == 1) { // delete selected, show confirm dialog
                                AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
                                alert.setTitle("Delete");
                                alert.setMessage("Are you sure you want to delete?");
                                alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        boolean deleted = mDBHandler.deleteUser(user);
                                        dialog.dismiss();

                                        if (deleted) {
                                            mAdapter.removeUser(user, position);
                                            Snackbar snackbar = Snackbar.make(mActivity.findViewById(android.R.id.content),
                                                    user.getName() + " has been deleted!", Snackbar.LENGTH_SHORT);
                                            snackbar.show();
                                        }
                                    }
                                });

                                alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                alert.show();
                            }
                        }
                    });
                    builder.show();
                }
            });
        }

        // Click listener for editing a household name (show a dialog with an EditText field)
        mEditHouseholdImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // help for following dialog code from
                // https://stackoverflow.com/a/10904665/2341126
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle("Enter Household Name");

                FrameLayout container = new FrameLayout(mActivity);
                final EditText input = new EditText(mActivity);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(20)});
                input.setText(household.getName());

                int margin = (int) Utils.convertDpToPixel(24, mActivity);
                FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.leftMargin = margin;
                params.rightMargin = margin;
                input.setLayoutParams(params);
                container.addView(input);
                builder.setView(container);

                builder.setPositiveButton("Submit", null);

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
                        String householdName = input.getText().toString().trim();
                        if(!householdName.isEmpty() && !household.getName().equals(householdName)) {
                            DBHandler dbHandler = new DBHandler(mActivity);
                            household.setName(householdName);
                            boolean updated = dbHandler.updateHousehold(household);

                            if (updated) {
                                mTitleText.setText(householdName + " Household");
                                Snackbar snackbar = Snackbar.make(mActivity.findViewById(android.R.id.content), "Household name updated!", Snackbar.LENGTH_SHORT);
                                snackbar.show();
                            }
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        // Start the Add Person Activity when the button is clicked
        mAddPersonFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity, AddPersonActivity.class);
                mActivity.startActivityForResult(intent, RESULT_ADD_PERSON);
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            mActivity = (Activity) context;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Update the list of users in the adapter after a person is added or edited
        if ((requestCode == RESULT_ADD_PERSON || requestCode == RESULT_EDIT_PERSON) && resultCode == RESULT_OK) {
            mAdapter.setUsers(mDBHandler.getUsers());
            mCurrentUser = ((MainActivity)mActivity).getCurrentUser();
        }

    }

}
