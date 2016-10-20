package ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onRESTButtonClick(View view) {
        // TODO: Open REST Activity
    }

    public void onWSButtonClick(View view) {
        Intent restIntent = new Intent(this, SOAPActivity.class);
        this.startActivity(restIntent);
    }

    public void onServerButtonClick(View view) {
        // TODO: Open Server Activity
    }
}
