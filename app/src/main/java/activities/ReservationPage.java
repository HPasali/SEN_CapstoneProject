package activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.capstoneproject_1.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.security.Timestamp;
import java.util.HashMap;

import models.User;

public class ReservationPage extends AppCompatActivity {
    private  FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String uId = "";
    DatabaseReference reference; //FirebaseDatabase.getInstance().getReferenceFromUrl("https://capstoneprojectdemo1-5706a-default-rtdb.firebaseio.com/");
    String selectedCarPark = "";
    //boolean activeFlag = false;

    private Button btnApply;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_page);

        //=>To get the selected car park info which is sent on CarParkDetailFragment after the user clicks on 'Make Reservation' button;
        Intent intent = getIntent();
        selectedCarPark = intent.getStringExtra("selectedCarPark");

        //To get the logged in user's userId;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("reservations");

        if (currentUser != null)
            uId = currentUser.getUid();
        else
            Toast.makeText(ReservationPage.this, "No user is currently logged in!", Toast.LENGTH_SHORT).show();

        /**=>If there is already an active reservation of the logined user by checking the reservationStatus on Firebase-Realtime Db in the 'hasActiveReservation()' method,
         * then the 'Apply' button will be set as disabled and a toast message will be given. Otherwise, the logined user can make a reservation as defined in the
         * 'makeReservation()' method which is called in the 'setOnClickListener()' method of 'btnApply' as below;*/
        btnApply = findViewById(R.id.btnApply);
        checkActiveReservation(btnApply);
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeReservation(); //**
                Intent i = new Intent(ReservationPage.this,ProfilePage.class);
                startActivity(i);
            }
        });
    }

    public void moveBackToCarParkDetail(View v){
        onBackPressed();
    }


    @Override
    public void onBackPressed() {
        finish();
    }


    /*=>Bu sayfa her gorunur oldugunda asagidaki checkActiveReservation() metodu cagrilacak ve boylece geri butonuna basilip (Profile Page'den) bu sayfaya donulse de aktif rez.
        kontrolu yapilip sonuc donerse yeni rez. yapilmasi engellenecek;*/
    //"onResume" is called every time the activity becomes visible to the user again, including when returning from another activity or pressing the back button.
    @Override
    protected void onResume() {
        super.onResume();
        checkActiveReservation(btnApply);
    }

    private void makeReservation(){
        //=>Save the new reservation of logged in user to Firebase Realtime Database;
        HashMap<String, Object> reservationInfo = new HashMap<>();
        reservationInfo.put("userId",uId);
        reservationInfo.put("reservationDate", ServerValue.TIMESTAMP); //current timestamp by the Firebase Database servers.
        reservationInfo.put("reservedCarPark", selectedCarPark);
        reservationInfo.put("reservationStatus", "ACTIVE");

        /**=>'push()' is used to generate a unique key for the new reservation to be able to send more than one data with the same user id for different reservations to the
             Firebase-Realtime Db; */
        DatabaseReference newRef = reference.push();
        newRef.setValue(reservationInfo, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    // Write succeeded
                    Toast.makeText(ReservationPage.this, "Your reservation is saved successfully.",
                            Toast.LENGTH_SHORT).show();
                    updateTheAvailability(); //to update the isAvailabile value of selected car park after making a reservation.
                } else {
                    // Write failed
                    Toast.makeText(ReservationPage.this, "An error occured while reservation is added!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //=>Login olan kullanicinin aktif rezervasyonu varsa(reservationStatus="ACTIVE") yeniden rezervasyon yapmasina izin verilmeyecek;
    public void checkActiveReservation(Button btn){
        //=>To get the reservation informations of the logined user from Firebase-Realtime Db;
        DatabaseReference refReservations = reference;
        Query query = refReservations.orderByChild("userId").equalTo(uId); //'equalTo()' creates a query constrained to only return child nodes with the given value.
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot resSnapshot: snapshot.getChildren()) {
                    String resStatus = resSnapshot.child("reservationStatus").getValue(String.class);
                    if(resStatus.equals("ACTIVE")){
                        btn.setEnabled(false);
                        Toast.makeText(ReservationPage.this, "You already have an active reservation!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /*=>Rezervasyon yapildiktan sonra car park'ta 1 tane park yeri bulundugu icin locations altindaki 'isAvailable' kolonu false olarak guncellenecek ve sonrasinda
        musait park yeri bulunmamis olacak;*/
    private void updateTheAvailability(){
        DatabaseReference refLocations = FirebaseDatabase.getInstance().getReference().child("locations");
        Query queryLocations = refLocations.orderByChild("title").equalTo(selectedCarPark); //user makes res. to selected car park which is sent on
        /*CarParkDetailFragment after the user clicks on 'Make Reservation' button and it can be used to find the relevant car park to change it's isAvailable field's
          value on the 'locations'.*/
        queryLocations.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();
                    DatabaseReference idRef = refLocations.child(id);
                    idRef.child("isAvailable").setValue(false, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null) {
                                Toast.makeText(ReservationPage.this, "Car park availability is updated successfully.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ReservationPage.this, "Car park availability cannot be updated!",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}

//TODO: Kullanicinin ProfilePage'de yarim saat gectikten sonra rezervasyonunun dusmesi, 'Open' butonuna basmasi veya 'Cancel Reservation' butonuna basmasi halinde aktif
// rezervasyonu 'PASSIVvE' olarak guncellenecek ve en son rezervasyon yapilan car park'in isAvailable degeri true olarak guncellenip tekrar rezerasyon yapilabilmesine olanak
// taninacak.
//TODO:ProfilePage'de login olan kullaniciya ait aktif rezervasyon varsa buna ait bilgiler gosterilecek. Aksi halde 'su anda aktif rezervasyonunuz yoktur denebilir'.