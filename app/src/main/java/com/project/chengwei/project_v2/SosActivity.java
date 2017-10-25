package com.project.chengwei.project_v2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static java.lang.String.valueOf;

public class SosActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private String sos_message = "緊急通知！";
    private String myGroup, myPhone, phoneNo;
    private TextView locationText;

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private double lat_gps = 0;
    private double lng_gps = 0;
    private DatabaseReference mDatabaseRef,mDatabaseRef2;
    private MemberData memberData;
    private ArrayList<String> mPhoneList;
    private MediaPlayer sos_sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        //TextView lat = (TextView)findViewById(R.id.lat);
        //TextView lng = (TextView)findViewById(R.id.lng);
        locationText = findViewById(R.id.locationText);
        sos_sound = MediaPlayer.create(this, R.raw.sos);
        mPhoneList = new ArrayList<>();

        getOtherFirebaseMembers();
        //sendSMS();
        //callPhone();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.sos_map);
        mapFragment.getMapAsync(this);

    }
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        //TextView lat=(TextView)findViewById(R.id.lng);
        //TextView lng=(TextView)findViewById(R.id.lat);

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        lat_gps = location.getLatitude();
        lng_gps = location.getLongitude();
        Log.e("my location is:",valueOf(lat_gps)+", "+(valueOf(lng_gps)));
        sos_message = "緊急通知！位置：http://maps.google.com/maps?q=" + valueOf(lat_gps) + "," + (valueOf(lng_gps));

//        lat.setText(valueOf(lat_gps));
//        lng.setText(valueOf(lng_gps));
        String lat = valueOf(lat_gps);
        String lng = valueOf(lng_gps);
        String locaiton = lat + " , "+ lng;
        locationText.setText(locaiton);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------- Firebase -------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    private void getOtherFirebaseMembers() {
        //取得房號
        myGroup = getIntent().getExtras().get("myGroup").toString();
        myPhone = getIntent().getExtras().get("myPhone").toString();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("groups").child(myGroup);
        mDatabaseRef2 = FirebaseDatabase.getInstance().getReference("groups").child(myPhone);
        mDatabaseRef.child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // get all of the children at this level.
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                // shake hands with each of them.'
                for (DataSnapshot child : children) {
                    memberData = child.getValue(MemberData.class);
                    String phone = memberData.getmPhone();
                    if(!phone.equals(myPhone) && !phone.equals("")){
                        //Toast.makeText(getApplicationContext(), "i send to "+ memberData.getmPhone(), Toast.LENGTH_LONG).show();
                        sendSMS(phone,sos_message);
                    }else{
                        //Toast.makeText(getApplicationContext(), "can not send "+ memberData.getmPhone(), Toast.LENGTH_LONG).show();
                    }
                }
                sos_sound.start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
