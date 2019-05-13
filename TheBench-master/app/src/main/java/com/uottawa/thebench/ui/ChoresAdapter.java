package com.uottawa.thebench.ui;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.uottawa.thebench.R;
import com.uottawa.thebench.model.Chore;
import com.uottawa.thebench.utils.Utils;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

/**
 * Adapter class used for the recycler view displaying the chores.
 */
public class ChoresAdapter extends RecyclerView.Adapter<ChoresAdapter.ViewHolder> {
    private Context mContext;
    private List<Chore> mChores;

    /**
     * View Holder class which declares the views that will be inflated in the recycler view.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView personAvatarImage;
        public TextView choreNameText;
        public TextView choreSubtitleText;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            personAvatarImage = (ImageView) v.findViewById(R.id.person_avatar_image);
            choreNameText = (TextView) v.findViewById(R.id.chore_name_text);
            choreSubtitleText = (TextView) v.findViewById(R.id.chore_subtitle_text);
        }
    }

    public ChoresAdapter(Context context, List<Chore> chores) {
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
     * Remove a chore from the list and notify the recycler view.
     *
     * @param chore Chore object
     * @param position Position in the list
     */
    public void removeChore(Chore chore, int position) {
        mChores.remove(chore);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mChores.size());
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
        View v = inflater.inflate(R.layout.chore_row_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
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

        if (chore.getStatus() == Chore.Status.ACTIVE) {
            // If a chore is active, show the deadline text in the subtitle
            DateTime deadline = new DateTime(chore.getDeadline());
            if (Utils.isPast(deadline)) { // A past deadline should be red
                viewHolder.choreSubtitleText.setTextColor(ContextCompat.getColor(mContext, R.color.red_text));
            }
            else {
                viewHolder.choreSubtitleText.setTextColor(ContextCompat.getColor(mContext, R.color.primaryText));
            }
            viewHolder.choreSubtitleText.setText(Utils.getDeadlineString(deadline, chore.isAllDay()));
        }
        else if (chore.getStatus() == Chore.Status.POSTPONED) {
            // If a chore is postponed, change the subtitle to "Postponed" and make it red
            viewHolder.choreSubtitleText.setTextColor(ContextCompat.getColor(mContext, R.color.red_text));
            viewHolder.choreSubtitleText.setText("Postponed");
        }
        else if (chore.getStatus() == Chore.Status.COMPLETED) {
            // If a chore is completed, show the points awarded in the subtitle
            viewHolder.choreSubtitleText.setTextColor(ContextCompat.getColor(mContext, R.color.primaryText));
            if (chore.hasAssignee()) {
                viewHolder.choreSubtitleText.setText("Points awarded: " + chore.getPoints());
            }
            else {
                viewHolder.choreSubtitleText.setText("No points awarded");
            }
        }
    }

    @Override
    public int getItemCount() {
        return mChores.size();
    }

}
