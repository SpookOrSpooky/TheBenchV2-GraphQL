package com.uottawa.thebench.ui;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.rohit.recycleritemclicksupport.RecyclerItemClickSupport;
import com.uottawa.thebench.R;
import com.uottawa.thebench.model.Admin;
import com.uottawa.thebench.model.User;
import com.uottawa.thebench.utils.DBHandler;
import com.uottawa.thebench.utils.PrefUtils;
import com.uottawa.thebench.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class SelectProfileActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private UsersAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_profile);

        mRecyclerView = (RecyclerView) findViewById(R.id.people_recycler_view);

        // Create and set the layout manager for the recycler view
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        // Add a line separator for the recycler view
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL));

        DBHandler dbHandler = new DBHandler(this);
        // Get the list of users
        List<User> users = dbHandler.getUsers();
        if (users == null) {
            users = new ArrayList<>();
        }
        //Utils.createRandomChores(this, users); // uncomment this line to generate random chores

        // Create a new adapter with the list of users, and set the adapter to the recycler view
        mAdapter = new UsersAdapter(this, users, null);
        mRecyclerView.setAdapter(mAdapter);

        // Add click support for the recycler view
        RecyclerItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new RecyclerItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, final int position, View v) {
                // Get the selected user
                final User user = mAdapter.getUsers().get(position);

                // If the user is an admin, request the password
                if (user.isAdmin()) {
                    // help for following dialog code from
                    // https://stackoverflow.com/a/10904665/2341126
                    AlertDialog.Builder builder = new AlertDialog.Builder(SelectProfileActivity.this);
                    builder.setTitle("Enter Password");

                    FrameLayout container = new FrameLayout(SelectProfileActivity.this);
                    final EditText input = new EditText(SelectProfileActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

                    int margin = (int) Utils.convertDpToPixel(24, SelectProfileActivity.this);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String password = input.getText().toString();
                            Admin admin = (Admin) user;
                            if (!password.isEmpty() && admin.validatePassword(password)) {
                                selectUser(user);
                            } else {
                                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Incorrect password.", Snackbar.LENGTH_SHORT);
                                snackbar.show();
                            }
                            dialog.dismiss();
                        }
                    });
                }
                else {
                    selectUser(user);
                }
            }
        });
    }

    /**
     * Set the user id in the shared preferences, set the result and close the activity.
     *
     * @param user Selected User
     */
    private void selectUser(User user) {
        PrefUtils.setCurrentUserId(user.getId(), this);
        Intent returnIntent = new Intent();
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}
