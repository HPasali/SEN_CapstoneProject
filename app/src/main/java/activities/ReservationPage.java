package activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.example.capstoneproject_1.R;

public class ReservationPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_page);
    }


    public void moveBackToCarParkDetail(View v){
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void redirectToProfilePage(View v){
        /** TODO: Intent ile rezervasyon yapilan car park bilgisi `Active Reservation Information` olarak ProfilePage'de gosterilmek uzere ProfilePage
         * sayfasina iletilecek
        */
        Intent i = new Intent(this,ProfilePage.class);
        startActivity(i);
    }
}