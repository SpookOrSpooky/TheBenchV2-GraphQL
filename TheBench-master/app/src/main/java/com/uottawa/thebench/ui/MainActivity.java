package com.uottawa.thebench.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uottawa.thebench.R;
import com.uottawa.thebench.model.Admin;
import com.uottawa.thebench.model.Household;
import com.uottawa.thebench.model.User;
import com.uottawa.thebench.utils.DBHandler;
import com.uottawa.thebench.utils.PrefUtils;
import com.uottawa.thebench.utils.Utils;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private CircleImageView mAvatarImage;
    private TextView mUserNameText;
    private TextView mUserScoreText;
    private RelativeLayout mSwitchProfileLayout;
    private TextView mSwitchProfileText;
    private ImageView mSwitchProfileArrow;

    private boolean mIsSwitchingProfile = false;
    private ColorStateList mNavColorStateList;
    private boolean mShowChoresFragmentOnPostResume = false;

    private DBHandler mDBHandler;
    private Household mHousehold;

    private static final int RESULT_INITIAL_SETUP = 1;
    private static final int RESULT_SELECT_PROFILE = 2;
    public static final int RESULT_ADD_PERSON = 3;
    public static final int RESULT_EDIT_PERSON = 4;
    public static final int RESULT_VIEW_CHORE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Replace the ActionBar with the Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Setup drawer view
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        setupNavDrawerContent(mNavigationView);

        // Inflate the header view in the navigation drawer
        View headerLayout = mNavigationView.inflateHeaderView(R.layout.nav_header);

        mAvatarImage = headerLayout.findViewById(R.id.avatar_image);
        mUserNameText = headerLayout.findViewById(R.id.user_name_text);
        mUserScoreText = headerLayout.findViewById(R.id.user_score_text);
        mSwitchProfileLayout = headerLayout.findViewById(R.id.switch_profile_layout);
        mSwitchProfileText = headerLayout.findViewById(R.id.switch_profile_text);
        mSwitchProfileArrow = headerLayout.findViewById(R.id.switch_profile_arrow_image);

        // Setup the drawer toggle
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open,  R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // Get the color state list used in the navigation drawer menu's tint
        mNavColorStateList = mNavigationView.getItemIconTintList();

        // Click listener for the switch profile button in the navigation drawer
        mSwitchProfileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsSwitchingProfile) {
                    // When switching profiles, remove the tint from the nav menu and make only the
                    // users group visible.
                    mIsSwitchingProfile = true;
                    mNavigationView.setItemIconTintList(null);
                    mSwitchProfileArrow.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_arrow_drop_up_white_18dp));
                    mSwitchProfileText.setText("Switch to...");
                    mNavigationView.getMenu().setGroupVisible(R.id.nav_group_main, false);
                    mNavigationView.getMenu().setGroupVisible(R.id.nav_group_more, false);
                    mNavigationView.getMenu().setGroupVisible(R.id.nav_group_users, true);
                }
                else {
                    // Revert back to the original nav menu look
                    mIsSwitchingProfile = false;
                    mNavigationView.setItemIconTintList(mNavColorStateList);
                    mSwitchProfileArrow.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_arrow_drop_down_white_18dp));
                    mSwitchProfileText.setText("Switch Profile");
                    mNavigationView.getMenu().setGroupVisible(R.id.nav_group_main, true);
                    mNavigationView.getMenu().setGroupVisible(R.id.nav_group_more, true);
                    mNavigationView.getMenu().setGroupVisible(R.id.nav_group_users, false);
                }
            }
        });

        mDBHandler = new DBHandler(this);
        getHousehold(true);

        if (mHousehold == null) {
            // If the household has not been setup yet, show the start screen activity
            Intent intent = new Intent(this, StartScreenActivity.class);
            this.startActivityForResult(intent, RESULT_INITIAL_SETUP);
        }
        else {
            // The household has been setup, so show the select profile activity
            Intent intent = new Intent(this, SelectProfileActivity.class);
            this.startActivityForResult(intent, RESULT_SELECT_PROFILE);
        }

        // Make the status bar translucent (can be done in XML, but this caused a bug in preview)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        // If the drawer is open, back will close it
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        // On back pressed, if the current fragment is the chores view,
        // exit the application. Otherwise, go back to the chores view.
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment instanceof ChoresFragment) {
            super.onBackPressed();
        }
        else {
            MenuItem choresItem = mNavigationView.getMenu().findItem(R.id.nav_chores_fragment);
            selectItem(choresItem);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_INITIAL_SETUP) { // Returned from start screen
            if (resultCode == RESULT_OK) {
                // Initial setup successful, get the newly created household and update the nav
                getHousehold(true);
                updateNavHeader();

                // Can't show chores fragment now (would cause state loss), so set a flag
                mShowChoresFragmentOnPostResume = true;

                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "\"" + mHousehold.getName() + "\" Household has been created!", Snackbar.LENGTH_SHORT);
                snackbar.show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDrawerLayout.openDrawer(Gravity.LEFT);
                    }
                }, 500);
            }
            else {
                // Initial setup unsuccessful, exit app
                finish();
            }
        }
        else if (requestCode == RESULT_SELECT_PROFILE) { // Returned from select profile screen
            if (resultCode == RESULT_OK) {
                // Select profile successful, update the nav accordingly
                updateNavHeader();

                // Can't show chores fragment now (would cause state loss), so set a flag
                mShowChoresFragmentOnPostResume = true;
            }
            else {
                // Select profile unsuccessful, exit app
                finish();
            }
        }
        else if ((requestCode == RESULT_ADD_PERSON || requestCode == RESULT_EDIT_PERSON) && resultCode == RESULT_OK) {
            // If a person was added or edited, update the household object and update the nav
            getHousehold(true);
            updateNavHeader();

            // Call onActivityResult in the current fragment
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (fragment != null) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }

            // Set the right menu item according to the current fragment
            if (fragment instanceof ChoresFragment) {
                mNavigationView.getMenu().findItem(R.id.nav_chores_fragment).setChecked(true);
            }
            else if (fragment instanceof PeopleFragment) {
                mNavigationView.getMenu().findItem(R.id.nav_people_fragment).setChecked(true);
            }
            else if (fragment instanceof ResourcesFragment) {
                mNavigationView.getMenu().findItem(R.id.nav_resources_fragment).setChecked(true);
            }

            // If adding a person, display a message saying so
            if (requestCode == RESULT_ADD_PERSON) {
                String name = data.getStringExtra("name");
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), name + " has been added!", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        }
        else if (requestCode == RESULT_VIEW_CHORE && resultCode == RESULT_OK) {
            updateChoresList();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mShowChoresFragmentOnPostResume) {
            // Insert the default fragment (chores screen) into the content frame layout
            MenuItem choresItem = mNavigationView.getMenu().findItem(R.id.nav_chores_fragment);
            selectItem(choresItem);
        }
        // Reset the boolean flag back to false for next time.
        mShowChoresFragmentOnPostResume = false;
    }

    /**
     * Set an on click listener for the navigation view menu.
     *
     * @param navigationView
     */
    private void setupNavDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectItem(menuItem);
                        return true;
                    }
                });
    }

    /**
     * Complete the required action for the given menuItem.
     * If it is in the 'nav_group_main', show the selected fragment.
     * If it is in the 'nav_group_more', either sign out or open an activity.
     *
     * @param menuItem
     */
    public void selectItem(MenuItem menuItem) {
        if (menuItem.getGroupId() == R.id.nav_group_main) {
            // Open a fragment based on which navigation item was clicked
            Fragment fragment = null;
            Class fragmentClass;
            switch (menuItem.getItemId()) {
                case R.id.nav_chores_fragment:
                    fragmentClass = ChoresFragment.class;
                    break;
                case R.id.nav_people_fragment:
                    fragmentClass = PeopleFragment.class;
                    break;
                case R.id.nav_resources_fragment:
                    fragmentClass = ResourcesFragment.class;
                    break;
                default:
                    fragmentClass = ChoresFragment.class;
            }

            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Insert the fragment into the content frame layout
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

            if (menuItem.isCheckable()) {
                // Highlight the selected navigation item
                menuItem.setChecked(true);
                // Set the activity title to the selected fragment
                setTitle(menuItem.getTitle());
            }
            mDrawerLayout.closeDrawers();
        }
        else if (menuItem.getGroupId() == R.id.nav_group_more) {
            switch (menuItem.getItemId()) {
                case R.id.nav_sign_out:
                    // Sign out clicked, so remove current user from shared preferences and go to
                    // select profile screen
                    PrefUtils.setCurrentUserId(-1, this);
                    Intent intent = new Intent(this, SelectProfileActivity.class);
                    this.startActivityForResult(intent, RESULT_SELECT_PROFILE);
                    break;
//                case R.id.nav_settings_activity:
//                    // open settings activity
//                    break;
                case R.id.nav_about_activity:
                    Intent intent1 = new Intent(this, AboutActivity.class);
                    this.startActivity(intent1);
                    break;
            }
            mDrawerLayout.closeDrawers();
        }
    }


    /**
     * If the update param is false and the household variable is not null, return the value of the
     * household variable. Otherwise, fetch the household (and its users) from the database and
     * create a new object.
     *
     * @param update Whether or not to update the household object
     * @return The Household object
     */
    public Household getHousehold(boolean update) {
        if (!update && mHousehold != null) {
            return mHousehold;
        }
        mHousehold = mDBHandler.getHousehold();
        if (mHousehold != null) {
            List<User> users = mDBHandler.getUsers();
            mHousehold.setUsers(users);
        }
        return mHousehold;
    }

    /**
     * Simply returns the users in the household object.
     *
     * @return List of users in the household
     */
    public List<User> getUsers() {
        if (mHousehold != null) {
            return mHousehold.getUsers();
        }
        return null;
    }

    /**
     * Get the user in the current session.
     *
     * @return Current user
     */
    public User getCurrentUser() {
        User user = null;
        List<User> users = getUsers();
        long currentUserId = PrefUtils.getCurrentUserId(this);
        if (currentUserId > 0) {
            for (User u: users) {
                if (u.getId() == currentUserId) {
                    return u;
                }
            }
        }
        return user;
    }

    /**
     * Update the navigation drawer header. This is usually used after the user switches profiles.
     */
    private void updateNavHeader() {
        List<User> users = getUsers();
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            // Show the current user's avatar, name and score in the nav header
            if (currentUser.getAvatar() != null) {
                mAvatarImage.setImageBitmap(currentUser.getAvatar());
            } else {
                mAvatarImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.default_avatar));
            }

            mUserNameText.setText(currentUser.getName());
            mUserScoreText.setText(currentUser.getPoints() + " pts");
        }
        else {
            // If no current user, show the select profile activity
            mUserNameText.setText("Select a Profile");
            mUserScoreText.setText("-");
            Intent intent = new Intent(this, SelectProfileActivity.class);
            this.startActivityForResult(intent, RESULT_SELECT_PROFILE);
        }

        // Reset the navigation view menu (clear it then inflate it again)
        mNavigationView.getMenu().clear();
        mNavigationView.inflateMenu(R.menu.drawer_view);

        // Add each user (except the current one) as menu items in the navigation view group
        if (users != null) {
            for (final User user: users) {
                if (user != currentUser) {
                    String menuName = user.isAdmin() ? user.getName() + " (Admin)" : user.getName();

                    // If avatar isn't set, use the default one
                    Drawable avatar = user.getAvatar() != null ? new BitmapDrawable(getResources(), user.getAvatar())
                            : ContextCompat.getDrawable(this, R.drawable.default_avatar);

                    // Add an item, set its icon, and add a click listener for switching profiles
                    mNavigationView.getMenu().add(R.id.nav_group_users, Menu.NONE, 0, menuName)
                            .setIcon(avatar)
                            .setCheckable(false)
                            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    // On click, switch to the profile (request password if admin)
                                    if (user.isAdmin()) {
                                        // help for following dialog code from
                                        // https://stackoverflow.com/a/10904665/2341126
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle("Enter Password");

                                        FrameLayout container = new FrameLayout(MainActivity.this);
                                        final EditText input = new EditText(MainActivity.this);
                                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(20)});

                                        int margin = (int) Utils.convertDpToPixel(24, MainActivity.this);
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
                                                String password = input.getText().toString();
                                                Admin admin = (Admin) user;
                                                // Verify that the input matches the user's password
                                                if(!password.isEmpty() && admin.validatePassword(password)) {
                                                    switchToUser(user);
                                                    mSwitchProfileLayout.callOnClick();
                                                }
                                                else {
                                                    Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Incorrect password.", Snackbar.LENGTH_SHORT);
                                                    snackbar.show();
                                                }
                                                dialog.dismiss();
                                            }
                                        });
                                    }
                                    else {
                                        switchToUser(user);
                                        mSwitchProfileLayout.callOnClick();
                                    }
                                    return false;
                                }
                            });
                }
            }
        }

        // Add a menu item to add a person (only for admins)
        if (currentUser.isAdmin()) {
            mNavigationView.getMenu().add(R.id.nav_group_users, Menu.NONE, 0, "Add Person")
                    .setIcon(R.drawable.ic_add_black_24dp)
                    .setCheckable(false)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            Intent intent = new Intent(MainActivity.this, AddPersonActivity.class);
                            MainActivity.this.startActivityForResult(intent, MainActivity.RESULT_ADD_PERSON);
                            mSwitchProfileLayout.callOnClick();
                            return false;
                        }
                    });
        }

        // Hide the users group for now
        mNavigationView.getMenu().setGroupVisible(R.id.nav_group_users, false);
    }

    /**
     * Switch to the selected user. This involves setting the shared preferences user id, going back
     * to the chores screen, updating the nav header and displaying a message to the user.
     *
     * @param user
     */
    private void switchToUser(User user) {
        if (user != null) {
            PrefUtils.setCurrentUserId(user.getId(), this);
            MenuItem choresItem = mNavigationView.getMenu().findItem(R.id.nav_chores_fragment);
            selectItem(choresItem);
            updateNavHeader();
            updateChoresList();
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Switched to " + user.getName(), Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    public void updateNavUserPoints(int score) {
        mUserScoreText.setText(score + " pts");
    }

    /**
     * Call the updateChoresList method in the chores fragment.
     */
    private void updateChoresList() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment instanceof ChoresFragment) {
            ((ChoresFragment)fragment).updateChoresList(true);
        }
    }

}
