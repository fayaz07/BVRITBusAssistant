package fz.bvritbusassistant;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    Spinner spinner;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            startActivity(new Intent(MainActivity.this, StartService.class));
            finish();
        }

        spinner = (Spinner) findViewById(R.id.spinner3);
        button = (Button) findViewById(R.id.continueB);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String i = String.valueOf(spinner.getSelectedItemId());
                Intent intent = new Intent(getApplicationContext(), PhoneVerification.class);
                intent.putExtra("i",i);
                startActivity(intent);
            }
        });

    }
}
