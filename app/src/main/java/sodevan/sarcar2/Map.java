package sodevan.sarcar2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class Map extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Algo a=null;
        boolean b=a.CollisionChecker(51.5031730,-0.1265895,51.5032131,-0.1256239,51.5032264,-0.1274478,51.5031329,-0.1241648);
        Log.i("Value: ",""+b);
    }
}
