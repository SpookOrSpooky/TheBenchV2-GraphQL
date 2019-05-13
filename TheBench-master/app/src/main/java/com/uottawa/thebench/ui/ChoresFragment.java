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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.rohit.recycleritemclicksupport.RecyclerItemClickSupport;
import com.uottawa.thebench.R;
import com.uottawa.thebench.model.Admin;
import com.uottawa.thebench.model.Chore;
import com.uottawa.thebench.model.User;
import com.uottawa.thebench.utils.DBHandler;
import com.uottawa.thebench.utils.PrefUtils;

import java.util.List;

public class ChoresFragment extends Fragment {

    protected Activity mActivity;
    private DBHandler mDBHandler;

    private User mCurrentUser;
    private Chore.Status mSelectedStatus = Chore.Status.ACTIVE;
    private boolean mFilterMyChores = true;
    private int mOffset = 0;

    private Spinner mStatusSpinner;
    private final CharSequence[] mFilters = {"My Chores", "All Chores"};
    private Button mFilterButton;
    private RecyclerView mRecyclerView;
    private ChoresAdapter mAdapter;
    private EndlessRecyclerViewScrollListener mScrollListener;
    private FloatingActionButton mAddChoresFab;
    private TextView mNoChoresText;

    public ChoresFragment() {
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
        View v = inflater.inflate(R.layout.fragment_chores, container, false);

        mStatusSpinner = (Spinner) v.findViewById(R.id.chores_status_spinner);
        mFilterButton = (Button) v.findViewById(R.id.filter_chores_button);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.chores_recycler_view);
        mAddChoresFab = (FloatingActionButton) v.findViewById(R.id.add_chore_fab);
        mNoChoresText = (TextView) v.findViewById(R.id.no_chores_text);

        // Create and set the layout manager for the recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        mRecyclerView.setLayoutManager(layoutManager);

        // Add a line separator for the recycler view
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL));

        // Set a scroll listener on the recycler view to load more chores when we reach the bottom
        mScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                mOffset += DBHandler.DEFAULT_CHORES_COUNT;
                updateChoresList(mOffset);
            }
        };
        // Adds the scroll listener to RecyclerView
        mRecyclerView.addOnScrollListener(mScrollListener);

        // Create an ArrayAdapter for the status spinner using the status string array
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mActivity,
                R.array.chores_status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Set the spinner adapter
        mStatusSpinner.setAdapter(adapter);

        // Get the Database object
        mDBHandler = new DBHandler(mActivity);

        // Display the chores
        updateChoresList(true);

        // And click listener for the recycler view chore items
        RecyclerItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new RecyclerItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, final int position, final View v) {
                if (mCurrentUser == null) {
                    return;
                }

                // Get the selected chore
                final Chore chore = mAdapter.getChores().get(position);

                // If admin, give options for the chore (if not complete). Otherwise, go to view chore activity
                if (mCurrentUser.isAdmin() && chore.getStatus() != Chore.Status.COMPLETED) {
                    final CharSequence options[];
                    if (chore.getStatus() == Chore.Status.POSTPONED) {
                        // If the clicked chore is postponed, give the options to view or mark as active
                        options = new CharSequence[]{"View", "Mark as Active"};
                    }
                    else {
                        // If the clicked chore is active, give the options to view, mark as complete, or postpone
                        options = new CharSequence[]{"View", "Mark as Completed", "Postpone"};
                    }

                    // Create a dialog with the clickable options
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setTitle(chore.getName());
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (options[which] == "View") {
                                Intent intent = new Intent(mActivity, ViewChoreActivity.class);
                                intent.putExtra("chore_id", chore.getId());
                                intent.putExtra("admin", true);
                                mActivity.startActivityForResult(intent, MainActivity.RESULT_VIEW_CHORE);
                            }
                            else if (options[which] == "Mark as Completed") {
                                // Mark the chore status as complete
                                chore.setStatus(Chore.Status.COMPLETED);

                                // Remove the chore from the adapter dataset (since it's not active anymore)
                                mAdapter.removeChore(chore, position);

                                // Update the database record
                                boolean updated = mDBHandler.updateChore(chore);

                                if (updated && chore.hasAssignee()) {
                                    // If the update was successful, update the assignee's points
                                    User assignee = chore.getAssignee();
                                    assignee.setPoints(assignee.getPoints() + chore.getPoints());
                                    updated = mDBHandler.updateUser(assignee);

                                    if (updated) {
                                        // Update the household since a user has more points
                                        ((MainActivity)mActivity).getHousehold(true);

                                        // If the update for the points was successful, show a message
                                        String message;
                                        if (mCurrentUser.getId() == assignee.getId()) {
                                            ((MainActivity)mActivity).updateNavUserPoints(assignee.getPoints());
                                            message = "You have just been awarded " + chore.getPoints() + " points!";
                                        } else {
                                            message = assignee.getName() + " has just been awarded " + chore.getPoints() + " points!";
                                        }
                                        Snackbar snackbar = Snackbar.make(mActivity.findViewById(R.id.content_frame), message, Snackbar.LENGTH_LONG);
                                        snackbar.show();
                                    }
                                }

                                // Show a message if the recycler view is now empty
                                checkIfChoresEmpty();
                            }
                            else if (options[which] == "Postpone") {
                                // Mark the chore status as postponed
                                chore.setStatus(Chore.Status.POSTPONED);

                                // Remove the chore from the adapter dataset (since it's not active anymore)
                                mAdapter.removeChore(chore, position);

                                // Update the database record
                                boolean updated = mDBHandler.updateChore(chore);

                                if (updated) {
                                    // If the update was successful, show a message
                                    String message = "\"" + chore.getName() + "\" has been postponed.";
                                    Snackbar snackbar = Snackbar.make(mActivity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }

                                // Show a message if the recycler view is now empty
                                checkIfChoresEmpty();
                            }
                            else if (options[which] == "Mark as Active") {
                                // Mark the chore status as active
                                chore.setStatus(Chore.Status.ACTIVE);

                                // Remove the chore from the adapter dataset (since it's not postponed anymore)
                                mAdapter.removeChore(chore, position);

                                // Update the database record
                                boolean updated = mDBHandler.updateChore(chore);

                                if (updated) {
                                    // If the update was successful, show a message
                                    String message = "\"" + chore.getName() + "\" is now active.";
                                    Snackbar snackbar = Snackbar.make(mActivity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }

                                // Show a message if the recycler view is now empty
                                checkIfChoresEmpty();
                            }
                        }
                    });
                    builder.show();
                }
                else {
                    // open view chore activity here
                    Intent intent = new Intent(mActivity, ViewChoreActivity.class);
                    intent.putExtra("chore_id", chore.getId());
                    intent.putExtra("admin", false);
                    mActivity.startActivityForResult(intent, MainActivity.RESULT_VIEW_CHORE);
                }
            }
        });

        // Add a change listener for the status spinner
        mStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean newStatus = false;
                switch (position) {
                    case 0:
                        if (mSelectedStatus != Chore.Status.ACTIVE) {
                            mSelectedStatus = Chore.Status.ACTIVE;
                            PrefUtils.setLastSelectedChoresStatus(Chore.Status.ACTIVE, mActivity);
                            newStatus = true;
                        }
                        break;
                    case 1:
                        if (mSelectedStatus != Chore.Status.POSTPONED) {
                            mSelectedStatus = Chore.Status.POSTPONED;
                            PrefUtils.setLastSelectedChoresStatus(Chore.Status.POSTPONED, mActivity);
                            newStatus = true;
                        }
                        break;
                    case 2:
                        if (mSelectedStatus != Chore.Status.COMPLETED) {
                            mSelectedStatus = Chore.Status.COMPLETED;
                            PrefUtils.setLastSelectedChoresStatus(Chore.Status.COMPLETED, mActivity);
                            newStatus = true;
                        }
                        break;
                }

                // If a new status is selected, update the chores list
                if (newStatus) {
                    updateChoresList(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Add a click listener for the filter button, show options to filter by my chores or all chores
        mFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
                dialog.setTitle("Filter Chores");
                dialog.setSingleChoiceItems(mFilters, mFilterMyChores ? 0 : 1, new DialogInterface
                        .OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        mFilterButton.setText(mFilters[item]);
                        if (item == 0) {
                            if (!mFilterMyChores) {
                                mFilterMyChores = true;
                                PrefUtils.setFilterMyChores(true, mActivity);
                                updateChoresList(false);
                            }
                        }
                        else if (item == 1) {
                            if (mFilterMyChores) {
                                mFilterMyChores = false;
                                PrefUtils.setFilterMyChores(false, mActivity);
                                updateChoresList(false);
                            }
                        }
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = dialog.create();
                alert.show();
            }
        });

        // Show the add chore activity when the add chore button is clicked
        mAddChoresFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity, CreateChoreActivity.class);
                intent.putExtra("user_id", ((MainActivity)mActivity).getCurrentUser().getId());
                mActivity.startActivityForResult(intent, MainActivity.RESULT_VIEW_CHORE);
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

    /**
     * Fetches the preferences for the previously selected filters and updates the chores list
     * accordingly.
     *
     * @param fetchPreferences Whether or not to fetch the preferences and update the filter views
     */
    public void updateChoresList(boolean fetchPreferences) {
        mOffset = 0;
        mScrollListener.resetState();

        if (fetchPreferences) {
            // Load previously selected filters stored in shared preferences
            mFilterMyChores = PrefUtils.getFilterMyChores(mActivity);
            mSelectedStatus = PrefUtils.getLastSelectedChoresStatus(mActivity);

            // Set the currently selected status
            switch (mSelectedStatus) {
                case ACTIVE:
                    mStatusSpinner.setSelection(0, false);
                    break;
                case POSTPONED:
                    mStatusSpinner.setSelection(1, false);
                    break;
                case COMPLETED:
                    mStatusSpinner.setSelection(2, false);
                    break;
            }

            // Set the text of the filter
            if (mFilterMyChores) {
                mFilterButton.setText(mFilters[0]);
            } else {
                mFilterButton.setText(mFilters[1]);
            }
        }

        updateChoresList(mOffset);

        // If the user is not an admin, hide the create chore button
        if (mCurrentUser == null || !mCurrentUser.isAdmin()) {
            mAddChoresFab.setVisibility(View.INVISIBLE);
        }
        else {
            mAddChoresFab.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Fetch the chores records from the database filtered by the status and the current user or
     * all users, then update the recycler view.
     *
     * @param offset Offset of the data we are fetching from the DB
     */
    private void updateChoresList(final int offset) {
        // Get the current user from the Main Activity
        mCurrentUser = ((MainActivity)mActivity).getCurrentUser();

        // Get the chores from the DB
        final List<Chore> chores = mDBHandler.getChores(mFilterMyChores ? mCurrentUser : null,
                mSelectedStatus, DBHandler.DEFAULT_CHORES_COUNT, offset);
        if (mAdapter == null) {
            // Create a new adapter with the list of chores, and set the adapter to the recycler view
            mAdapter = new ChoresAdapter(mActivity, chores);
            mRecyclerView.setAdapter(mAdapter);
            checkIfChoresEmpty();
        }
        else {
            // Runnable was needed to ensure this runs on the main thread
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    if (offset > 0) {
                        mAdapter.addChores(chores);
                    }
                    else {
                        mAdapter.setChores(chores);
                    }
                    checkIfChoresEmpty();
                }
            });
        }
    }

    /**
     * Show an empty message if the chores dataset is empty.
     */
    private void checkIfChoresEmpty() {
        if (mAdapter.getItemCount() == 0) {
            mNoChoresText.setVisibility(View.VISIBLE);
            switch (mSelectedStatus) {
                case ACTIVE:
                    mNoChoresText.setText("No active chores to display");
                    break;
                case POSTPONED:
                    mNoChoresText.setText("No postponed chores to display");
                    break;
                case COMPLETED:
                    mNoChoresText.setText("No completed chores to display");
                    break;
            }
        }
        else {
            mNoChoresText.setVisibility(View.INVISIBLE);
        }
    }

}
