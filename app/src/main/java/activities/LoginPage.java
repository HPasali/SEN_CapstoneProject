package activities;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.capstoneproject_1.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;
//import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {
    //---------Regex Patterns;------------------------
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-z0-9]+@[a-z]+\\.[a-z]{2,3}"); //username@domain.com
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=])(?=\\S+$).{6,16}$"); //min 6 characters are needed to be saved on Firebase Db.

    private FirebaseAuth mAuth =  FirebaseAuth.getInstance(); // Initialize Firebase Auth
    String userEmail = "";
    String userPassword = "";


    //=>Login olan kullanici icin farkli bir islem yapilacaksa burada anlik login olan kullanici bilgisini alip kontrol edebilirsin.
    /*@Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //reload();
        }
    }
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        final EditText txtEmail = (EditText) findViewById(R.id.txtLoginEmail);
        final EditText txtPassword = (EditText) findViewById(R.id.txtLoginPassword);
        final Button btnLogin = findViewById(R.id.btnLogin);

        //=>Kullanici register oldugunda Login Page'e iletilen email ve password bilgisii burada alip
        //ekranda default olarak gostermek icin. Bu sekilde islem yapilmazsa alanlar ekrana 'hint' textleri ile gelir.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            userEmail = extras.getString("emailOfUser");
            userPassword = extras.getString("passwordOfUser");
            txtEmail.setText(userEmail);
            txtPassword.setText(userPassword);
            // Use the email and password to authenticate the user
        }

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = txtEmail.getText().toString();
                final String password = txtPassword.getText().toString();

                /*if (email.isEmpty() || password.isEmpty()) {
                    System.out.println(email);
                    System.out.println(password);
                    Toast.makeText(LoginPage.this, "Please enter your email and password", Toast.LENGTH_SHORT).show();
                    return;
                }*/

                //=>If all inputs' formats do not match with the specified Regex patterns, then Login operation should fail;
                if(!checkLoginInputs(email,EMAIL_PATTERN) || !checkLoginInputs(password, PASSWORD_PATTERN))
                {
                    System.out.println("------------------------------------");
                    System.out.println("Email: " + email);
                    System.out.println("Password: "+ password);
                    System.out.println("------------------------------------");
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //For successful sign-in(login);
                                    Toast.makeText(LoginPage.this, "Logined successfully.",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginPage.this,MainPage.class);
                                    startActivity(intent);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(LoginPage.this, "Login failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
               }
        });
    }

    public void moveToRegister(View v){
        Intent intent = new Intent(this, RegisterPage.class);
        startActivity(intent);
    }

    //=>Asagidaki metot gecici olarak kontrol saglamak icin eklenmisti, bu kontrol guncel olarak Firebase Authentication uzerinden saglaniyor;
    /*
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
    }*/

    //=>Regex Control For The Login Inputs (Email and Password Validation);
    public boolean checkLoginInputs(String text, Pattern pattern){
        if(text.isEmpty()) {
            Toast.makeText(LoginPage.this, "Please enter your email and password", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(pattern.matcher(text).matches() == false){
            Toast.makeText(LoginPage.this, "Entered values are in the wrong format, please check the hint texts.",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        else
            return true;
    }
}
