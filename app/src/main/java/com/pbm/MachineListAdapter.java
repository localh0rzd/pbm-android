package com.pbm;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MachineListAdapter extends ArrayAdapter<com.pbm.Machine> {
	private List<com.pbm.Machine> machines;
	private List<com.pbm.Machine> filteredMachineList;
	private boolean disableSelectImage;
	private Filter filter;

	public MachineListAdapter(Context context, List<com.pbm.Machine> machines, boolean disableSelectImage) {
		super(context, R.layout.machine_list_listview, machines);

		this.machines = new ArrayList<>(machines);
		this.filteredMachineList = new ArrayList<>(machines);

		this.disableSelectImage = disableSelectImage;
	}

    @SuppressWarnings("deprecation")
	public View getView(final int position, View convertView, ViewGroup parent) {
		MachineViewHolder holder;
		View row = convertView;

		if (row == null) {
			LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = layoutInflater.inflate(R.layout.machine_list_listview, parent, false);

			holder = new MachineViewHolder();
			holder.name = (TextView) row.findViewById(R.id.machine_info);
			holder.machineSelectButton = (ImageView) row.findViewById(R.id.machineSelectButton);

			row.setTag(holder);
		} else {
			holder = (MachineViewHolder) row.getTag();
		}

		Machine machine = filteredMachineList.get(position);
		if (Build.VERSION.SDK_INT >= 24) {
            holder.name.setText(Html.fromHtml("<b>" + machine.name + "</b>" + " " + "<i>" + machine.metaData() + "</i>",Html.FROM_HTML_MODE_LEGACY)); // for 24 api and more
		} else {
            holder.name.setText(Html.fromHtml("<b>" + machine.name + "</b>" + " " + "<i>" + machine.metaData() + "</i>")); // or for older api
		}

		if (disableSelectImage) {
			holder.machineSelectButton.setVisibility(View.INVISIBLE);
		}

		return row;
	}

	class MachineViewHolder {
		TextView name;
		TextView metaData;
		ImageView machineSelectButton;
	}

	public Filter getFilter() {
		if (filter == null) {
			filter = new MachineFilter();
		}

		return filter;
	}

	private class MachineFilter extends Filter {
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			String filter = constraint.toString().toLowerCase();

			if (constraint == null || constraint.length() == 0) {
				ArrayList<com.pbm.Machine> list = new ArrayList<>(machines);
				results.values = list;
				results.count = list.size();
			} else {
				ArrayList<com.pbm.Machine> newValues = new ArrayList<>();
				for (int i = 0; i < machines.size(); i++) {
					com.pbm.Machine item = machines.get(i);
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
			filteredMachineList = (ArrayList<com.pbm.Machine>) results.values;

			clear();

			for (Machine machine : filteredMachineList) {
				add(machine);
			}

			notifyDataSetChanged();
		}
	}
}
