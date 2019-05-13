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
import com.uottawa.thebench.model.Admin;
import com.uottawa.thebench.model.BasicUser;
import com.uottawa.thebench.model.User;

import java.util.List;

/**
 * Adapter class used for the recycler view displaying the users.
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    private User mCurrentUser;

    /**
     * View Holder class which declares the views that will be inflated in the recycler view.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView personAvatarImage;
        public TextView personNameText;
        public TextView personSubtitleText;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;
            personAvatarImage = (ImageView) v.findViewById(R.id.person_avatar_image);
            personNameText = (TextView) v.findViewById(R.id.person_name_text);
            personSubtitleText = (TextView) v.findViewById(R.id.person_subtitle_text);
        }
    }

    public UsersAdapter(Context context, List<User> users, User currentUser) {
        mContext = context;
        mUsers = users;
        mCurrentUser = currentUser;
    }

    /**
     * Set the list of users and notify the recycler view that the list has changed.
     *
     * @param users
     */
    public void setUsers(List<User> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    /**
     * Get the list of users.
     *
     * @return List of users
     */
    public List<User> getUsers() {
        return mUsers;
    }

    /**
     * Remove a user from the list and notify the recycler view.
     *
     * @param user User object
     * @param position Position in the list
     */
    public void removeUser(User user, int position) {
        mUsers.remove(user);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mUsers.size());
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
        View v = inflater.inflate(R.layout.people_row_layout, parent, false);
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
        final User user = mUsers.get(position);
        if (user.getAvatar() != null) {
            viewHolder.personAvatarImage.setImageBitmap(user.getAvatar());
        }
        else {
            viewHolder.personAvatarImage.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.default_avatar));
        }
        viewHolder.personNameText.setText(user.getName());
        if (user.isAdmin()) {
            viewHolder.personSubtitleText.setText("Admin - " + user.getPoints() + " pts");
        }
        else {
            viewHolder.personSubtitleText.setText("User - " + user.getPoints() + " pts");
        }

        // remove background effect on item for basic users (so it doesn't seem clickable)
        if (mCurrentUser == null || mCurrentUser instanceof BasicUser) {
            viewHolder.itemView.setBackground(null);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

}
