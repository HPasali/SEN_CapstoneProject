package activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.example.capstoneproject_1.R;

import fragments.EditProfileFragment;

public class ProfilePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);
        //System.out.println("deneme123");
    }

    public void moveToEditProfile(View v){
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
        Fragment mFragment = new EditProfileFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.container_edit, mFragment).commit();
    }

    public void moveBackToLoginPage(View v){
        Intent i = new Intent(this, LoginPage.class);
        startActivity(i);
    }
}
