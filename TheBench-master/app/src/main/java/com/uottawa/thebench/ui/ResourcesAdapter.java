package com.uottawa.thebench.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.uottawa.thebench.R;
import com.uottawa.thebench.model.Resource;
import com.uottawa.thebench.utils.DBHandler;

import java.util.List;

public class ResourcesAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<Resource> mResources;
    private final int mBackgroundColor;
    private final boolean mEditable;

    static class ViewHolder {

        private TextView resourceNameText;
        private CheckBox resourceCheckBox;
        private RelativeLayout resourceLayout;

    }

    public ResourcesAdapter(Context context, List<Resource> resources, int backgroundColor, boolean editable) {
        mContext = context;
        mBackgroundColor = backgroundColor;
        mEditable = editable;

        // If odd number of resources, add extra empty one to fill gridview
        if((resources.size()%2) != 0) {
            resources.add(null);
        }
        mResources = resources;
    }

    @Override
    public int getCount() {
        return mResources.size();
    }

    @Override
    public Object getItem(int i) {
        return mResources.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder = null;

        if (view == null) {
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            // Inflate the resource layout
            view = layoutInflater.inflate(R.layout.resource_grid_item_layout, viewGroup, false);

            viewHolder.resourceNameText = (TextView) view.findViewById(R.id.resource_name_text);
            viewHolder.resourceCheckBox = (CheckBox) view.findViewById(R.id.resource_check_box);
            viewHolder.resourceLayout = (RelativeLayout) view.findViewById(R.id.resource_layout);

            view.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final Resource resource = mResources.get(position);

        if (resource != null) {
            viewHolder.resourceNameText.setVisibility(View.VISIBLE);
            viewHolder.resourceCheckBox.setVisibility(View.VISIBLE);

            if (mEditable) {
                viewHolder.resourceNameText.setClickable(true);
                viewHolder.resourceCheckBox.setEnabled(true);
            } else {
                viewHolder.resourceNameText.setClickable(false);
                viewHolder.resourceCheckBox.setEnabled(false);
            }

            viewHolder.resourceNameText.setText(resource.getName());

            if (mEditable) {
                viewHolder.resourceNameText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CharSequence options[] = new CharSequence[]{"Delete"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setTitle(resource.getName());
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    DBHandler dbHandler = new DBHandler(mContext);
                                    dbHandler.deleteResource(resource);
                                    mResources.remove(resource);

                                    int lastResourceIndex = mResources.size() - 1;
                                    if (lastResourceIndex >= 0 && mResources.get(lastResourceIndex) == null) {
                                        mResources.remove(lastResourceIndex);
                                    }

                                    // If odd number of resources, add extra empty one to fill gridview
                                    if ((mResources.size() % 2) != 0) {
                                        mResources.add(null);
                                    }

                                    // Update the grid view
                                    notifyDataSetChanged();
                                }
                            }
                        });
                        builder.show();
                    }
                });
            }

            viewHolder.resourceCheckBox.setChecked(resource.isChecked());

            viewHolder.resourceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    resource.setChecked(checked);
                    DBHandler dbHandler = new DBHandler(mContext);
                    dbHandler.updateResource(resource);
                }
            });
        } else {
            viewHolder.resourceNameText.setVisibility(View.INVISIBLE);
            viewHolder.resourceCheckBox.setVisibility(View.INVISIBLE);
        }

        viewHolder.resourceLayout.setBackgroundColor(mBackgroundColor);

        return view;
    }
}
