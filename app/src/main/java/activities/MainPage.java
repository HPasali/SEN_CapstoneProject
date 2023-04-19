package activities;
import static helpers.Constants.MAPVIEW_BUNDLE_KEY;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import helpers.Constants;
import com.example.capstoneproject_1.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import fragments.CarParkDetailFragment;


/** A new project and api-key is created on Google Cloud platform. Then this created api-key is given in local.properties in order to be able to fetch the map which is
 * provided by Google Maps. Then this map is placed onto the 'mapViewCarPark' "MapView" that is created on 'acitivity_main_page' layout file.
 *  */

/*TODO:Asagidaki latBau-lonBau'dan farkli olarak lat-lon degerleri girilerek static olarak birden fazla lokasyon 'onMapReady()' metodunda kullanilarak map uzerinde
    gosterilebilir veya asil planlanan yapiya gore 'Firebase RealtimeDb' uzerinde 'location' gibi bir alan(child) olusturup buna children olarak farkli lokasyonlarin
    lat-lon-aktifParkYeriDurumu(true-false) gibi alanlarini ekleyecek sekilde olusturulacak Location(ornegin User class'i gibi 'models' package'i altinda) nesneleri
    ayri ayri idler veya isimlerine gore eklenebilir (RealtimeDb'de 'users' altinda eklenen veriler/valuelar gibi).
    Bu sekilde Firebase uzerinde location bilgileri ve bu loacationlardaki aktif park durumlari kullanicilarin yaptigi rezervasyona gore musait veya dolu olarak gosterilirse
    'CarParkDetailFragment' sayfasindaki 'Available Charge Station'in karsiligi bos yer varsa 1, yoksa 0 olarak gosterilebilir ve bu degere gore 'MakeReservation' butonunun
    clickable olmasi kontrol edilebilir (veya her turlu tiklanir ama rezervasyon icin park yeri secince eger dolu ise uyari mesaji verir).
    => 'MakeReservation' butonuna tiklandiktan sonra acilan ReservationPage'de 3 slot icin secim yapilabilir gibi gosterilse de yalnizca 1 slot icin rezervasyon yapilabilecek
    cunku 1 tane prototip uzerinde islem yapacagiz.
*/
public class MainPage extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private MapView mMapView;
    private double latBau = 41.04237536231388;
    private double lonBau = 29.009312741127506;
    private final float zoomRatio = (float) 13.5;
    private String titleOfTheMarker = "";

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
        // Add a marker in BAU South Campus and move the camera;
        LatLng bauSouthCampus = new LatLng(latBau, lonBau); //-34,151
        map.addMarker(new MarkerOptions().position(bauSouthCampus));
        titleOfTheMarker = "BAU South Campus"; //in order to send it to CarParkDetailFragment when user clicks on the marker.

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bauSouthCampus, zoomRatio));
        map.setOnMarkerClickListener(this); //Set a listener for marker click and will be controlled on the onMarkerClick() method below.
    }

    //=>The below methods is used for redirection to another page;
    public void moveToProfilePage(View v) {
        Intent i = new Intent(this, ProfilePage.class);
        startActivity(i);
    }

    /*public void moveToCarParkDetail(View v) {
        ConstraintLayout constraintView = findViewById(R.id.constraintLayoutMain);
        for (int i = 0; i < constraintView.getChildCount(); i++) {
            View childView = constraintView.getChildAt(i);
            if (!(childView instanceof FrameLayout)) {
                childView.setVisibility(View.GONE);
            }
        }
        if (mMapView.getVisibility() == View.VISIBLE)
            mMapView.setVisibility(View.GONE);

        Fragment mFragment = new CarParkDetailFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.containerCarParkDetail, mFragment).commit();
    }
    */

    public void moveToCarParkDetailFragment(double lat, double lon) {
        ConstraintLayout constraintView = findViewById(R.id.constraintLayoutMain);
        for (int i = 0; i < constraintView.getChildCount(); i++) {
            View childView = constraintView.getChildAt(i);
            if (!(childView instanceof FrameLayout)) {
                childView.setVisibility(View.GONE);
            }
        }
        if (mMapView.getVisibility() == View.VISIBLE)
            mMapView.setVisibility(View.GONE);

        //=>To send the lat-lon values of clicked mark on the map to the CarParkDetail fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putDouble("fetchedLat",lat);
        bundle.putDouble("fetchedLon",lon);
        bundle.putString("markerTitle",titleOfTheMarker);
        CarParkDetailFragment fragment = new CarParkDetailFragment();
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.containerCarParkDetail, fragment).commit();
    }

    /** =>When user clicks on a marker, the user will be redirected to the CarParkDetailFragment page with the selected lat-lon values of the location
        by sending it with bundle in the moveToCarParkDetailFragment() method.
        Also, the user can see the car park location more zoomed after he/she redirected to the CarParkDetailFragment page.
    */
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        moveToCarParkDetailFragment(latBau,lonBau);
        return false;
    }
}