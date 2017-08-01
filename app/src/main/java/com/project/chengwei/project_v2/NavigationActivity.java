package com.project.chengwei.project_v2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class NavigationActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private SQLiteDBHelper dbHelper;
    private Cursor cursor;
    private final int REQUEST_PERMISSION = 10;
    //カメラとの距離は１７に統一
    private GoogleMap mMap;
    ArrayList<LatLng> MarkerPoints;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private LatLng userLocaton;
    private LatLng userHouse;
    private double zoomcounter = 17;
    private int updata = 0;
    private LatLng gpspos;
    private String HomeAddress;
    //private Button zoom=(Button)findViewById(R.id.zoomin);
    //final Button zoomo=(Button)findViewById(R.id.zoomout);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        //Toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_home);
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationIcon(R.drawable.ic_home_white_50dp);
        //Go to home page
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            finish();
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Initializing
        MarkerPoints = new ArrayList<>();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);//航空写真
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            return true;
        }else if(id == R.id.action_settings2){
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);//地図
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        getAddressFromDB();
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);//航空写真
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        homeAddress();
        buttonZoom();
        buttonGps();
        buttonCameraRotation();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        Button start = (Button) findViewById(R.id.start);//開始導航

        // Setting onclick
        // mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

        // @Override
        // public void onMapClick(LatLng point) {
        //touch start button
        start.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {

                                         final LatLng point = null;
                                         // Already two locations
                                         if (MarkerPoints.size() >= 0) {//もと１
                                             MarkerPoints.clear();
                                             mMap.clear();
                                         }

                                         MarkerPoints.add(point);
                                         LatLng origin = userLocaton;
                                         LatLng dest = userHouse;

                                         String url = getUrl(origin, dest);//なぜか固定される
                                         Log.d("onMapClick", url.toString());
                                         FetchUrl FetchUrl = new FetchUrl();

                                         // Start downloading json data from Google Directions API
                                         FetchUrl.execute(url);
                                         //move map camera
                                         //mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                                         //mMap.animateCamera(CameraUpdateFactory.zoomTo((float) zoomcounter));
                                         zoomcounter = 17;
                                         CameraPosition cameraPos = new CameraPosition.Builder().target(userLocaton).zoom((float) zoomcounter).bearing(rotCount).tilt(50).build();
                                         mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));

                                         //distance
                                         distance(userLocaton);

                                         //開始導航後的marker
                                         mMap.addMarker(new MarkerOptions().position(userLocaton).title("Marker").snippet("you").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                         mMap.addMarker(new MarkerOptions().position(userHouse).title("Marker").snippet("your home").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));
                                         updata++;
                                     }

                                     // }
                                 }
        );
        mMap.setMyLocationEnabled(true);//gps表示

        //test address
        //LatLng sydney = new LatLng(22.624384,120.269434);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker").snippet("your home").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));

        // CameraPosition cameraPos = new CameraPosition.Builder().target(sydney).zoom(80.0f).bearing(0).tilt(60).build();
        // mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));

    }

    private double homeLat;
    private double homeLon;

    private void getAddressFromDB(){
        //get home address from DB
        initDB();
        cursor = dbHelper.getProfileData();
        cursor.moveToPosition(0);
        HomeAddress = cursor.getString(cursor.getColumnIndex("address"));
        closeDB();
    }

    private void homeAddress() {

        String Address = HomeAddress;
        //HomeAddress = " 高雄市鼓山區臨海二路50號";//住所入力　高雄市鹽埕區中正四路272號　高雄市鼓山區臨海二路50號

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<android.location.Address> listAddress = geocoder.getFromLocationName(Address, 1);
            android.location.Address addr = listAddress.get(0);
            double latitude = (double) (addr.getLatitude());//緯度
            double longitude = (double) (addr.getLongitude());//経度
            homeLat = latitude;
            homeLon = longitude;
            LatLng sydney = new LatLng(latitude, longitude);
            userHouse = sydney;
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker").snippet("your home").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)));//家marker

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buttonZoom() {
        final Button zoom = (Button) findViewById(R.id.zoomin);
        final Button zoomout = (Button) findViewById(R.id.zoomout);
        zoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CameraPosition cameraPos = mMap.getCameraPosition();
                if (true && zoomcounter <= 20) {
                    zoomcounter = zoomcounter + 0.5;
                    //textView.setText("Hello");
                    mMap.animateCamera(CameraUpdateFactory.zoomTo((float) zoomcounter));
                } else {}
            }
        });

        zoomout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CameraPosition cameraPos = mMap.getCameraPosition();
                if (true && zoomcounter >= 3) {
                    zoomcounter = zoomcounter - 0.5;
                    //textView.setText("World");
                    mMap.animateCamera(CameraUpdateFactory.zoomTo((float) zoomcounter));
                } else {}
            }
        });
    }

    private void buttonGps() {
        final Button gps = (Button) findViewById(R.id.gps);
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CameraPosition cameraPos2 = mMap.getCameraPosition();
                zoomcounter = 17;
                //textView.setText("World");
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(gpspos));
                mMap.animateCamera(CameraUpdateFactory.zoomTo((float) zoomcounter));//カメラとの距離17

                CameraPosition cameraPos = new CameraPosition.Builder().target(userLocaton).zoom((float) zoomcounter).bearing(rotCount).tilt(35).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
            }
        });
    }

    //Rotation
    private int rotCount = 0;
    private void buttonCameraRotation() {
        final Button rotation = (Button) findViewById(R.id.rotation);
        rotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CameraPosition cameraPos = mMap.getCameraPosition();
                LatLng center = new LatLng(cameraPos.target.latitude, cameraPos.target.longitude);
                if (rotCount <= 180) {
                    rotCount = rotCount + 90;
                } else {
                    rotCount = 0;
                }
                CameraPosition cameraPos2 = new CameraPosition.Builder().target(center).zoom((float) zoomcounter).bearing(rotCount).tilt(45).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos2));
            }


        });
    }
    private String getUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_mod = "mod=walking";
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin + "&" + str_dest + "&" + str_mod;
        String output = "json";

        // Building the url to the web service
        //車ルート
        // String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters ;
        //成功
        String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + origin.latitude + "," + origin.longitude + "&destination=" + dest.latitude + "," + dest.longitude + "&avoid=highways&mode=walking";
        //失敗
        //String url = "https://maps.googleapis.com/maps/api/directions/json?origin="+ origin.latitude + "," + origin.longitude+"destination"+ dest.latitude + "," + dest.longitude+"&"+"mod=walking";
        return url;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            ///////////////////////////////////////////////////////////////////////////////
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process//////////////////////////////////////////
        private List<List<HashMap<String, String>>> resultcopy;

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(40);
                lineOptions.color(Color.RED);//導航顏色

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");
            }

            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    private LocationSource.OnLocationChangedListener onLocationChangedListener = null;
    private boolean a = true;

    @Override
    public void onLocationChanged(Location location) {//gps
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        userLocaton = latLng;
        gpspos = latLng;
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));//maker初期位置
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //gpsボタンの表示
        UiSettings settings = mMap.getUiSettings();
        settings.setMyLocationButtonEnabled(false);


        //auto move map gps camera

        if (a != false) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo((float) zoomcounter));//カメラとの距離17
            //zoomcounter=17;固定
            CameraPosition cameraPos = new CameraPosition.Builder().target(latLng).zoom((float) zoomcounter).bearing(rotCount).tilt(38).build();//zoom(17.0f)
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPos));
            a = false;
        }
        userLocaton = latLng;
    }


    //distance
    public void distance(LatLng location) {
        TextView distance = (TextView) findViewById(R.id.distance);
        String distanceText = "null";

        float[] results = new float[1];
        Location.distanceBetween(location.latitude, location.longitude, homeLat, homeLon, results);
        distanceText = String.format("%.2f", (Float) (results[0] / 1000)) + "km";
        //distanceText = String.format("%.2f", (Float) (results[0] / 1000)).toString() + "km";
        distance.setText(distanceText);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    //なんかバグる
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    private void initDB(){
        dbHelper = new SQLiteDBHelper(getApplicationContext());
    }
    //Database : close database
    private void closeDB(){
        dbHelper.close();
    }
}
