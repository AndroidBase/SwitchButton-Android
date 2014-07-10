package com.custom.switchbtn;

import android.os.Bundle;
import android.app.Activity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class MainActivity extends Activity implements CheckBox.OnCheckedChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((SwitchButton) findViewById(R.id.id_switch_btn)).setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Toast.makeText(MainActivity.this, isChecked ? "开启" : "关闭", Toast.LENGTH_SHORT).show();
    }

}
