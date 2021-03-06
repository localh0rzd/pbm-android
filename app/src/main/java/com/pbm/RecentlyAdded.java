package com.pbm;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class RecentlyAdded extends PinballMapActivity {
	private List<Spanned> recentAdds = new ArrayList<>();

	public void onCreate(Bundle savedInstanceState) {
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recently_added);

		logAnalyticsHit("com.pbm.RecentlyAdded");

		new Thread(new Runnable() {
			public void run() {
			try {
				getLocationData();
			} catch (UnsupportedEncodingException | InterruptedException | ExecutionException | JSONException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
				RecentlyAdded.super.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showTable(recentAdds);
				}
			});
			}
		}).start();
	}

    @SuppressWarnings("deprecation")
	public void getLocationData() throws UnsupportedEncodingException, InterruptedException, ExecutionException, JSONException, ParseException {
		int NUM_ADDED_TO_SHOW = 20;
		PBMApplication app = getPBMApplication();

		String json = new RetrieveJsonTask().execute(
			app.requestWithAuthDetails(regionBase + "location_machine_xrefs.json?limit=" + NUM_ADDED_TO_SHOW),
			"GET"
		).get();

		if (json == null) {
			return;
		}

		JSONObject jsonObject = new JSONObject(json);
		JSONArray lmxes = jsonObject.getJSONArray("location_machine_xrefs");

		for (int i = 0; i < lmxes.length(); i++) {
			JSONObject lmxJson = lmxes.getJSONObject(i);

			int id = lmxJson.getInt("id");
			String rawCreatedAt = lmxJson.getString("created_at").split("T")[0];
			LocationMachineXref lmx = app.getLmx(id);

			DateFormat inputDF = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			DateFormat outputDF = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
			Date dateCreatedAt = inputDF.parse(rawCreatedAt);
			String createdAt = outputDF.format(dateCreatedAt);

			String textToShow = "<b>" + lmx.getMachine(this).name + "</b> was added to <b>" + lmx.getLocation(this).name + "</b> (" + lmx.getLocation(this).city + ")";
			textToShow += "<br /><small>" + createdAt + "</small>";

            if (Build.VERSION.SDK_INT >= 24) {
                recentAdds.add(Html.fromHtml(textToShow,Html.FROM_HTML_MODE_LEGACY)); // for 24 api and more
            } else {
                recentAdds.add(Html.fromHtml(textToShow)); // or for older api
            }
		}
	}

	public void showTable(List<Spanned> locations) {
		ListView table = (ListView) findViewById(R.id.recentlyAddedTable);

		table.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parentView, View selectedView, int position, long id) {
			Intent myIntent = new Intent();
			Spanned spanned = (Spanned) parentView.getItemAtPosition(position);
			String locationName = spanned.toString().split(" was added to ")[1];
			locationName = locationName.split(" \\(")[0];
			locationName = locationName.split("\n")[0];

			PBMApplication app = getPBMApplication();
			Location location = app.getLocationByName(locationName);

			if (location == null) {
				Toast.makeText(getBaseContext(), "Sorry, can't find that location", Toast.LENGTH_LONG).show();
				return;
			}

			myIntent.putExtra("Location", location);
			myIntent.setClassName("com.pbm", "com.pbm.LocationDetail");
			startActivityForResult(myIntent, QUIT_RESULT);
			}
		});

		table.setAdapter(new ArrayAdapter<>(this, R.layout.custom_list_item_1, locations));
	}
}