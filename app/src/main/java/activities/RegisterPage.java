package activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.regex.Pattern;
import models.User;

public class RegisterPage extends AppCompatActivity {
    //---------Regex Patterns;------------------------
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-z0-9]+@[a-z]+\\.[a-z]{2,3}"); //username@domain.com
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=])(?=\\S+$).{6,16}$"); //min 6 characters are needed to be saved on Firebase Db.
    //'P@ssw1rd' =>Contains at least one uppercase letter (P), one lowercase letter (s), one digit (0), and one special character (@), Does not contain any whitespace characters and meets the length requirement
    // of 6 characters.

    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+(?:[0-9]?){6,14}[0-9]$"); //+905555555555
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z\\s'-]+$"); //John Doe, O'Connor, Smith-Jones
    private static final Pattern SURNAME_PATTERN = Pattern.compile("^[A-Za-z\\s'-]+$"); //John Doe, O'Connor, Smith-Jones
    //-------------------------------------------

    /*TODO: 'actitivty_register_page.xml' layout'undaki alanlari Regex gibi bir yapiyla kontrol edip FirebaseAuth ve RealTimeDb'ye veri gondermen daha dogru olur.
             Bunlar icin kontrol eklenecek. ==> Eklendi. Firebase'den register esnasinda hatalar icin try-catch bloklari ile ekranda mesaj gosterecek kontroller de eklendi.
             Ayni kontroller Login ekranlarina da eklenecek!!
     */

    /**=>The registered users' email and password informations are added to the FirebaseAuth db and then the additional informations for the user which
    are their name, surname, phone number (with the created User object) are added to the RealTimeDb on the given reference url below after the authentication process is completed.
    With this usage, all of the given registration informations can be added to the related parts of Firebase which are FirebaseAuthentication and RealtimeDatabase.*/

    FirebaseAuth mAuth; // = FirebaseAuth.getInstance();
    DatabaseReference ref = FirebaseDatabase.getInstance().getReferenceFromUrl("https://capstoneprojectdemo1-5706a-default-rtdb.firebaseio.com/");
    User newUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);
        mAuth = FirebaseAuth.getInstance();

        final EditText nameOfUser = findViewById(R.id.txtRegisterName);
        final EditText surnameOfUser = findViewById(R.id.txtRegisterSurname);
        final EditText emailOfUser = findViewById(R.id.txtRegisterEmail);
        final EditText phoneNumberOfUser = findViewById(R.id.txtRegisterPhoneNumber);
        final EditText passwordOfUser = findViewById(R.id.txtRegisterPassword);
        final Button btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = emailOfUser.getText().toString();
                final String password = passwordOfUser.getText().toString();
                final String name = nameOfUser.getText().toString();
                final String surname = surnameOfUser.getText().toString();
                final String phoneNumber = phoneNumberOfUser.getText().toString();

                /*if (email.isEmpty() || password.isEmpty()) {
                    System.out.println(email);
                    System.out.println(password);
                    Toast.makeText(RegisterPage.this, "Please enter your email and password", Toast.LENGTH_SHORT).show();
                    return;
                }*/

                //------------------
                /*if(EMAIL_PATTERN.matcher(email).matches() == false)
                    System.out.println("EMAIL IS WRONG");

                if(PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches() == false) {
                    System.out.println("Phone Number IS WRONG");
                    System.out.println(phoneNumber);
                    System.out.println(PHONE_NUMBER_PATTERN);
                }
                if(NAME_PATTERN.matcher(name).matches() == false)
                    System.out.println("NAME IS WRONG");

                if(SURNAME_PATTERN.matcher(surname).matches() == false)
                    System.out.println("SURNAME IS WRONG");

                if(PASSWORD_PATTERN.matcher(password).matches() == false)
                    System.out.println("PASSWORD IS WRONG");
                 */
                //----------------

                //=>If all inputs does not match with the specified Regex patterns, then Register operation should be cancelled;
                if(!checkInputs(email,EMAIL_PATTERN) || !checkInputs(password, PASSWORD_PATTERN) || !checkInputs(name,NAME_PATTERN)
                        || !checkInputs(surname, SURNAME_PATTERN) || !checkInputs(phoneNumber,PHONE_NUMBER_PATTERN))
                {
                    System.out.println("------------------------------------");
                    System.out.println("Email: " + email + " - " + "Password: "+ password);
                    System.out.println("Name: " + name + " - " + "Surname: " + surname);
                    System.out.println("Phone Number: "+phoneNumber);
                    System.out.println("------------------------------------");
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Create a new user object with additional data
                                    newUser = new User(name, email, surname, phoneNumber);
                                    //System.out.println(newUser.getName());
                                    addExtraInfo(mAuth.getCurrentUser().getUid(), newUser);

                                    //For successful sign-up;
                                    Toast.makeText(RegisterPage.this, "User is registered successfully.",
                                            Toast.LENGTH_SHORT).show();

                                    //move to Login Page
                                    Intent intent = new Intent(RegisterPage.this,LoginPage.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("emailOfUser", email);
                                    bundle.putString("passwordOfUser", password);
                                    intent.putExtras(bundle);
                                    startActivity(intent);

                                } else {
                                    // If sign-up fails, display a message to the user after catching the error with try-catch blocks;
                                    try{
                                        throw task.getException();
                                    }
                                    catch(FirebaseAuthWeakPasswordException ex){
                                        Toast.makeText(RegisterPage.this, "Your password is too weak - Firebase Error",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    catch(FirebaseAuthInvalidCredentialsException ex){
                                        Toast.makeText(RegisterPage.this, "Your email is invalid or already in use, please re-enter it - Firebase Error",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    catch(FirebaseAuthUserCollisionException ex){
                                        Toast.makeText(RegisterPage.this, "User is already registered with this email, try another email - Firebase Error",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    catch (Exception ex){
                                        Toast.makeText(RegisterPage.this, "Registration failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
            }
        });
    }

    //=>This method is created to add shared user informations for registration which will not added to the FirebaseAuthentication, but they will be
    //added to RealTimeDb. These informations are users' name,surname,email and phone number informations. This operation is applied if the registration of user
    //is successful with the given email and password by them.
    public void addExtraInfo(String userId, User user){
        // Save the new user profile to Firebase Realtime Database
        ref.child("users").child(userId).setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    // Write succeeded
                    Toast.makeText(RegisterPage.this, "Additional info is added successfully.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Write failed
                    Toast.makeText(RegisterPage.this, "An error occured while additional info is added!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //=>Regex Control For The Inputs (Validation);
    public boolean checkInputs(String text, Pattern pattern){
        if(text.isEmpty()) {
            Toast.makeText(RegisterPage.this, "All fields must be filled!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(pattern.matcher(text).matches() == false){
            Toast.makeText(RegisterPage.this, "Please check the fields by comparing with the hint texts.",
            Toast.LENGTH_SHORT).show();
            return false;
        }
        else
            return true;
    }
}