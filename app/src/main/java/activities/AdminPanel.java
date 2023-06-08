package activities;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.example.capstoneproject_1.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class AdminPanel extends AppCompatActivity {
    private Handler handler;
    private Runnable runnable;
    private static final long INTERVAL = 10000; // 10 seconds

    PieChart pieChart;
    Segment s1;
    Segment s2;

    TextView txtCurValElGrid;
    TextView txtCurValSolPanel;
    Button btnSignOut;
    Button btnResetRatios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);
        txtCurValElGrid = findViewById(R.id.txtElGridCurrent);
        txtCurValSolPanel = findViewById(R.id.txtSolPanelCurrent);
        btnSignOut = findViewById(R.id.btnAdminSignOut);
        btnResetRatios = findViewById(R.id.btnResetRatios);

        fetchCurrentValues(); //it provides to draw the chart initially and it will be called by each time interval with the Handler structure below.

        //Calls the method to fetch the current values by each specified interval value;
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // Call your method here to perform the desired task
                fetchCurrentValues();
                // Schedule the next execution after the specified interval
                handler.postDelayed(this, INTERVAL);
            }
        };

        //=>Since the admin values kept at Realtime Db, the admin user is directly redirected to the Login Page with sign out operation;
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(AdminPanel.this, LoginPage.class);
                startActivity(i);
            }
        });

        //=>In order to reset the electrical current ratios which are taken from the electrical sources as zero on the Realtime Database
        //and redrawing the chart by fetching the updated values again;
        btnResetRatios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference curSources = FirebaseDatabase.getInstance().getReference().child("currentSources");
                curSources.child("electricalGridRatio").setValue(0);
                curSources.child("solarPanelRatio").setValue(0);
                fetchCurrentValues(); //fetch the values and redraw the chart again to fetch the 0 values
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start the periodic execution when the activity is resumed
        handler.postDelayed(runnable, INTERVAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop the periodic execution when the activity is paused
        handler.removeCallbacks(runnable);
    }

    public void drawChart(double elGridRatio, double solPanelRatio){
        //Since the chart is not drawn if their values are NaN(not a number), their value is assigned as default
        if(Double.isNaN(elGridRatio) && Double.isNaN(solPanelRatio)){
            elGridRatio = 50.0;
            solPanelRatio = 50.0;
        }

        ArrayList<Double> currentAry = new ArrayList<>();
        currentAry.add(Double.parseDouble(String.format("%.2f", elGridRatio)));
        currentAry.add(Double.parseDouble(String.format("%.2f", solPanelRatio)));

        pieChart = findViewById(R.id.pieChart);
        pieChart.clear(); //clear the piechart before it is redrawn again by calling it with the fetched values with the handler by each time interval.
        s1 = new Segment("Solar " + currentAry.get(1)+ "%", currentAry.get(1));
        s2 = new Segment("Grid " + currentAry.get(0)+ "%", currentAry.get(0));
        SegmentFormatter sgf1 = new SegmentFormatter(Color.BLUE);
        sgf1.getLabelPaint().setTextSize(40);
        SegmentFormatter sgf2 = new SegmentFormatter(Color.RED);
        sgf2.getLabelPaint().setTextSize(40);
        pieChart.addSegment(s1,sgf1);
        pieChart.addSegment(s2,sgf2);
        pieChart.redraw();
    }

    //In order to fetch the current(Amper) values from the electrical sources;
    private void fetchCurrentValues(){
        DatabaseReference currentRef = FirebaseDatabase.getInstance().getReference().child("currentSources");
        currentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double elGridCurrentValue = snapshot.child("egCurrentValue").getValue(Double.class);
                double spCurrentValue = snapshot.child("spCurrentValue").getValue(Double.class);
                double egRatio = snapshot.child("electricalGridRatio").getValue(Double.class);
                double spRatio = snapshot.child("solarPanelRatio").getValue(Double.class);
                double egPerRatio = (egRatio/(egRatio+spRatio)) * 100;
                double spPerRatio = (spRatio/(egRatio+spRatio)) * 100;
                txtCurValElGrid.setText(String.format("%.2f", elGridCurrentValue) + " A");
                txtCurValSolPanel.setText(String.format("%.2f", spCurrentValue) + " A");
                drawChart(egPerRatio,spPerRatio);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}
