package com.uottawa.thebench.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.uottawa.thebench.R;
import com.uottawa.thebench.model.Admin;
import com.uottawa.thebench.model.BasicUser;
import com.uottawa.thebench.model.User;
import com.uottawa.thebench.utils.DBHandler;
import com.uottawa.thebench.utils.PrefUtils;
import com.uottawa.thebench.utils.Utils;

import java.io.IOException;

public class AddPersonActivity extends AppCompatActivity {

    private boolean mFirstUser = false;
    private boolean mIsEdit = false;
    private User mExistingUser;

    private RelativeLayout mAvatarContainerLayout;
    private ImageView mAvatarImage;
    private Bitmap mAvatarBitmap = null;
    private EditText mPersonNameEdit;
    private Switch mAdminSwitch;
    private EditText mPasswordEdit;

    private int RESULT_GET_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_person);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAvatarContainerLayout = (RelativeLayout) findViewById(R.id.avatar_container_layout);
        mAvatarImage = (ImageView) findViewById(R.id.avatar_image);
        mPersonNameEdit = (EditText) findViewById(R.id.person_name_edit);
        mAdminSwitch = (Switch) findViewById(R.id.admin_switch);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);

        // Show back button in toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Avatar click event
        mAvatarContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Only option is "Select from Gallery", might add ability to take a picture later
                CharSequence options[] = new CharSequence[] {"Select from Gallery"};

                AlertDialog.Builder builder = new AlertDialog.Builder(AddPersonActivity.this);
                builder.setTitle("Get Avatar");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // Open image selector intent
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent, "Select Avatar"), RESULT_GET_IMAGE);
                        }
                    }
                });
                builder.show();
            }
        });

        // Get parameters sent to this activity from previous activity
        Bundle b = getIntent().getExtras();
        if (b != null) {
            mFirstUser = b.getBoolean("firstUser", false);

            if (b.getBoolean("edit", false)) { // Edit user mode
                setTitle("Edit Person");
                mIsEdit = true;
                int userId = b.getInt("userId", -1);

                DBHandler dbHandler = new DBHandler(this);
                mExistingUser = dbHandler.findUser(userId);
                if (mExistingUser != null) {
                    if (mExistingUser.getAvatar() != null) {
                        mAvatarBitmap = mExistingUser.getAvatar();
                        mAvatarImage.setImageBitmap(mExistingUser.getAvatar());
                    }
                    mPersonNameEdit.setText(mExistingUser.getName());
                    if (mExistingUser.isAdmin()) {
                        mPasswordEdit.setText(((Admin) mExistingUser).getPassword());
                        mAdminSwitch.setChecked(true);

                        long currentUserId = PrefUtils.getCurrentUserId(this);
                        if (currentUserId == mExistingUser.getId()) {
                            // The current user can't edit himself from admin to basic user
                            mAdminSwitch.setEnabled(false);
                        }
                        else {
                            // An admin can't change another admin's password
                            mPasswordEdit.setEnabled(false);
                        }
                    }
                    else {
                        mAdminSwitch.setChecked(false);
                        mPasswordEdit.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }

        // If creating the first user, force admin switch to be checked
        if (mFirstUser) {
            mAdminSwitch.setChecked(true);
            mAdminSwitch.setEnabled(false);
        }

        // Hide the password field when admin switch is unchecked
        mAdminSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) {
                    mPasswordEdit.setVisibility(View.VISIBLE);
                }
                else {
                    mPasswordEdit.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.menu_add_person, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        else if (item.getItemId() == R.id.action_submit_person) {
            // Add or update the user when the submit button is clicked
            String name = mPersonNameEdit.getText().toString().trim();
            boolean isAdmin = mAdminSwitch.isChecked();
            String password = mPasswordEdit.getText().toString();

            if (TextUtils.isEmpty(name) || (isAdmin && TextUtils.isEmpty(password))) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Please fill out all fields.", Snackbar.LENGTH_LONG);
                snackbar.show();
                return true;
            }

            DBHandler dbHandler = new DBHandler(this);
            User user;

            if (isAdmin) {
                user = new Admin(
                        -1,
                        name,
                        mAvatarBitmap,
                        null,
                        password,
                        0
                );
            } else {
                user = new BasicUser(
                        -1,
                        name,
                        mAvatarBitmap,
                        null,
                        0
                );
            }

            Intent returnIntent = new Intent();
            long userId;

            if (mIsEdit) { // Existing user (update)
                user.setId(mExistingUser.getId());
                user.setPoints(mExistingUser.getPoints());
                boolean updated = dbHandler.updateUser(user);

                returnIntent.putExtra("userId", user.getId());
                if (updated) { // User was successfully updated in the DB
                    setResult(RESULT_OK, returnIntent);
                }
                else {
                    setResult(RESULT_CANCELED, returnIntent);
                }
            }
            else { // New user (insert)
                userId = dbHandler.addUser(user);
                returnIntent.putExtra("userId", userId);
                returnIntent.putExtra("name", name);
                if (userId > -1) { // User was successfully inserted in the DB
                    setResult(RESULT_OK, returnIntent);
                }
                else {
                    setResult(RESULT_CANCELED, returnIntent);
                }
            }

            // Close the activity
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_GET_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                // Get the bitmap from the selected URI
                mAvatarBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                // Make the bitmap circular, and resize it to a maximum of 256x256 pixels
                mAvatarBitmap = Bitmap.createScaledBitmap(Utils.getCircularBitmap(mAvatarBitmap), 256, 256, true);

                // Display the image
                mAvatarImage.setImageBitmap(mAvatarBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Image was not selected.", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
    }

}
