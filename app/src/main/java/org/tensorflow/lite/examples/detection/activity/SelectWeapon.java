package org.tensorflow.lite.examples.detection.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.tensorflow.lite.examples.detection.R;

public class SelectWeapon extends ChooseProject {

    private weaponSelected chosenWeapon;

    enum weaponSelected {
        M16ShortMafro,
        M4M5,
        M4Mafro,
        MicroM5,
        MicroMafro
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_weapon);
    }

    public void M16Clicked(View view)
    {
        chosenWeapon = weaponSelected.M16ShortMafro;
        letsStart(view);
    }

    public void M4Clicked(View view)
    {
        chosenWeapon = weaponSelected.M4M5;
        letsStart(view);
    }

    public void M4MafroClicked(View view)
    {
        chosenWeapon = weaponSelected.M4Mafro;
        letsStart(view);
    }

    public void MicroM5Clicked(View view)
    {
        chosenWeapon = weaponSelected.MicroM5;
        letsStart(view);
    }

    public void MicroMafroClicked(View view)
    {
        chosenWeapon = weaponSelected.MicroMafro;
        letsStart(view);
    }


    public void letsStart(View view)
    {
        Intent intent = new Intent(this, ProjectActivity.class);
        intent.putExtra("WEAPON", chosenWeapon.toString());
        intent.putExtra("NAME", getIntent().getStringExtra("NAME"));
        startActivity(intent);
    }
}