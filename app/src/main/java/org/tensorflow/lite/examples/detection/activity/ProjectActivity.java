package org.tensorflow.lite.examples.detection.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.tensorflow.lite.examples.detection.helpers.Pair;
import org.tensorflow.lite.examples.detection.R;
import org.tensorflow.lite.examples.detection.db.ShooterRangesDB;
import org.tensorflow.lite.examples.detection.helpers.SingleRange;

public class ProjectActivity extends AppCompatActivity {
    public final static int REQUEST_CODE_FOR_CAMERA = 1;
    public static double maxDistance = 0;
    public static int bonus = 0;
    private static String weapon;
    public static Pair<Float,Float> singleRangeResult = new Pair<Float, Float>(0.0f,0.0f);

    ShooterRangesDB shooterRangesDB;
    TextView head;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_project);
        listView = (ListView) findViewById(R.id.listView);
        head = (TextView) findViewById(R.id.projectNameHead);
        weapon = getIntent().getStringExtra("WEAPON");
        shooterRangesDB = new ShooterRangesDB(ProjectActivity.this, getIntent().getStringExtra("NAME"));
        String t ="Project : " + getIntent().getStringExtra("NAME");
        head.setText(t);
        updateList();
    }

    public void addRange(View view) {
        Intent intent = new Intent(this, CameraMainActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FOR_CAMERA);
    }

    private void updateList() {
        //insert to viewlist
        ArrayAdapter aa = new ArrayAdapter<>(this,
                R.layout.list_view_text,
                shooterRangesDB.getAllRanges());
        listView.setAdapter(aa);
        listView.bringToFront();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_FOR_CAMERA)
        {
            SingleRange sg = new SingleRange();
            if(shooterRangesDB.addRange(sg))
                updateList();
        }
    }

    public static String getWeapon()
    {
        return weapon;
    }
}