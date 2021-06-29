package org.tensorflow.lite.examples.detection.activity;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import org.tensorflow.lite.examples.detection.R;

public class MainActivity extends AppCompatActivity {

    MediaPlayer bgSong;
    ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        imageButton = (ImageButton) findViewById(R.id.stopMusic);
        bgSong = MediaPlayer.create(MainActivity.this, R.raw.music);
        bgSong.start();
    }

    public void startTutorial(View view)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=1iW0tExwuuc"));
        startActivity(intent);
    }

    public void aboutUsFunction(View view) {
        Intent intent = new Intent(this, aboutUs.class);
        startActivity(intent);
    }

    public void letsStart(View view)
    {
        Intent intent = new Intent(this, ChooseProject.class);
        startActivity(intent);
    }

    public void handleMusic(View view)
    {
        if (bgSong.isPlaying()) {
            bgSong.pause();
            imageButton.setBackgroundResource(R.drawable.ic_music_on);
        }
        else {
            bgSong.start();
            imageButton.setBackgroundResource(R.drawable.ic_music_off);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        bgSong.stop();
    }
}