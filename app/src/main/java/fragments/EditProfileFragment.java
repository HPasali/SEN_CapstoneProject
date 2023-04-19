package fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import activities.ProfilePage;
import helpers.RegexPatterns;

import com.example.capstoneproject_1.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditProfileFragment extends Fragment {

    private static final String ARG_NAME = "name";
    private static final String ARG_SURNAME = "surname";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_PHONENUMBER = "phoneNumber";

    //These variables are created to get current user Info and related datas on RealTimeDb;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String uId = "";
    private DatabaseReference reference;
    Map<String, Object> updatedFields = new HashMap<>();

    /*//---------Regex Patterns;------------------------
    //private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-z0-9]+@[a-z]+\\.[a-z]{2,3}"); //username@domain.com
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=_-])(?=\\S+$).{6,16}$"); //min 6 characters are needed to be saved on Firebase Db.
    //'P@ssw1rd' =>Contains at least one uppercase letter (P), one lowercase letter (s), one digit (0), and one special character (@), Does not contain any whitespace characters and meets the
    // length requirement of 6 characters.
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^\\+(?:[0-9]?){6,14}[0-9]$"); //+905555555555
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z\\s'-]+$"); //John Doe, O'Connor, Smith-Jones
    private static final Pattern SURNAME_PATTERN = Pattern.compile("^[A-Za-z\\s'-]+$"); //John Doe, O'Connor, Smith-Jones
    //-------------------------------------------
    */

    private String name;
    private String surname;
    private String email;
    private String phoneNumber;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    /**=>This static constructor takes user as a parameter to show the fetched infos of logined user
     from Firebase Realtime Database which is sent on "ProfilePage" after the user clicked on edit(pen)
     icon to edit his/her infos;
     */
    public static EditProfileFragment newInstance(String paramName, String paramSurname, String paramEmail, String paramPhoneNumber) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, paramName);
        args.putString(ARG_SURNAME, paramSurname);
        args.putString(ARG_EMAIL, paramEmail);
        args.putString(ARG_PHONENUMBER, paramPhoneNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(ARG_NAME);
            surname = getArguments().getString(ARG_SURNAME);
            email = getArguments().getString(ARG_EMAIL);
            phoneNumber = getArguments().getString(ARG_PHONENUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        //To fetch the relevant informations of currently logged in user;
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference().child("users").child(currentUser.getUid());

        EditText txtName = (EditText) view.findViewById(R.id.txtEditTextName);
        EditText txtSurname = (EditText) view.findViewById(R.id.txtEditTextSurname);
        TextView txtEmail = view.findViewById(R.id.txtEditTextEmail);
        EditText txtPhoneNumber =(EditText) view.findViewById(R.id.txtEditTextPhoneNumber);
        EditText txtNewPassword = (EditText) view.findViewById(R.id.txtEditTextPassword);

        //=>Fetched datas from ProfilePage will be assigned to the related fields on the EditProfileFragment;
        txtName.setText(name);
        txtSurname.setText(surname);
        txtEmail.setText(email);
        txtPhoneNumber.setText(phoneNumber);

        Button button = view.findViewById(R.id.btnSave);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //=>If all inputs do not match with the specified Regex patterns, then Edit/Update The Profile operation should give warning messages;
                //=>"!checkEditedInputs(txtEmail.getText().toString(),EMAIL_PATTERN)" removed from if condition since email should not be updated. It changed
                //to TextView component on layout(xml) file. However, it can be seen on the page even it cannot be edited.
                if(!checkEditedInputs(txtName.getText().toString(),RegexPatterns.NAME_PATTERN) || !checkEditedInputs(txtSurname.getText().toString(),RegexPatterns.SURNAME_PATTERN)
                        || !checkPassword(txtNewPassword.getText().toString()))
                {
                    System.out.println("------------------------------------");
                    System.out.println("Email: " + txtEmail.getText().toString() + " - " + "Password: "+ txtNewPassword.getText().toString());
                    System.out.println("Name: " + txtName.getText().toString() + " - " + "Surname: " + txtSurname.getText().toString());
                    System.out.println("Phone Number: "+txtPhoneNumber.getText().toString());
                    System.out.println("------------------------------------");
                    return;
                }

                //If all the inputs are in correct format,then the informations of logined user is updated on Firebase db and reflected on ProfilePage;
                updatedFields.put("name",txtName.getText().toString());
                updatedFields.put("surname",txtSurname.getText().toString());
                //updatedFields.put("email",txtEmail.getText().toString());
                updatedFields.put("phoneNumber",txtPhoneNumber.getText().toString());
                reference.updateChildren(updatedFields);

                //=>To change the password of logined user;
                if(!txtNewPassword.getText().toString().isEmpty()){
                    currentUser.updatePassword(txtNewPassword.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Password updated successfully.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getActivity(), "Error occured while updating the password!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                Toast.makeText(getActivity(), "Your informations are updated successfully.", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getActivity(), ProfilePage.class);
                startActivity(i);
            }
        });
        return view;
    }

    //=>Regex Control For The EditProfileFragment Inputs (Validation) before updating the related values on Firebase RealTimeDb;
    public boolean checkEditedInputs(String text, Pattern pattern){
        if(text.isEmpty()) {
            Toast.makeText(getActivity(), "All fields must be filled!", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(pattern.matcher(text).matches() == false){
            Toast.makeText(getActivity(), "Please check the fields by comparing with the hint texts.", Toast.LENGTH_SHORT).show();
            return false;
        }
        else
            return true;
    }

    //=>Regex control for the password seperately than the other fields since it can be sent null and will stay same(as previous password) then;
    /**If this control is not added, all fields would be checked with checkEditedInputs() method and the password field must be written even if
     it is not going to be changed/updated since there is no known way to get current password of user from Firebase and compare it with the newly
     given one because of the security concerns;*/
    public boolean checkPassword(String password){
        //password can be sent null and will stay same(as previous password) then for this editing page;
        if(password.isEmpty())
            return true;
        else if(RegexPatterns.PASSWORD_PATTERN.matcher(password).matches() == false){
            Toast.makeText(getActivity(), "Please check the password by comparing with the hint text format!", Toast.LENGTH_SHORT).show();
            return false;
        }
        else
            return true;
    }

}