package fz.bvritbusassistant;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StartService extends AppCompatActivity {

    private DatabaseReference databaseReference, clientData;
    private String[] routeCode,phones;
    private int index;
    private FirebaseAuth firebaseAuth;
    private Switch start;
    public static Location locationGlobal;
    private ProgressDialog progressDialog;
    private LocationManager locationManager;
    List<Address> addresses;
    TextView locationTextView,statusButton, routeTextView, longitudeTextView,latitudeTextView, lastUpdateTextView, nearbyPlaces;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_service);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("Bus Tracker Assistant");
        actionBar.hide();
        //Intent intent = getIntent();
        //index = Integer.parseInt(intent.getStringExtra("i"));

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

       // statusButton = findViewById(R.id.serviceStatus);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        clientData = FirebaseDatabase.getInstance().getReference().child("Clients");
        clientData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren() ){
                    ClientDataModel cm = d.getValue(ClientDataModel.class);
                    try {
                        if (FirebaseAuth.getInstance().getUid().equals(cm.getUid())){
                            index = Integer.parseInt(cm.getIndex());
                            progressDialog.dismiss();
                            break;
                        }
                    }catch (NullPointerException e){

                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        routeCode = getResources().getStringArray(R.array.codes);
        phones = getResources().getStringArray(R.array.numbers);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Bus Tracker").child(routeCode[index]);

         start = findViewById(R.id.serviceStartSwitch);

        lastUpdateTextView = findViewById(R.id.lastupdate);
        locationTextView = findViewById(R.id.locationPresent);
        latitudeTextView = findViewById(R.id.latitude);
        longitudeTextView = findViewById(R.id.longitude);
        routeTextView = findViewById(R.id.routeCodeT);
        nearbyPlaces = findViewById(R.id.nearby);

        start.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    startMyService();
                   // statusButton.setText("Tracking service is running");
                }else {
                    stopMyService();
                   // statusButton.setText("Tracking service is stopped");
                }
            }
        });

        progressDialog.dismiss();
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            locationGlobal = location;
        //    Toast.makeText(getApplicationContext(),"Location Changed \n" + location.getLatitude() + "\n" + location.getLongitude() ,Toast.LENGTH_SHORT).show();
            //mMap.clear();
            LatLng sydney = new LatLng(location.getLatitude(),location.getLongitude());

            DataModel dataModel = new DataModel();
            dataModel.setLongitude(String.valueOf(location.getLongitude()));
            longitudeTextView.setText(String.valueOf(location.getLongitude()));
            dataModel.setLatitude(String.valueOf(location.getLatitude()));
            latitudeTextView.setText(String.valueOf(location.getLatitude()));

            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss yyyy/MM/dd");
            Date date = new Date();
            dataModel.setLastSeen(dateFormat.format(date));
            lastUpdateTextView.setText(dateFormat.format(date));
            dataModel.setRouteCode(String.valueOf(routeCode[index]));
            routeTextView.setText(String.valueOf(routeCode[index]));
            dataModel.setPhone(String.valueOf(phones[index]));

            Geocoder gcd = new Geocoder(getApplicationContext(), Locale.getDefault());
            try {
                addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 10);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses.size() > 0) {
                dataModel.setPresentLocation(addresses.get(0).getLocality());
                locationTextView.setText(addresses.get(0).getLocality());
                String nearbyplacesString="";
                for (int i = 1 ; i<addresses.size();i++) {
                    if (addresses.get(i).getLocality()!=null)
                        nearbyplacesString += addresses.get(i).getLocality() + "\n";
                }
                nearbyPlaces.setText(nearbyplacesString);
                dataModel.setNearbyPlaces(nearbyplacesString);
            }
            else {
                // do your stuff
                dataModel.setPresentLocation("Unknown Location");
            }
            databaseReference.setValue(dataModel);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            // Toast.makeText(getApplicationContext(),"Status changed, " + s,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String s) {
            Toast.makeText(getApplicationContext(),s + " enabled" ,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String s) {
            Toast.makeText(getApplicationContext(),s+ " disabled",Toast.LENGTH_SHORT).show();
        }
    };

    private void stopMyService(){
        locationManager = null;
    }

    private void startMyService(){

        if (ContextCompat.checkSelfPermission(StartService.this, "android.permission.ACCESS_FINE_LOCATION")!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(StartService.this,
                    "android.permission.ACCESS_FINE_LOCATION")) {

            } else {
                ActivityCompat.requestPermissions(StartService.this,
                        new String[]{"android.permission.ACCESS_FINE_LOCATION","android.permission.ACCESS_COARSE_LOCATION"},
                        1);
            }
        }else {
            Toast.makeText(getApplicationContext(),"GPS Permission Granted",Toast.LENGTH_SHORT).show();
        }
        progressDialog.show();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 100, mLocationListener); //60000 milliseconds and 10 meters distance
    }
}
