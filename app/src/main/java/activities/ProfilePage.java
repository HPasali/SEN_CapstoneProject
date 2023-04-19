package activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.capstoneproject_1.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import fragments.EditProfileFragment;

public class ProfilePage extends AppCompatActivity {

    /** => Guncel yapida RegisterPage'de kullanicidan alinip Firebase uzerinde RealTimeDb'ye kaydedilen name, surname, email gibi bilgiler getirilip
           profil sayfasinda gosterilmekte ve EditProfileFragment'ta bu bilgiler degistirilince db'ye guncellenen verileri gonderip tekrar ProfilePage'e
           donulunce guncellenen veriler gosterilmekte.
     */
   private  FirebaseAuth mAuth;
   private FirebaseUser currentUser;
   private String uId = "";
   private DatabaseReference reference; //FirebaseDatabase.getInstance().getReference().child("users").child();

   /*private String name;
   private String surname;
   private String email;
   private String phoneNumber;*/

    private TextView txtName;
    private TextView txtSurname;
    private TextView txtEmail;
    private TextView txtPhoneNumber;

    /**=>Not:Login olan kullanicinin bilgileri Firebase db'den profil sayfasina yansitiliyor fakat verilerin ilgili viewlere setlenmesi birkac sn aliyor, bu daha hizli veya async yapilabilir mi
       diye bakilabilir. Su an icin duzenle ikonuna tiklaninca tetiklenen "moveToEditProfile()" metodunda email'in bos gelmesi, yani henuz db'den verilerin tam cekilememesi halinde kullaniciya
       bir uyari mesaji verilerek kontrol ediliyor ve veri cekilmezse duzenleme sayfasina gidilip yanlis bir veri guncellemesinin onune gecilmis oluyor. Email alani dolduktan sonra verilerin de
       cekildigi soylenebileceginden, bu durumda ikona tekrar tiklaninca 'EditProfileFragment' sayfasi aciliyor ve login olan kullaniciya ait veriler duzenlenip Firebase uzerinde guncellenip islem
       tamamlaninca ProfilePage'de tekrar gosteriliyor.*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        txtName = findViewById(R.id.txtProfileName);
        txtSurname = findViewById(R.id.txtProfileSurname);
        txtEmail = findViewById(R.id.txtProfileEmail);
        txtPhoneNumber = findViewById(R.id.txtProfilePhoneNumber);

        //To fetch the relevant informations of currently logged in user;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            uId = currentUser.getUid();
            reference = FirebaseDatabase.getInstance().getReference().child("users").child(uId);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //Login olan kullanicinin id'si ile RealTimeDb'ye bir tane veri kaydedileceginden for ile donmeden yalnzica bir veriyi alacak sekidle kontrol saglanailir;
                    txtName.setText(snapshot.child("name").getValue().toString());
                    txtSurname.setText(snapshot.child("surname").getValue().toString());
                    txtEmail.setText(snapshot.child("email").getValue().toString());
                    txtPhoneNumber.setText(snapshot.child("phoneNumber").getValue().toString());
                    System.out.println(txtName.getText() +"-" + txtSurname.getText() +"-"+txtEmail.getText()+"-"+txtPhoneNumber.getText());
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else {
            Toast.makeText(ProfilePage.this, "No user is currently logged in!", Toast.LENGTH_SHORT).show();
        }
    }

    public void moveToEditProfile(View v){
        /**=>This means email field cannot be fetched yet because of the performance issues etc. but it must be set since it cannot be null on the Firebase db for the logined user's infos.
         In order to prevent direct pass to edit page fragment without completing the fetching operation on db, the user can be informed about that and they can wait for a little moment.*/
        if(txtEmail.getText().toString().isEmpty()){
            Toast.makeText(ProfilePage.this, "Please wait until the user informations fetched completely...", Toast.LENGTH_SHORT).show();
            return;
        }

        /*Button button = (Button)findViewById(R.id.button3);
        button.setVisibility(View.INVISIBLE);*/
        // Get the root view of your activity
        ConstraintLayout constraintView = findViewById(R.id.constraintProfile);
        for (int i = 0; i < constraintView.getChildCount(); i++) {
            View childView = constraintView.getChildAt(i);
            if (!(childView instanceof FrameLayout)) {
                // Set the child view's visibility to GONE or INVISIBLE
                childView.setVisibility(View.GONE);
            }
        }
        Fragment mFragment = EditProfileFragment.newInstance(txtName.getText().toString(),txtSurname.getText().toString(),txtEmail.getText().toString(),txtPhoneNumber.getText().toString());
        getSupportFragmentManager().beginTransaction().replace(R.id.container_edit, mFragment).commit();
    }

    //Sign Out Operation;
    public void moveBackToLoginPage(View v){
        mAuth.signOut(); //the current user will be sign out and redirected to Login Page.
        Intent i = new Intent(this, LoginPage.class);
        startActivity(i);
    }
}
