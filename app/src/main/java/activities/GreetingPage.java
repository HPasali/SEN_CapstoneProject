package activities;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.capstoneproject_1.R;

public class GreetingPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greeting);
        ImageView iv = (ImageView)findViewById(R.id.imageView);
        iv.setImageResource(R.drawable.solaralti_logo);
    }

    public void moveToLogin(View v){
        Intent intent = new Intent(this, LoginPage.class);
        startActivity(intent);
    }

    public void moveToRegister(View v){
        Intent intent = new Intent(this, RegisterPage.class);
        startActivity(intent);
    }
}