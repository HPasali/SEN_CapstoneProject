package activities;

import static com.example.capstoneproject_1.Constants.MAPVIEW_BUNDLE_KEY;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.example.capstoneproject_1.Constants;
import com.example.capstoneproject_1.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import fragments.CarParkDetailFragment;

/**!!!!INCELENECEK:::: AndroidManifest.xml'de cagrilan ve local.properties'de tanimlanan ${MAPS_API_KEY}  degerinin, Google Cloud icin pricing mailini ve kart
 * bilgilerini girdikten sonra aldigin api-key degeri olmasi gerekiyor. Su an icin api-key alinmadi ama bu islemler tamamlanip api-key alindiktan sonra
 * local.properties altinda api-key degernin degistirilmesi gerekiyor. Ayni zamanda MapsActivity su anda gostermek ve acilip acilmadigini kontrol etmek adina
 * olusturuldu. Harita ekrani MainPage'de gosterilecek ve sonrasinda islem saglaninca MapsActivity de silinebilir. MapView ile atanacak su anda MapFragment ile
 * degil ama api-key aldiktan sonra MapFragment kullanimina da basvurulabilir. Bu saglandiktan sonra, yine haritayi kullanan 'CarParkDetailPage' de
 * olusturulabilir.
* */
public class MainPage extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        initGoogleMap(savedInstanceState);
    }

    private void initGoogleMap(Bundle savedInstanceState){
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
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
        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void moveToProfilePage(View v){
        Intent i = new Intent(this,ProfilePage.class);
        startActivity(i);
    }

    public void moveToCarParkDetail(View v){
        ConstraintLayout constraintView = findViewById(R.id.constraintLayoutMain);
        for (int i = 0; i < constraintView.getChildCount(); i++) {
            View childView = constraintView.getChildAt(i);
            if (!(childView instanceof FrameLayout)) {
                childView.setVisibility(View.GONE);
            }
        }
        if(mMapView.getVisibility() == View.VISIBLE)
            mMapView.setVisibility(View.GONE);

        Fragment mFragment = new CarParkDetailFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.containerCarParkDetail, mFragment).commit();
    }

    /**TODO: Su an icin 'Open CarPark Detail' butonu koyuldu ve bununla CarParkDetail sayfasi aciliyor fakat bu buton kaldirilacak.
     * Cunku bu buton su anda yonlendirmede sorun var mi bunu gormek adina test icin eklendi. Asil yapilmasi planlanan MainPage'de harita
     * uzerinden CarPark'i secip sonrasinda bu dogrultuda CarParkDetailFragment'i acmak (moveToCarParkDetail() metodundaki gibi) ve o carpark'taki
     * musait park yerlerini rezezrvasyon yapabilmek veya navigasyon ile gidebilmek icin CarParkDetaiLFragment'ta daha kucuk bir harita uzerinde gostermek.
     * */
}