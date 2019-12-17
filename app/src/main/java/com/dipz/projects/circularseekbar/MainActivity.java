package com.dipz.projects.circularseekbar;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView seekBarArcProgress;
    private CircularSeekbar circularSeekbar;
    private ImageView ivOnOffButton;

    private ImageButton ibBtnBrightness, ibBtnColor;

    @Override
    protected void onStart() {
        super.onStart();
        seekBarArcProgress.setText(String.valueOf(circularSeekbar.getProgress()));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBarArcProgress = (TextView) findViewById(R.id.seekArcProgress);
        circularSeekbar = (CircularSeekbar) findViewById(R.id.circularSeekbar);
        ibBtnBrightness = (ImageButton) findViewById(R.id.ibBtnBrightness);
        ibBtnColor = (ImageButton) findViewById(R.id.ibBtnColor);
        ivOnOffButton = (ImageView) findViewById(R.id.ivOnOffButton);


        circularSeekbar.setOnSemiCircularSeekbarArcChangeListener(new CircularSeekbar.OnSemiCircularSeekbarArcChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekbar circularSeekbar, int progress, boolean fromUser) {
                seekBarArcProgress.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(CircularSeekbar circularSeekbar) {

            }

            @Override
            public void onStopTrackingTouch(CircularSeekbar circularSeekbar) {

            }
        });

        ibBtnBrightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circularSeekbar.setmIsBrightnessColor(false);
                seekBarArcProgress.setText("0");
                circularSeekbar.setProgress(0);
                circularSeekbar.invalidate();
            }
        });

        ibBtnColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circularSeekbar.setmIsBrightnessColor(true);
                seekBarArcProgress.setText("0");
                circularSeekbar.setProgress(0);
                circularSeekbar.invalidate();

            }
        });

        ivOnOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Clicked Light ON/OFF Button", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
