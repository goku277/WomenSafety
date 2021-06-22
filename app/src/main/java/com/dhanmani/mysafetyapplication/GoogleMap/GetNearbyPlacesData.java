package com.dhanmani.mysafetyapplication.GoogleMap;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    String googlePlacesData;
    GoogleMap googleMap;
    String Url;

    @Override
    protected String doInBackground(Object... objects) {
        googleMap = (GoogleMap) objects[0];
        Url = (String) objects[1];

        DownloadUrl downloadUrl = new DownloadUrl();
        try {
            googlePlacesData = downloadUrl.readUrl(Url);
        } catch (Exception e) {
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s) {
        try {
            JSONObject parentObject = new JSONObject(s);
            JSONArray resultArray = parentObject.getJSONArray("results");

            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject jsonObject = resultArray.getJSONObject(i);
                JSONObject locationObj = jsonObject.getJSONObject("geometry").getJSONObject("location");
                String latitude = locationObj.getString("lat");
                String longitude = locationObj.getString("lng");

                JSONObject nameObject = resultArray.getJSONObject(i);

                String name = nameObject.getString("name");

                LatLng latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(name);
                markerOptions.position(latLng);

                googleMap.addMarker(markerOptions);

            }
        } catch (JSONException e) {
        }
    }
}