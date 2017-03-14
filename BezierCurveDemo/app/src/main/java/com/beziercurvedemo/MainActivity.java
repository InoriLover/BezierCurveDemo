package com.beziercurvedemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Created by Fishy on 2017/3/14.
 */

public class MainActivity extends AppCompatActivity {
    Button btnCancel, btnClear;
    ToggleButton btnSwitch;
    SeekBar pathWidthSeek, pointWidthSeek;
    BezierViewBoard bezierViewBoard;
    EditText editPathColor, editPointColor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //findview
        bezierViewBoard = (BezierViewBoard) findViewById(R.id.bezierView);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnClear = (Button) findViewById(R.id.btnClear);
        btnSwitch= (ToggleButton) findViewById(R.id.btnSwitch);
        pathWidthSeek = (SeekBar) findViewById(R.id.pathWidthSeek);
        pointWidthSeek = (SeekBar) findViewById(R.id.pointWidthSeek);
        editPathColor = (EditText) findViewById(R.id.editPathColor);
        editPointColor = (EditText) findViewById(R.id.editPointColor);
        pathWidthSeek.setProgress(2*100/15);
        pointWidthSeek.setProgress(5*100/25);
        //setListener
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bezierViewBoard.clearBoard();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bezierViewBoard.cancelForBack();
            }
        });
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnSwitch.isChecked()){
                    bezierViewBoard.setBezier_2(false);
                }else{
                    bezierViewBoard.setBezier_2(true);
                }
            }
        });
        pathWidthSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int size=15*progress/100;
                bezierViewBoard.setDefaultPathSize(size);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        pointWidthSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int size=25*progress/100;
                bezierViewBoard.setDefaultPointWidth(size);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        editPathColor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6) {
                    try {
                        int color = Integer.parseInt(s.toString(),16);
                        color=0xff000000|color;
                        bezierViewBoard.setDefaultPaintColor(color);
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "请输入合法的颜色值，如e5e5e5", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editPointColor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6) {
                    try {
                        int color = Integer.parseInt(s.toString(),16);
                        color=0xff000000|color;
                        bezierViewBoard.setDefaultPointColor(color);
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, "请输入合法的颜色值，如e5e5e5", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
