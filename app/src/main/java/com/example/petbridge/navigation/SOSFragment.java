package com.example.petbridge.navigation;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.petbridge.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class SOSFragment extends Fragment implements
        OnMapReadyCallback,
        LocationListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener {
    private GoogleMap mMap;
    private SearchView searchView;
    private LocationRequest locationRequest;
    private RequestQueue requestQueue;
    private static final String TAG = "SOSFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private boolean received = false;
    private final boolean followMe = true;

    private String mParam1;
    private String mParam2;

    public SOSFragment() {
    }

    public static SOSFragment newInstance(String param1, String param2) {
        SOSFragment fragment = new SOSFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        requestQueue = Volley.newRequestQueue(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_s_o_s, container, false);

        // Initializing the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // Initializing the search view
        searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handling the search text submission
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;

                if (location != null) {
                    Geocoder geocoder = new Geocoder(requireContext());
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (addressList != null && !addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                        searchAnimalServices(latLng);
                    } else {
                        Toast.makeText(requireContext(), "City not found", Toast.LENGTH_LONG).show();
                    }
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Creating the location request
        locationRequest = createLocationRequest();

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Map location default Milano
        LatLng milano = new LatLng(45.464664, 9.188540);
        mMap.addMarker(new MarkerOptions().position(milano).title("Milano"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(milano, 10));


        // Checking location Permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (GPSActivated()) {
                getCurrentLocation();
                // Start location updates and enable my location on the map
                startLocationUpdate();
            }
            //show user postion with blue icon
            mMap.setMyLocationEnabled(true);
            //listener su click su button position
            mMap.setOnMyLocationButtonClickListener(this);
            //Action Person On postion Button Click
            mMap.setOnMyLocationClickListener(this);
        } else {
            // Request location permission
            requestLocationPermission();
        }
    }

    private LocationRequest createLocationRequest() {
        // Creating a location request for high-accuracy updates
        return new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000);
    }

    private void searchAnimalServices(LatLng location) {
        // Performing a Google Places API search for veterinary clinics or pet stores
        String apiKey = "AIzaSyAbv05CQgCwdKA1iinCyjCVCYE4ZlXTqXc";
        String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?" +
                "query=veterinary+clinic+OR+pet+store" +
                "&location=" + location.latitude + "," + location.longitude +
                "&radius=20000" +
                "&key=" + apiKey;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Processing the API response and updating the map
                        mMap.clear();
                        JSONArray results = response.getJSONArray("results");
                        Log.d(TAG, "numero servizi: " + results.length());
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);
                            JSONObject placeLocation = place.getJSONObject("geometry").getJSONObject("location");
                            double lat = placeLocation.getDouble("lat");
                            double lng = placeLocation.getDouble("lng");
                            String name = place.getString("name");

                            LatLng latLng = new LatLng(lat, lng);
                            mMap.addMarker(new MarkerOptions().position(latLng).title(name)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12));

                    } catch (JSONException e) {
                        Log.e(TAG, "map json : " + e.getMessage());
                    }
                },
                error -> {
                    // Handling errors from the API request
                    Log.e(TAG, "API MAP: " + error.getMessage());
                    Toast.makeText(requireContext(), "Error making API request", Toast.LENGTH_SHORT).show();
                });

        requestQueue.add(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Handling the result of the location permission request
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (GPSActivated()) {
                    getCurrentLocation();
                    startLocationUpdate();
                } else {
                    turnOnGPS();
                }
            }
        }
    }

    private void startLocationUpdate() {
        // Starting location updates using GPS_PROVIDER
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0L,
                    (float) 0,
                    this
            );
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void getCurrentLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (GPSActivated()) {
                    // Requesting location updates using FusedLocationProviderClient
                    LocationServices.getFusedLocationProviderClient(requireContext())
                            .requestLocationUpdates(locationRequest, new LocationCallback() {
                                @Override
                                public void onLocationResult(@NonNull LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(requireContext())
                                            .removeLocationUpdates(this);
                                    if (locationResult != null && locationResult.getLocations().size() > 0) {
                                        Location location = locationResult.getLocations().get(locationResult.getLocations().size() - 1);
                                        double latitude = location.getLatitude();
                                        double longitude = location.getLongitude();
                                        LatLng city = new LatLng(latitude, longitude);
                                        searchAnimalServices(city);
                                    }
                                }
                            }, Looper.getMainLooper());
                } else {
                    turnOnGPS();
                }
            } else {
                // Request location permission if not granted
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    private void turnOnGPS() {
        // Checking and turning on GPS
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(requireContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(task -> {
            try {
                LocationSettingsResponse response = task.getResult(ApiException.class);
                Toast.makeText(requireContext(), "GPS is already turned on", Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                // Handling exceptions from the LocationSettingsResponse
                handleApiException(e);
            }
        });
    }

    private void handleApiException(ApiException e) {
        // Handling exceptions from the LocationSettingsResponse
        switch (e.getStatusCode()) {
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    resolvableApiException.startResolutionForResult(requireActivity(), 2);
                } catch (IntentSender.SendIntentException ex) {
                    ex.printStackTrace();
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                break;
        }
    }

    private boolean GPSActivated() {
        // Checking if GPS is activated
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void requestLocationPermission() {
        // Requesting location permission
        int requestCode = 1;
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                requestCode
        );
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Handling location changes
        double latitude = location.getAltitude();
        double longitude = location.getLongitude();
        if (!received || !followMe) {
            received = true;
            float desiredZoom = 15.0f;
            LatLng newLocation = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(newLocation));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLocation, desiredZoom));
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Handling "My Location" button click
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        // Handling "My Location" click
    }
}
