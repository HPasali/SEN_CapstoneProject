package activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.capstoneproject_1.R;

public class LoginPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
    }

    public void moveToRegister(View v){
        Intent intent = new Intent(this, RegisterPage.class);
        startActivity(intent);
    }

    /**TODO:Asagidaki metot gecici olarak MainPage'e yonlendirme yapmak icin eklendi. Sonrasinda bu kontroller
    //FirebaseAuthentication ile saglanacak.*/
    public void checkCredentialsForLogin(View v){
        EditText txtEmail = (EditText) findViewById(R.id.txtLoginEmail);
        EditText txtPassword = (EditText) findViewById(R.id.txtLoginPassword);
        if(txtEmail.getText().toString().equals("onrozk@gmail.com") && txtPassword.getText().toString().equals("onr123")){
            Intent intent = new Intent(this,MainPage.class);
            startActivity(intent);
        }
        else{
            Toast toast=Toast. makeText(this,"Please check your credentials!",Toast.LENGTH_LONG);
            toast.show();
        }
    }
}
