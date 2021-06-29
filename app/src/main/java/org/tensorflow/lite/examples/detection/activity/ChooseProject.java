package org.tensorflow.lite.examples.detection.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.tensorflow.lite.examples.detection.R;

public class ChooseProject extends MainActivity {

    EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_project);
        editText = (EditText) findViewById(R.id.projectName);
    }

    public void letsStart(View view)
    {
        String projectName = editText.getText().toString();
        if(projectName.equals("")) {
            Toast.makeText(this, "Project Name can't be empty!", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, SelectWeapon.class);
        intent.putExtra("NAME", projectName);
        startActivity(intent);
    }
}