package com.uottawa.thebench.ui;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.uottawa.thebench.R;
import com.uottawa.thebench.model.Chore;
import com.uottawa.thebench.model.Resource;
import com.uottawa.thebench.utils.DBHandler;
import com.uottawa.thebench.utils.Utils;

import java.util.List;

/**
 * Adapter class used for the recycler view displaying the chore resources.
 */
public class ChoresResourcesAdapter extends RecyclerView.Adapter<ChoresResourcesAdapter.ViewHolder> {
    private Context mContext;
    private List<Chore> mChores;

    /**
     * View Holder class which declares the views that will be inflated in the recycler view.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView personAvatarImage;
        public TextView choreNameText;
        public ImageButton addResourceImageButton;
        public ExpandableHeightGridView resourcesGridView;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            personAvatarImage = (ImageView) v.findViewById(R.id.person_avatar_image);
            choreNameText = (TextView) v.findViewById(R.id.chore_name_text);
            addResourceImageButton = (ImageButton) v.findViewById(R.id.add_resource_image_button);
            resourcesGridView = (ExpandableHeightGridView) v.findViewById(R.id.resources_grid_view);
            resourcesGridView.setExpanded(true);
        }
    }

    public ChoresResourcesAdapter(Context context, List<Chore> chores) {
        mContext = context;
        mChores = chores;
    }

    /**
     * Set the list of chores and notify the recycler view that the list has changed.
     *
     * @param chores
     */
    public void setChores(List<Chore> chores) {
        mChores = chores;
        notifyDataSetChanged();
    }

    /**
     * Add to the list of chores and notify the recycler view that the list has changed.
     *
     * @param chores
     */
    public void addChores(List<Chore> chores) {
        int position = mChores.size() + 1;
        mChores.addAll(chores);
        notifyItemRangeInserted(position, chores.size());
    }

    /**
     * Get the list of chores.
     *
     * @return List of chores
     */
    public List<Chore> getChores() {
        return mChores;
    }

    /**
     * Inflate the row view layout.
     *
     * @param parent
     * @param viewType
     * @return ViewHolder object
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate a new row layout
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.chore_resources_row_layout, parent, false);
        final ViewHolder vh = new ViewHolder(v);

        // Set the click listener for the add resource button
        vh.addResourceImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Chore chore = mChores.get(vh.getAdapterPosition());
                if (chore == null) return;

                // help for following dialog code from
                // https://stackoverflow.com/a/10904665/2341126
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Enter Resource Name");

                FrameLayout container = new FrameLayout(mContext);
                final EditText input = new EditText(mContext);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(30)});

                int margin = (int) Utils.convertDpToPixel(24, mContext);
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
                            int lastResourceIndex = chore.getResources().size() - 1;
                            if (lastResourceIndex >= 0 && chore.getResources().get(lastResourceIndex) == null) {
                                chore.getResources().remove(lastResourceIndex);
                            }

                            DBHandler dbHandler = new DBHandler(mContext);
                            Resource resource = new Resource(-1, resourceName, false, chore);
                            long resourceId = dbHandler.addResource(resource);

                            if (resourceId > 0) {
                                resource.setId((int)resourceId);

                                // If odd number of resources, add extra empty one to fill gridview
                                if((chore.getResources().size() % 2) != 0) {
                                    chore.getResources().add(null);
                                }

                                // Update the resources adapter for this chore
                                ResourcesAdapter resourcesAdapter = (ResourcesAdapter)vh.resourcesGridView.getAdapter();
                                resourcesAdapter.notifyDataSetChanged();

                                if (mContext instanceof MainActivity) {
                                    // Notify the user that the resource was added
                                    Snackbar snackbar = Snackbar.make(((MainActivity) mContext).findViewById(android.R.id.content),
                                            "\"" + resourceName + "\" resource added!", Snackbar.LENGTH_SHORT);
                                    snackbar.show();
                                }
                            }
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        return vh;
    }

    /**
     * Set the view contents.
     *
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        final Chore chore = mChores.get(position);
        // Set the avatar to the user's avatar if it's set or the default avatar
        if (chore.getAssignee() == null) {
            viewHolder.personAvatarImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.question_mark_avatar));
        }
        else if (chore.getAssignee().getAvatar() != null) {
            viewHolder.personAvatarImage.setImageBitmap(chore.getAssignee().getAvatar());
        }
        else {
            viewHolder.personAvatarImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.default_avatar));
        }
        viewHolder.choreNameText.setText(chore.getName());

        ResourcesAdapter resourcesAdapter = new ResourcesAdapter(mContext, chore.getResources(), ContextCompat.getColor(mContext, R.color.white), true);
        viewHolder.resourcesGridView.setAdapter(resourcesAdapter);
    }

    @Override
    public int getItemCount() {
        return mChores.size();
    }

}
