package tw.uglyindian;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MapsActivity extends FragmentActivity implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private JSONArray data;
    private LocationClient mLocationClient;
    private Location mCurrentLocation;
    private Marker currentLocationMarker;

    private boolean mUpdatesRequested;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(this, this, this);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        mPrefs = this.getSharedPreferences("THE_UGLY_INDIAN_PREFS", MODE_PRIVATE);

        new LongOperation().execute();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
    }

    @Override
    protected void onPause() {
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
        mEditor.commit();
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    SharedPreferences mPrefs;

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences.Editor mEditor = mPrefs.edit();

        if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested =
                    mPrefs.getBoolean("KEY_UPDATES_ON", false);

        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }
    }

    @Override
    protected void onStop() {
        if (mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(this);
        }
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        Location mCurrentLocation = mLocationClient.getLastLocation();

        SharedPreferences.Editor preferenceEditor = mPrefs.edit();
        preferenceEditor.putFloat("latitude", new Float(mCurrentLocation.getLatitude()));
        preferenceEditor.putFloat("longitude", new Float(mCurrentLocation.getLongitude()));
        preferenceEditor.commit();

        mMap.setMyLocationEnabled(true);
        if(currentLocationMarker != null) currentLocationMarker.remove();

        if (mCurrentLocation != null) {
            BitmapDescriptor addIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_addmarker);
            MarkerOptions options = new MarkerOptions()
                    .position(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()))
                    .icon(addIcon)
                    .title("add")
                    .draggable(false);
            mMap.addMarker(options);
            onLocationChanged(mCurrentLocation);

            currentLocationMarker = mMap.addMarker(options);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent;
                if ("add".equals(marker.getTitle())) {
                    intent = new Intent(getApplicationContext(), ReportActivity.class);
                } else {
                    intent = new Intent(getApplicationContext(), SpotFixDetailViewActivity.class);
                    intent.putExtra("oid", marker.getTitle());
                }
                startActivity(intent);

                return true;
            }
        });

        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setNumUpdates(6);

        if (mUpdatesRequested) {
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        LatLng currentLocation = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14f));
            }

            @Override
            public void onCancel() {
            }
        });
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Error. " + connectionResult.getErrorCode(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... values) {
            DataFetcher.fetchData(new DataFetchListener() {
                @Override
                public void onFetchData(JSONArray response) {
                    data = response;
                }
            });

            return "success";
        }

        @Override
        protected void onPostExecute(String result) {

            for (int i = data.length() - 1; i >= 0; i--) {
                if (mMap != null) {
                    try {
                        JSONObject spot = toJson(data.get(i).toString());
                        mMap.addMarker(
                                new MarkerOptions()
                                        .position(new LatLng(spot.optDouble("latitude"), spot.optDouble("longitude")))
                                        .icon(BitmapDescriptorFactory.defaultMarker(spot.optBoolean("fixed") ?
                                                BitmapDescriptorFactory.HUE_AZURE : BitmapDescriptorFactory.HUE_RED))
                                        .title(toJson(spot.get("_id").toString()).get("$oid").toString())
                        );

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private JSONObject toJson(String jsonString){
            JSONObject jsonObject = null;

            try {
                jsonObject = new JSONObject(jsonString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return jsonObject;
        }
    }
}