package com.find.parkinglot;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import io.reactivex.disposables.CompositeDisposable;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.view.View;
import android.widget.ImageView;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import java.util.List;


public class FindLotFragment extends Fragment implements OnMapReadyCallback {

    LatLng DevicelatLng;
    ArrayList<LatLng> placelist = new ArrayList<LatLng>();
    LatLng bennettuni = new LatLng(28.44969473988111, 77.58116701084592);
    LatLng amityuni = new LatLng(28.465204184541207, 77.48490254707995);
    LatLng parichowk = new LatLng(28.463394823194008, 77.50802855317512);
    LatLng alld = new LatLng(25.467290413035826, 81.85985664882509);
    LatLng  shd = new LatLng(28.670865723709806, 77.4154744531828);

    ArrayList<String> title = new ArrayList<String>();
    String bennett_lot = "Bennett University Parking Lot";
    String alld1 = "Allahabad University Parking Lot";
    String parichowk1 = "Pari Chowk Metro Station";
    String amityuni1 = "Amity University Parking Lot";
    String shd1 = "Shaheed Sthal Bus Station Lot";
    DatabaseReference databaseReference;

    AutoCompleteTextView inputSearch;
    float zoomLevel = 16f;
    String username;
    int j = 0, i = 0;
    private GoogleMap mGoogleMap;
    FusedLocationProviderClient mfusedLocationProviderClient;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    MapFragment supportMapFragment;

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_find_lot, container, false);
        supportMapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.mapID);
        if (supportMapFragment!= null) {
            supportMapFragment.getMapAsync(this);
        }

        placelist.add(bennettuni);
        placelist.add(alld);
        placelist.add(amityuni);
        placelist.add(parichowk);
        placelist.add(shd);

        title.add(bennett_lot);
        title.add(alld1);
        title.add(amityuni1);
        title.add(parichowk1);
        title.add(shd1);



        inputSearch = v.findViewById(R.id.searchMapID);
        ImageView searchIcon = v.findViewById(R.id.magnifyMapID);
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = inputSearch.getText().toString();
                performSearch(searchText);
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Reserved List");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null){
            if(user.getDisplayName()!=null) {
                username = user.getDisplayName();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            init();
        }

        return v;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void init(){
        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_style);
        mGoogleMap.setMapStyle(mapStyleOptions);

        // Add markers of primarily specified parking lot location
        for(i=0; i<placelist.size(); i++){
            if(j==i){
                mGoogleMap.addMarker(new MarkerOptions().position(placelist.get(i)).title(String.valueOf(title.get(j)))
                        .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.lot_marker)));
            }j++;
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(placelist.get(i)));
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(placelist.get(i), zoomLevel));
            mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    String markertitle = marker.getTitle();
                    if(markertitle.equals(username)){
                        Toast.makeText(getActivity(), "You cannot park your car here", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), markertitle, Toast.LENGTH_SHORT).show();
                        BookingAlertDialogFunc(markertitle);
                    }
                    return false;
                }
            });
        }

        // Check permission
        Dexter.withContext(getContext()).withPermission(Manifest.permission.ACCESS_FINE_LOCATION).
                withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                            return;
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

                        mGoogleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.M)
                            @Override
                            public boolean onMyLocationButtonClick() {
                                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
                                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                    return false;
                                }
                                mfusedLocationProviderClient.getLastLocation().addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Snackbar snackbar = Snackbar.make(getView(), "Location permission denied !", Snackbar.LENGTH_LONG);
                                        View sbView = snackbar.getView();
                                        sbView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.Red));
                                        snackbar.setDuration(5000).show();
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        DevicelatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        mGoogleMap.addMarker(new MarkerOptions().position(DevicelatLng).title(username)
                                                .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.self_location)));
                                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(DevicelatLng, zoomLevel));
                                    }
                                });

                                return true;
                            }
                        });

                        // Set device location button layout right bottom
                        View locationButton = ((View)supportMapFragment.getView().findViewById(Integer.parseInt("1"))
                                .getParent()).findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        params.setMargins(0, 0, 0, 500);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Snackbar snackbar = Snackbar.make(getView(), "Location permission denied !", Snackbar.LENGTH_LONG);
                        View sbView = snackbar.getView();
                        sbView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.Red));
                        snackbar.setDuration(5000).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {}
                }).check();
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int VectorID) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, VectorID);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

private void performSearch(String searchText) {
    if (searchText != null && !searchText.isEmpty()) {
        Geocoder geocoder = new Geocoder(getContext());
        try {
            List<Address> addresses = geocoder.getFromLocationName(searchText, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();
                LatLng searchedLocation = new LatLng(latitude, longitude);
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLocation, zoomLevel));
            } else {
                Toast.makeText(getActivity(), "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("Geocoding", "Error fetching location", e);
            Toast.makeText(getActivity(), "Error searching location", Toast.LENGTH_SHORT).show();
        }
    } else {
        Toast.makeText(getActivity(), "Please enter a location", Toast.LENGTH_SHORT).show();
    }
}

    public void BookingAlertDialogFunc(final String markertitle) {
        AlertDialog.Builder alertDialogBuilder;
        alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Do you want to park your car at " + markertitle + " ?");
        alertDialogBuilder.setIcon(R.drawable.park_marker);
        alertDialogBuilder.setCancelable(false);

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String saveCurrentDate, saveCurrentTime;
                Calendar c = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
                saveCurrentDate = currentDate.format(c.getTime());
                SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
                saveCurrentTime = currentTime.format(c.getTime());

                StoreReservedListData(markertitle, saveCurrentDate, saveCurrentTime);
                Toast.makeText(getActivity(), "Added to your reserve list", Toast.LENGTH_LONG).show();
            }
        });
        alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void StoreReservedListData(String markertitle, String saveCurrentDate, String saveCurrentTime){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null) {
            if (user.getDisplayName() != null) {
                username = user.getDisplayName();
            }
        }
        String Key_User_Info = username;
        StoreReservedData storeReservedData;
        storeReservedData = new StoreReservedData(markertitle, saveCurrentDate, saveCurrentTime);
        databaseReference.child(Key_User_Info).setValue(storeReservedData);
    }
}
