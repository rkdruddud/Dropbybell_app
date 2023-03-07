package com.example.alamproject1;

import android.Manifest; // 이걸 써줘야 manifest접근 가능.
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    MarkerOptions myMarker;
    private GoogleMap mMap;
    private Marker currentMarker = null;



    private static final String TAG = "googlemap";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500;
    private static final int PERMISSIONS_REQUEST_CODE =100;
    boolean needRequest = false;

    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    Location mCurrentLocation;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private com.google.android.gms.location.LocationRequest locationRequest;
    private Location location;

    private View mLayout;

    LatLng previousPosition = null;
    Marker addedMarker = null;
    int tracking =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout2);
        NavigationView navigationView = findViewById(R.id.navigationView);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,R.string.menu_Open,R.string.menu_Close);


        //백그라운드 실행
         new Thread(new Runnable() {
             @Override
             public void run() {

             }
         }).start();

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();  // 타이틀 바 숨기기

        ImageView imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

      ///-->맵
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        locationRequest = new com.google.android.gms.location.LocationRequest().setPriority(LocationRequest.QUALITY_HIGH_ACCURACY).setInterval(UPDATE_INTERVAL_MS).setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);




    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

       setDefaultLocation();




        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            startLocationUpdates();
        }
        else{
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,REQUIRED_PERMISSIONS[0])){
                Snackbar.make(mLayout,"이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,PERMISSIONS_REQUEST_CODE);
                    }
                }).show();
            }
            else {
                ActivityCompat.requestPermissions(this,REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        }


    }
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if(locationList.size() > 0){
                location = locationList.get(locationList.size()-1);
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "lat: "+ String.valueOf(location.getLatitude())+
                        "lon: "+String.valueOf(location.getLongitude());

                setCurrentLocation(location, markerTitle, markerSnippet);
                mCurrentLocation = location;
            }
        }
    };

    private void startLocationUpdates(){
        if(!checkLocationServicesStatus()){
            showDialogForLocationServiceSetting();
        }
        else {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED||hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED){
                return;
            }
            mFusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());

            if(checkPermission()){

                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(checkPermission()){
            mFusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,null);
            if(mMap !=null){
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public String getCurrentAddress(LatLng latLng){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        try{
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1);

        } catch (IOException ioException){
            Toast.makeText(this,"지오코더 서비스 사용불가",Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        }


        if(addresses == null || addresses.size() == 0){
            Toast.makeText(this,"주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }
        else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }

    public boolean checkLocationServicesStatus(){
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet){
        if(currentMarker!= null)currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(),location.getLongitude());

        ImageView currentImagView = findViewById(R.id.imageView3);
        currentImagView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
                mMap.moveCamera(cameraUpdate);
            }
        });

    }

    public void setDefaultLocation(){
        LatLng DEFAULT_LOCATION = new LatLng(37.56,126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if(currentMarker!=null) currentMarker.remove();
/*
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION );
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        currentMarker = mMap.addMarker(markerOptions);*/

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION,15);
        mMap.moveCamera(cameraUpdate);
    }

    //런타임 퍼미션 처리를 위한 메소드
    private boolean checkPermission(){
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length){
            boolean check_result = true;

            for (int result:grantResults){
                if(result != PackageManager.PERMISSION_GRANTED){
                    check_result = false;
                    break;
                }
            }

            if(check_result){
                startLocationUpdates();
            }
            else {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,REQUIRED_PERMISSIONS[0])||ActivityCompat.shouldShowRequestPermissionRationale(this,REQUIRED_PERMISSIONS[1])){
                    Snackbar.make(mLayout,"퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }else {
                    Snackbar.make(mLayout,"퍼미션이 거부되었습니다. 설정에서 퍼미션을 허용해야합니다.", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    }).show();
                }
            }
        }
    }

    //GPS 활성화를 위한 메소드
    private void showDialogForLocationServiceSetting(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"+"위치 설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent,GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case GPS_ENABLE_REQUEST_CODE:
                if(checkLocationServicesStatus()){

                    needRequest = true;
                    return;
                }
                break;
        }

    }
}


