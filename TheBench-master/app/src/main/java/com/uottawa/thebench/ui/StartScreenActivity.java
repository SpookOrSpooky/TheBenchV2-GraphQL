package com.uottawa.thebench.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.uottawa.thebench.R;
import com.uottawa.thebench.model.Household;
import com.uottawa.thebench.model.User;
import com.uottawa.thebench.utils.DBHandler;
import com.uottawa.thebench.utils.PrefUtils;

import java.util.List;

public class StartScreenActivity extends AppCompatActivity {

    private EditText mHouseholdNameEdit;
    private TextView mSetupMessageText;
    private Button mAddPersonButton;
    private Button mFinishSetupButton;
    private boolean mFirstUserAdded = false;

    private static final int RESULT_ADD_PERSON = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        mHouseholdNameEdit = (EditText) findViewById(R.id.household_name_edit);
        mSetupMessageText = (TextView) findViewById(R.id.setup_message_text);
        mAddPersonButton = (Button) findViewById(R.id.add_person_button);
        mFinishSetupButton = (Button) findViewById(R.id.finish_setup_button);

        final DBHandler dbHandler = new DBHandler(this);
        List<User> users = dbHandler.getUsers();

        // The list of users should only have items if the setup process was started but not
        // finished. If the setup was finish, this activity should never be opened again.
        for (User user : users) {
            if (!mFirstUserAdded) {
                mFirstUserAdded = true;

                // Enabled the finish button
                mFinishSetupButton.setEnabled(true);
                mFinishSetupButton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

                // List the first user's name
                mSetupMessageText.setText("Family: " + user.getName());
            }
            else {
                // List subsequent users' names
                mSetupMessageText.append(", " + user.getName());
            }
        }

        // When the add person button is clicked, start the add person activity
        mAddPersonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartScreenActivity.this, AddPersonActivity.class);
                if (!mFirstUserAdded) {
                    // Specify that it is for the first user (to force the user to be an admin)
                    Bundle b = new Bundle();
                    b.putBoolean("firstUser", true);
                    intent.putExtras(b);
                }
                StartScreenActivity.this.startActivityForResult(intent, RESULT_ADD_PERSON);
            }
        });

        // Create the household when the finish button is clicked
        mFinishSetupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String householdName = mHouseholdNameEdit.getText().toString().trim();
                if (TextUtils.isEmpty(householdName)) {
                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Please enter the household name.", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    return;
                }

                Household household = new Household(-1, householdName);
                dbHandler.addHousehold(household);

                // Set return intent to let MainActivity know that setup is complete
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RESULT_ADD_PERSON && resultCode == RESULT_OK) {
            // Get the user's name
            String name = data.getStringExtra("name");

            if (!mFirstUserAdded) {
                // Set the first added user as the current user session
                long firstUserId = data.getLongExtra("userId", -1);
                PrefUtils.setCurrentUserId(firstUserId, this);

                // Enable the finish button
                mFinishSetupButton.setEnabled(true);
                mFinishSetupButton.setTextColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

                // List the first user's name
                mSetupMessageText.setText("Family: " + name);
            }
            else {
                // List subsequent users' names
                mSetupMessageText.append(", " + name);
            }

            Snackbar snackbar = Snackbar.make(findViewById(R.id.start_screen__coordinator_layout),
                    name + " has been added!", Snackbar.LENGTH_SHORT);
            snackbar.show();

            mFirstUserAdded = true;
        }

    }

}