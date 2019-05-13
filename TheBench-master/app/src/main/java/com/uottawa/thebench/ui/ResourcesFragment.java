package com.uottawa.thebench.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.uottawa.thebench.R;
import com.uottawa.thebench.model.Chore;
import com.uottawa.thebench.model.User;
import com.uottawa.thebench.utils.DBHandler;
import com.uottawa.thebench.utils.PrefUtils;

import java.util.List;

public class ResourcesFragment extends Fragment {

    protected Activity mActivity;
    private DBHandler mDBHandler;

    private User mCurrentUser;
    private boolean mFilterMyChores = true;
    private int mOffset = 0;
    private final CharSequence[] mFilters = {"My Chores", "All Chores"};
    private Button mFilterButton;
    private RecyclerView mRecyclerView;
    private ChoresResourcesAdapter mAdapter;
    private EndlessRecyclerViewScrollListener mScrollListener;
    private TextView mNoResourcesText;

    public ResourcesFragment() {
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
        View v = inflater.inflate(R.layout.fragment_resources, container, false);

        mFilterButton = (Button) v.findViewById(R.id.filter_chores_button);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.chores_resources_recycler_view);
        mNoResourcesText = (TextView) v.findViewById(R.id.no_resources_text);

        // Create and set the layout manager for the recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
        layoutManager.setAutoMeasureEnabled(true);
        mRecyclerView.setLayoutManager(layoutManager);

        // Set a scroll listener on the recycler view to load more chores when we reach the bottom
        mScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                mOffset += DBHandler.DEFAULT_CHORES_COUNT;
                updateChoreResourcesList(mOffset);
            }
        };
        // Adds the scroll listener to RecyclerView
        mRecyclerView.addOnScrollListener(mScrollListener);

        // Get the Database object
        mDBHandler = new DBHandler(mActivity);

        // Display the chores
        updateChoreResourcesList(true);

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
                                updateChoreResourcesList(false);
                            }
                        }
                        else if (item == 1) {
                            if (mFilterMyChores) {
                                mFilterMyChores = false;
                                PrefUtils.setFilterMyChores(false, mActivity);
                                updateChoreResourcesList(false);
                            }
                        }
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = dialog.create();
                alert.show();
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
    public void updateChoreResourcesList(boolean fetchPreferences) {
        mOffset = 0;
        mScrollListener.resetState();

        if (fetchPreferences) {
            // Load previously selected filters stored in shared preferences
            mFilterMyChores = PrefUtils.getFilterMyChores(mActivity);

            // Set the text of the filter
            if (mFilterMyChores) {
                mFilterButton.setText(mFilters[0]);
            } else {
                mFilterButton.setText(mFilters[1]);
            }
        }

        updateChoreResourcesList(mOffset);
    }

    /**
     * Fetch the chores records from the database filtered by the current user or all users, then
     * update the recycler view. Resources can be found in the chore objects.
     *
     * @param offset Offset of the data we are fetching from the DB
     */
    private void updateChoreResourcesList(final int offset) {
        // Get the current user from the Main Activity
        mCurrentUser = ((MainActivity)mActivity).getCurrentUser();

        // Get the chores from the DB
        final List<Chore> chores = mDBHandler.getChores(mFilterMyChores ? mCurrentUser : null,
                Chore.Status.ACTIVE, DBHandler.DEFAULT_CHORES_COUNT, offset);

        // Remove chores without resources
        for (int i = 0; i < chores.size(); i++) {
            if (!chores.get(i).hasResources()) {
                chores.remove(i);
            }
        }

        if (mAdapter == null) {
            // Create a new adapter with the list of chores, and set the adapter to the recycler view
            mAdapter = new ChoresResourcesAdapter(mActivity, chores);
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
            mNoResourcesText.setVisibility(View.VISIBLE);
        }
        else {
            mNoResourcesText.setVisibility(View.INVISIBLE);
        }
    }

}
