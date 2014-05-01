package com.alexabraham.zmanit.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ZmanimListAdapter extends BaseAdapter {

	private final List<ZmanItem> mItems = new ArrayList<ZmanItem>();

	private final Context mContext;

    private MainFragment mActivity;

	public ZmanimListAdapter(MainFragment activity, Context context) {
		mContext = context;
        mActivity = activity;
	}

	// Add ZmanItem to the list
	// Notify change
	public void add(ZmanItem item) {
		mItems.add(item);
		notifyDataSetChanged();
	}

	// Clears the list adapter of all items.

	public void clear() {
		mItems.clear();
		notifyDataSetChanged();
	}

	// Returns number of ZmanItems
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	// Get ID for ZmanItem
	@Override
	public long getItemId(int position) {
		return position;
	}

	// Get item's position
	// Return -1 if item not found
	private int getPosition(ZmanItem item) {
		int index = 0;
		while (index < mItems.size()) {
			if (item.equals(mItems.get(index))) {
				return index;
			}
			index++;
		}
		return -1;
	}

	// Remove item at position
	public void removeItem(ZmanItem item) {
		mItems.remove(getPosition(item));
		notifyDataSetChanged();
	}

	// Create view to diaplay the ZmanItem at the specified position in mItems
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Get the ZmanItem
		final ZmanItem zmanItem = (ZmanItem) getItem(position);

		// TODO - Inflate the View for this ZmanItem
		// from todo_item.xml.
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout itemLayout = (LinearLayout) inflater.inflate(
				R.layout.zman_item, null);
		itemLayout.setLongClickable(true);
		
		//Find and display mTitleView in titleView
		final TextView titleView = (TextView) itemLayout.findViewById(R.id.titleView);
		titleView.setText(zmanItem.getTitle());
		
		//Find and display time in timeView
		final TextView timeView = (TextView) itemLayout.findViewById(R.id.timeView);
        try {
            timeView.setText(formatTime(zmanItem.getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return itemLayout;
	}

    private String formatTime(Date date) throws java.text.ParseException {
        String result;
        if (mActivity.getTimeFormat() == true) {
            result = (new SimpleDateFormat("h:mm:ss a").format(date));
        } else {
            result = (new SimpleDateFormat("HH:mm:ss").format(date));
        }
        return result;
    }

    public List<ZmanItem> getItemsList(){
        return mItems;
    }

}
