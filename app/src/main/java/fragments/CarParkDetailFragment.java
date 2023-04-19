package fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.capstoneproject_1.R;

import activities.MainPage;
import activities.ReservationPage;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CarParkDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CarParkDetailFragment extends Fragment implements OnMapReadyCallback {
    MapView mapView;
    GoogleMap map;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private final float zoomRatio = (float) 15.0;

    private String mParam1;
    private String mParam2;

    /*private static final String ARG_FETCHEDLATVALUE = "latValue";
    private static final String ARG_FETCHEDLONVALUE = "lonValue";
    //------------------------------------
    //=>lat-lon values which is sent from the Main Page when the user clicks on a marker on the map;
    private double fetchedLatValue = 0.0;
    private double fetchedLonValue = 0.0;*/

    public CarParkDetailFragment() {
        // Required empty public constructor
    }

    public static CarParkDetailFragment newInstance(String param1, String param2) {
        CarParkDetailFragment fragment = new CarParkDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        /*args.putDouble(ARG_FETCHEDLATVALUE,lat);
        args.putDouble(ARG_FETCHEDLONVALUE,lon);*/
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_car_park_detail, container, false);
        Button button = (Button) view.findViewById(R.id.btnReservation);
        ImageButton imgButton = (ImageButton) view.findViewById(R.id.btnBackButton2);
        TextView txtCarParkName = (TextView) view.findViewById(R.id.txtNameOfCarPark);

        /*=>The selected car park name from the map on the Main Page will be assigned to the txtCarParkName view if it is not null.
            If it is fetched as null, then the default 'Selected Car Park Name' will be shown;*/
        if(!this.getArguments().getString("markerTitle").isEmpty())
            txtCarParkName.setText(this.getArguments().getString("markerTitle"));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ReservationPage.class);
                startActivity(i);
            }
        });
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), MainPage.class);
                startActivity(i);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mapView = (MapView) view.findViewById(R.id.mapViewCarParkDetail);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);//when you already implement OnMapReadyCallback in your fragment
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        /*=>Add a marker in selected Car Park and move the camera by giving it's lat-lon values and title with the sent datas on bundle
            from the MainPage.java when the user clicks on a marker on the Main Page;*/
        //LatLng markerLocation = new LatLng(41.04237536231388, 29.009312741127506);
        LatLng markerLocation = new LatLng(this.getArguments().getDouble("fetchedLat"), this.getArguments().getDouble("fetchedLon"));
        //googleMap.addMarker(new MarkerOptions().position(markerLocation).title("Marker in Car Park"));
        googleMap.addMarker(new MarkerOptions().position(markerLocation).title(this.getArguments().getString("markerTitle")));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLocation,zoomRatio));
        map=googleMap;
    }
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}

/** Note: Since navigation can directly be provided when the user clicks on a marker point on the map, the 'Navigation' button on the 'fragment_car_park_detail.xml'
 * is removed(wrapped with comment block and the user is directly redirected to different app(Google Maps Navigation) to be able to see navigation after he/she clicks on
 * the marker point on the map.
 * */