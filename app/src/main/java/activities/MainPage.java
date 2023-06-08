package activities;
import static helpers.Constants.MAPVIEW_BUNDLE_KEY;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import helpers.Constants;
import com.example.capstoneproject_1.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import fragments.CarParkDetailFragment;
import models.ArduinoConnection;

/**Note 1:: A new project and api-key is created on Google Cloud platform. Then this created api-key is given in local.properties in order to be able to fetch the map which is
 * provided by Google Maps. Then this map is placed onto the 'mapViewCarPark' "MapView" that is created on 'acitivity_main_page' layout file.
 *  */

/**Not 2: 'Firebase RealtimeDb' uzerinde 'locations' alani olusturuldu ve buna children olarak iki farkli lokasyonun (Ev ve Bau Guney Kampus) lat-lon,title ve bu lat-lon'a karsilik
 *  gelen park yeri icin musaitlik durumunu iceren boolean 'isAvailable' alani eklendi. Bu sekilde, kullanicinin map uzerinden sectigi marker(lokasyon) icin bos park yeri varsa
 *  (karsilik gelen deger 'true' ise) 'CarParkDetailFragment' sayfasindaki 'Available Charge Station' alaninin karsiligi "1", yoksa "0" olarak gosterilecek sekilde kontrol eklendi.
 *  ve yine 'CarParkDetailFragment' sayfasinda 'MakeReservation' butonunun bos park yeri olmamasi halinde disable olup 'Toast' message ile bos yer olmadigina dair mesaj verilmesi
 *  saglandi. Bundle yapisi ile 'CarParkDetailFragment'a iletilen 'markerTitle' degeri uzerinden ilgili otoparkin statusunun 'CarParkDetailFragment' sayfasinda
 *  'fetchCarParkStatus()' metoduna parametre olarak verilip Firebase-RealtimeDb'den cekiliyor.
 * => Ek olarak, 'MakeReservation' butonuna tiklandiktan sonra acilan ReservationPage'de 3 slot icin secim yapilabilir gibi gosterilse de yalnizca 1 slot icin rezervasyon
 * yapilabilecek cunku 1 tane prototip uzerinde islem yapacagiz.*/

public class MainPage extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private MapView mMapView;
    private final float zoomRatio = (float) 13.5;
    private String titleOfTheMarker = "";

    /**Below locations will be shown on the map as default. When user clicks one of these locations' markers, he/she will be redirected to the
       CarParkDetailFragment page by taking the marker's lat-lon and  title data dynamically;*/
    private LatLng bauSouthCampus = new LatLng(41.04237536231388, 29.009312741127506);
    //private LatLng homeLocation; // = new LatLng(41.046647557244526, 29.002310684659246); //Ihlamurdere Caddesi,Besiktas

    DatabaseReference reference; // = FirebaseDatabase.getInstance().getReferenceFromUrl("https://capstoneprojectdb-a0940-default-rtdb.firebaseio.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        initGoogleMap(savedInstanceState);
    }

    private void initGoogleMap(Bundle savedInstanceState) {
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(Constants.MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.mapViewCarPark);
        mMapView.onCreate(mapViewBundle);
        //reference = FirebaseDatabase.getInstance().getReference().child("locations");
        mMapView.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkExceededReservations(); //=>It will check the active reservations which exceeds 30 minutes for every time this page is opened.
        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        //Add a marker in BAU South Campus and move the camera;
        //'.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))' can be given with MarkerOptions() to define a color for the marker.

        /**=>Fetched location datas from Firebase RealtimeDb are assigned to the variables with the usage of getLocationDatas() method which is created below;*/
        getLocationDatas(map); //to get the locations from Realtime Db.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bauSouthCampus, zoomRatio)); //as default zoomed marker on Bau South Campus
        map.setOnMarkerClickListener(this); //Set a listener for marker click and will be controlled on the onMarkerClick() method below.
    }

    //=>The below methods is used for redirection to another page;
    public void moveToProfilePage(View v) {
        Intent i = new Intent(this, ProfilePage.class);
        startActivity(i);
    }

    public void moveToCarParkDetailFragment(double lat, double lon,String title) {
        //=>To remove the current views on the activity page and show the fragment's views on the page;
        ConstraintLayout constraintView = findViewById(R.id.constraintLayoutMain);
        for (int i = 0; i < constraintView.getChildCount(); i++) {
            View childView = constraintView.getChildAt(i);
            if (!(childView instanceof FrameLayout)) {
                childView.setVisibility(View.GONE);
            }
        }
        if (mMapView.getVisibility() == View.VISIBLE)
            mMapView.setVisibility(View.GONE);

        //*=>To send the lat-lon values and title of the clicked marker on the map to the CarParkDetail fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putDouble("fetchedLat",lat);
        bundle.putDouble("fetchedLon",lon);
        bundle.putString("markerTitle",title);
        CarParkDetailFragment fragment = new CarParkDetailFragment();
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.containerCarParkDetail, fragment).commit();
    }

    /** =>When user clicks on a marker, the user will be redirected to the CarParkDetailFragment page with the selected lat-lon values of the location
        by sending it with bundle in the moveToCarParkDetailFragment() method. Since the markers are added to map object, the 'setOnMarkerClickListener()'
        method listens which marker is clicked dynamically and this marker's lat-lon and title datas can be fetched with 'marker.getPosition()' and 'marker.getTitle()'
        as below.
        => Moreover, the user can see the car park location more zoomed after he/she redirected to the CarParkDetailFragment page.*/
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        moveToCarParkDetailFragment(marker.getPosition().latitude,marker.getPosition().longitude,marker.getTitle());
        return false;
    }

    //=>To get and return the location(lat-lon) datas from Firebase;
    public void getLocationDatas(GoogleMap map){
        reference = FirebaseDatabase.getInstance().getReference().child("locations"); //to define where is the stored 'location' datas on Firebase.
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Iterate over the data and extract the latitude and longitude values from each location.
                for (DataSnapshot locSnapshot : snapshot.getChildren()) {
                    double latitude = locSnapshot.child("lat").getValue(Double.class);
                    double longitude = locSnapshot.child("lon").getValue(Double.class);
                    //Add markers for each location on the map;
                    LatLng location = new LatLng(latitude, longitude);
                    MarkerOptions markerOptions = new MarkerOptions().position(location);
                    markerOptions.title(locSnapshot.child("title").getValue().toString());
                    map.addMarker(markerOptions);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /*=>This method checks the 'ACTIVE' reservations whose reservations date exceeds 30 minutes (by comparing it with the current time)
        and updates their status as 'PASSIVE' status accordingly;*/
    private void checkExceededReservations() {
        DatabaseReference refReservations = FirebaseDatabase.getInstance().getReference().child("reservations");
        refReservations.orderByChild("reservationStatus").equalTo("ACTIVE").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Long reservationDate = snapshot.child("reservationDate").getValue(Long.class);
                    if (reservationDate != null) {
                        long currentTime = System.currentTimeMillis();
                        long timeDiffMillis = currentTime - reservationDate;
                        long timeDiffMinutes = TimeUnit.MILLISECONDS.toMinutes(timeDiffMillis);
                        //-----------------------------------------------------------------
                        if (timeDiffMinutes >= 30) {
                            if(snapshot.child("reservedCarPark").getValue(String.class).equals("BAU South Campus")){
                                ArduinoConnection.sendCommand("/Lock=ON");
                            }
                            // The reservation has passed thirty minutes, update the reservationStatus
                            String reservationKey = snapshot.getKey();
                            DatabaseReference reservationRef = refReservations.child(reservationKey);
                            reservationRef.child("reservationStatus").setValue("PASSIVE", new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                    if (error == null) {
                                        Toast.makeText(MainPage.this, "Active reservations which passes 30 minutes are updated!", Toast.LENGTH_SHORT).show();
                                        //=>Belowed function is defined on the ProfilePage before and called to update the reserved car park's availability as available (true);
                                        ProfilePage.updateCarParkAvailability(snapshot.child("reservedCarPark").getValue(String.class));
                                    } else {
                                        Toast.makeText(MainPage.this, "Active reservations cannot be updated!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            /*System.out.println(timeDiffMinutes);
                            System.out.println(snapshot.child("reservedCarPark").getValue());*/
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that occur during the data retrieval
            }
        });
    }
}