package com.pbm;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocationListAdapter extends ArrayAdapter<com.pbm.Location> {
	private List<com.pbm.Location> locations;
	private List<com.pbm.Location> filteredLocationList;
	private Context context;
	private Filter filter;

	public LocationListAdapter(Context context, List<com.pbm.Location> locations) {
		super(context, R.layout.location_list_listview, locations);

		this.context = context;
		this.locations = new ArrayList<>(locations);
		this.filteredLocationList = new ArrayList<>(locations);
	}

    @SuppressWarnings("deprecation")
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		View row = convertView;

		if (row == null) {
			LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.location_list_listview, parent, false);

			holder = new ViewHolder();
			holder.name = (TextView) row.findViewById(R.id.machine_info);
			holder.distance = (TextView) row.findViewById(R.id.distance);
			holder.numMachines = (TextView) row.findViewById(R.id.numMachines);
			holder.city = (TextView) row.findViewById(R.id.locationCity);
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		if (filteredLocationList.size() > 0) {
			Location location = filteredLocationList.get(position);
            if (Build.VERSION.SDK_INT >= 24) {
                holder.name.setText(Html.fromHtml("<b>" + location.name + "</b> " + "<i>(" + location.city + ")</i>",Html.FROM_HTML_MODE_LEGACY)); // for 24 api and more
            } else {
                holder.name.setText(Html.fromHtml("<b>" + location.name + "</b> " + "<i>(" + location.city + ")</i>")); // or for older api
            }
			holder.distance.setText(location.milesInfo);
			holder.numMachines.setText(Integer.toString(location.numMachines((PinballMapActivity) context)));
		}

		return row;
	}

	@Override
	public void sort(Comparator<? super Location> comparator) {
		super.sort(comparator);
		Collections.sort(this.locations, comparator);
		Collections.sort(this.filteredLocationList, comparator);
	}

	class ViewHolder {
		TextView name;
		TextView distance;
		TextView numMachines;
		TextView city;
	}

	public Filter getFilter() {
	    if (filter == null) {
	        filter = new LocationFilter();
	    }

	    return filter;
	}
	
	private class LocationFilter extends Filter {
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			String filter = constraint.toString().toLowerCase();

			if(constraint.length() == 0) {
			    ArrayList<com.pbm.Location> list = new ArrayList<>(locations);
			    results.values = list;
			    results.count = list.size();
			} else {
			    ArrayList<com.pbm.Location> newValues = new ArrayList<>();
			    for(int i = 0; i < locations.size(); i++) {
			        com.pbm.Location item = locations.get(i);
			        if (item.name.toLowerCase().contains(filter)) {
			        	newValues.add(item);
			        }
			    }
			    results.values = newValues;
			    results.count = newValues.size();
			}
			
			return results;
		}
			
		protected void publishResults(CharSequence constraint, FilterResults results) {
		    filteredLocationList = (ArrayList<com.pbm.Location>) results.values;

		    clear();

		    for (com.pbm.Location location : filteredLocationList) {
				add(location);
		    }

		    notifyDataSetChanged();
		}
    }
}
