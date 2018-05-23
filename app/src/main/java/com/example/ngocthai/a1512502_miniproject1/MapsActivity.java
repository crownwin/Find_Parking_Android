package com.example.ngocthai.a1512502_miniproject1;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        DirectionFinderListener {

    private GoogleMap mMap;
    private EditText edtParking;
    private Button btnFindParking;
    private Button btnAddParking;
    private Button btnFindPath;

    private List<Parking> listParking = new ArrayList<Parking>();

    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        edtParking = (EditText) findViewById(R.id.edtParking);

        btnFindParking = (Button) findViewById(R.id.btnFindParking);
        btnFindParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendFindParkingRequest();
            }
        });

        btnAddParking = (Button) findViewById(R.id.btnAddParking);
        btnAddParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAddParkingRequest();
            }
        });

        btnFindPath = (Button) findViewById(R.id.btnFind_Path);
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFindPathRequest();
            }
        });
    }


    private void sendFindParkingRequest() {
        mMap.clear();
        String parking_position = edtParking.getText().toString();
        if (parking_position.isEmpty()) {
            Toast.makeText(this, R.string.enter_parking_again, Toast.LENGTH_SHORT).show();
            return;
        }
        Marker place = searchLocation(parking_position);
        if (place != null) {
            mMap.addMarker(new MarkerOptions().title(place.getTitle()).position(place.getPosition()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getPosition(), 15));

        } else {
            Toast.makeText(this, R.string.edit_parking, Toast.LENGTH_SHORT).show();
        }
        show_list_parking();
    }

    private void sendAddParkingRequest() {
        mMap.clear();
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(MapsActivity.this);
        View mview = getLayoutInflater().inflate(R.layout.dialog_add_parking, null);

        final AlertDialog dialog = mbuilder.setView(mview).create();
        dialog.show();

        final EditText mName = (EditText) mview.findViewById(R.id.edtNameParking);
        final EditText mAddress = (EditText) mview.findViewById(R.id.edtAddress);
        final EditText mCost = (EditText) mview.findViewById(R.id.edtCost);

        Button mOK = (Button) mview.findViewById(R.id.btnOK);
        mOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = mName.getText().toString();
                String address = mAddress.getText().toString();
                String cost = mCost.getText().toString();
                if (!name.isEmpty() && !address.isEmpty() && !cost.isEmpty()) {
                    Marker place = searchLocation(address);
                    if (place == null) {
                        Toast.makeText(MapsActivity.this,
                                R.string.edit_address, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MapsActivity.this,
                                R.string.ok_addParking, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                        Parking parking = new Parking(name, address, place.getPosition(), cost);
                        listParking.add(parking);

                        update_data();
                        show_list_parking();
                    }
                } else {
                    Toast.makeText(MapsActivity.this,
                            R.string.error_addParking, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button mCancel = (Button) mview.findViewById(R.id.btnCancel);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        show_list_parking();
    }

    private void sendFindPathRequest() {
        mMap.clear();
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(MapsActivity.this);
        final View mview = getLayoutInflater().inflate(R.layout.find_direction, null);

        final AlertDialog dialog = mbuilder.setView(mview).create();
        dialog.show();

        final EditText mOrigin = (EditText) mview.findViewById(R.id.edtOrigin);
        final EditText mDes = (EditText) mview.findViewById(R.id.edtDes);

        Button mFind = (Button) mview.findViewById(R.id.btnFind_Direction);
        mFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String origin = mOrigin.getText().toString();
                String des = mDes.getText().toString();
                if (!origin.isEmpty() && !des.isEmpty()) {
                    Marker place1 = searchLocation(origin);
                    Marker place2 = searchLocation(des);
                    if (place1 == null || place2 == null) {
                        Toast.makeText(MapsActivity.this,
                                R.string.edit_origin_des, Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            new DirectionFinder(MapsActivity.this, origin, des).execute();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                } else {
                    Toast.makeText(MapsActivity.this,
                            R.string.fill_in, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button mCancel = (Button) mview.findViewById(R.id.btnCancel_Find);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        show_list_parking();
    }

    private void sendFindPathRequest(String des_address) {
        mMap.clear();
        AlertDialog.Builder mbuilder = new AlertDialog.Builder(MapsActivity.this);
        final View mview = getLayoutInflater().inflate(R.layout.go_parking, null);

        final AlertDialog dialog = mbuilder.setView(mview).create();
        dialog.show();

        final EditText mOrigin = (EditText) mview.findViewById(R.id.edtOrigin_go);
        final TextView mDes = (TextView) mview.findViewById(R.id.edtDes_go);
        mDes.setText(des_address);

        Button mFind = (Button) mview.findViewById(R.id.btnFind_Direction_go);
        mFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String origin = mOrigin.getText().toString();
                String des = mDes.getText().toString();
                if (!origin.isEmpty() && !des.isEmpty()) {
                    Marker place1 = searchLocation(origin);
                    Marker place2 = searchLocation(des);
                    if (place1 == null || place2 == null) {
                        Toast.makeText(MapsActivity.this,
                                R.string.edit_origin, Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            new DirectionFinder(MapsActivity.this, origin, des).execute();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        dialog.dismiss();
                    }
                } else {
                    Toast.makeText(MapsActivity.this,
                            R.string.fill_in, Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button mCancel = (Button) mview.findViewById(R.id.btnCancel_Find_go);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        show_list_parking();
    };

    Marker searchLocation(String parking_position) {
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocationName(parking_position, 5);
            if (addresses.size() > 0) {
                Double lat = (double) (addresses.get(0).getLatitude());
                Double lon = (double) (addresses.get(0).getLongitude());

                Log.d("lat-long", "" + lat + "......." + lon);
                final LatLng user = new LatLng(lat, lon);
                Marker location = mMap.addMarker(new MarkerOptions()
                        .position(user)
                        .title(parking_position)
                        .visible(false));
                return location;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

        mMap = googleMap;
        LatLng hcmus = new LatLng(10.762446, 106.681198);
        Marker myMarker = mMap.addMarker(new MarkerOptions().
                        position(hcmus).
                        title(getString(R.string.hcmus)).
                        visible(false));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom((hcmus), 15));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        load_data();

        show_list_parking();


        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                for(int i = 0; i < listParking.size(); i++) {
                    if (marker.getTitle().equals(listParking.get(i).getName())) {
                        sendFindPathRequest(listParking.get(i).getAddress());
                    }
                }
            }
        });
    }


    @Override
    public void onDirectionFinderStart() {
        mMap.clear();
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));

            ((TextView) findViewById(R.id.txtDuration)).setText(route.duration.text);
            ((TextView) findViewById(R.id.txtDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(route.startAddress)
                    .visible(true)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(route.endAddress)
                    .visible(true)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    visible(true).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
        show_list_parking();

    }

    private void load_data(){
        try {
            File file = getApplicationContext().getFileStreamPath(getString(R.string.file_list_parking));
            String lineFromFile;
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(openFileInput(getString(R.string.file_list_parking))));
                while ((lineFromFile = reader.readLine()) != null) {
                    String name = lineFromFile;
                    String address = reader.readLine();
                    String[] location = reader.readLine().split(" ");
                    LatLng latLng = new LatLng((Double.parseDouble(location[0])), Double.parseDouble(location[1]));
                    String cost = reader.readLine();
                    Parking parking = new Parking(name, address, latLng, cost);
                    listParking.add(parking);
                }
            }
        }catch (IOException ex){
                Toast.makeText(MapsActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
    }

    private void update_data(){
        try{
            FileOutputStream file = openFileOutput(getString(R.string.file_list_parking), MODE_PRIVATE);
            OutputStreamWriter outputFile = new OutputStreamWriter(file);
            for(int i = 0; i < listParking.size(); i++){
                outputFile.write(listParking.get(i).getName() + "\n");
                outputFile.write(listParking.get(i).getAddress() + "\n");
                double lat = listParking.get(i).getLatlng().latitude;
                double lng = listParking.get(i).getLatlng().longitude;
                outputFile.write(lat + " " + lng + "\n");
                outputFile.write(listParking.get(i).getCost() + "\n");
            }
            outputFile.flush(); // make sure all the files are written to the file
            outputFile.close(); // closes the file

        }catch (IOException ex){
            Toast.makeText(this, "Error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void show_list_parking() {
        int height = 150;
        int width = 150;
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.logo_parking);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        for (int i = 0; i < listParking.size(); i++) {
            Parking parking = listParking.get(i);
            mMap.addMarker(new MarkerOptions()
                    .title(parking.getName())
                    .position(parking.getLatlng())
                    .snippet(parking.getCost() + " vnd")
                    .icon(BitmapDescriptorFactory.fromBitmap(smallMarker)));
        }
    }

}