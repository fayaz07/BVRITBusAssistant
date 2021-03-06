# BVRITBusAssistant
---
##### This project is an addition or sub-project of BVRIT Bus Tracker, the functionality of this app is to fetch the location of bus (through the GPS of BUS driver's mobile), latitude, longitude and update it in the [Firebase Realtime Database](https://firebase.google.com/docs/database/) as a [data-model](https://github.com/fayaz07/BVRITBusAssistant/blob/master/app/src/main/java/fz/bvritbusassistant/DataModel.java)

### [Android Manifest File](https://github.com/fayaz07/BVRITBusAssistant/blob/master/app/src/main/AndroidManifest.xml)

* #### Permissions
  
  * Internet
  * Location
  
  ```xml
       
      <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
      <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
      <uses-permission android:name="android.permission.INTERNET"/>
      
  ```
  
* #### Activities
  
  * [MainActivity](https://github.com/fayaz07/BVRITBusAssistant/blob/master/app/src/main/java/fz/bvritbusassistant/MainActivity.java) it is the launcher activity of our app, initially checks if there is authentication provided, if not authenticated, it pops-up a screen asking to choose bus number. After continuing with this process, it automatically fetches the registered number of bus driver and sends an otp to his mobile, he has to enter that to get authenticated
  ---
  
  >  Code checking if authentication is provided
  
  ```java
    if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(MainActivity.this, StartService.class));
            finish();
    }
  ```
  <img src="https://github.com/fayaz07/BVRITBusAssistant/blob/master/busassistant1.png" alt="Home Screen asking to select Route Number" title="Home Screen" width="225" height="400"/>
  &nbsp&nbsp&nbsp&nbsp
  <img src="https://github.com/fayaz07/BVRITBusAssistant/blob/master/busassistant2.png" alt="Screen asking user to enter verification code" title="Verification Screen" width="225" height="400"/>
  &nbsp&nbsp&nbsp&nbsp
  <img src="https://github.com/fayaz07/BVRITBusAssistant/blob/master/Screenshot_1536948195.png" alt="Tracking service screen" title="Tracking Service Screen" width="225" height="400"/>
  
  if authentication is provided or user verified with code, it redirects to [StartService](https://github.com/fayaz07/BVRITBusAssistant/blob/master/app/src/main/java/fz/bvritbusassistant/StartService.java) Activity..  
---
 *  [Authentication Activity](https://github.com/fayaz07/BVRITBusAssistant/blob/master/app/src/main/java/fz/bvritbusassistant/PhoneVerification.java), this activity is all about verifying the user, as shown in above screens, it sends an OTP generated by [Firebase Auth](https://firebase.google.com/docs/auth/) to the registered mobile number. Once verification is done, it fetches the token of verification and stores the data as a [client model](https://github.com/fayaz07/BVRITBusAssistant/blob/master/app/src/main/java/fz/bvritbusassistant/ClientDataModel.java) inf Firebase Database, so that [BVRIT Bus Tracker](https://github.com/fayaz07/BVRITBusTracker) app can track the bus using that client id.
 ---
 
  * [Tracking Service Activity](https://github.com/fayaz07/BVRITBusAssistant/blob/master/app/src/main/java/fz/bvritbusassistant/StartService.java), as shown in the screenshots, this is standalone screen that does the whole work of our purpose, it uses [GPS](https://en.wikipedia.org/wiki/Global_Positioning_System) and internet connection to fetch the location of the device and store it in the database

  > Let me explain the work done by this activity
  
  ```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_service);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("Bus Tracker Assistant");
        actionBar.hide();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        clientData = FirebaseDatabase.getInstance().getReference().child("Clients");
        clientData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren() ){
                    ClientDataModel cm = d.getValue(ClientDataModel.class);
                    if (FirebaseAuth.getInstance().getUid().equals(cm.getUid())){
                        index = Integer.parseInt(cm.getIndex());
                        progressDialog.dismiss();
                        break;
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
  ``` 
  - Let's split it up
  
    - Initializing a location managet object
    ```java
      locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    ```
    
    - Checking the database if the client is registered to avoid spam, if client is real(registered) we will get the id of the client to perform further actions
    ```java
        clientData = FirebaseDatabase.getInstance().getReference().child("Clients");
        clientData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren() ){
                    ClientDataModel cm = d.getValue(ClientDataModel.class);
                    if (FirebaseAuth.getInstance().getUid().equals(cm.getUid())){
                        index = Integer.parseInt(cm.getIndex());
                        progressDialog.dismiss();
                        break;
                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    ```
    
       ![Client model database](https://github.com/fayaz07/BVRITBusAssistant/blob/master/Screenshot%20from%202018-09-15%2000-46-12.png)


     - Using the id we have got from database, we are initializing string resources and getting reference to save tracking instance data by creating a tree there in the database
    ```java
        routeCode = getResources().getStringArray(R.array.codes);
        phones = getResources().getStringArray(R.array.numbers);
    
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Bus Tracker").child(routeCode[index]);
    ```
    - As the user turns on the tracking service this method gets executed, re-intialising the location manager with system service
    ```java
        
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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 100, mLocationListener); //60000    milliseconds and 10 meters distance
     }
    ```
    - The location listener service, the functionality of this is to continously check for location changes __locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 100, mLocationListener);__ for this line of code in the **startService()** method, here I have taken for 60000 milliseconds and 10 meters distance, after this span, it fetches tha latitude, longitude and updates in the database as a [DataModel](https://github.com/fayaz07/BVRITBusAssistant/blob/master/app/src/main/java/fz/bvritbusassistant/DataModel.java). Using [DateFormat](https://docs.oracle.com/javase/7/docs/api/java/text/DateFormat.html) function we will get the appropriate date & time, using [Geocoder](https://developer.android.com/reference/android/location/Geocoder) function
    
    ```java
      
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
      
    ```
    That's it now we are done
    
    ![Datbase showing location data stored as JSON](https://github.com/fayaz07/BVRITBusAssistant/blob/master/Screenshot%20from%202018-09-15%2000-46-12(1).png)
    
    
    
  
