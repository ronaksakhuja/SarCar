package sodevan.sarcar2;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import sodevan.sarcar2.Service.CarObject;

public class Map extends AppCompatActivity implements OnMapReadyCallback {
    final Context context = this;
    GoogleMap gmap ;
    Marker marker;
    private int flag = 0 , flagds =0 , ovspd=0 ;
    HashMap<String,LatLng> NearbyVehichles ;
    HashMap<String,LatLng> Nearbyaam ;

    HashMap<String , Marker> markersred ;
    HashMap<String , Marker> ambulance ;
    TextView loc_road  ,  speedtv;

    Firebase_datalayer fb=new Firebase_datalayer();
    Location mLocation ;

    String road="";
    String prev_road="";
    double prev_lat=0,prev_long=0;
    HashMap< String, CarObject> ac  ,aam;

    TextToSpeech t1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        speedtv = (TextView)findViewById(R.id.speedtv) ;

        speedtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overspeed();
            }
        });
        loc_road = (TextView)findViewById(R.id.loc_road) ;

        ac=new HashMap<>() ;
        aam = new HashMap<>();

        markersred = new HashMap<>() ;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("Roads");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()) ;
        final String uname=sp.getString("vehicleno","0000");
        final FusedLocation fusedLocation = new FusedLocation(context, new FusedLocation.Callback(){
            @Override
            public void onLocationResult(Location location){
                //Do as you wish with location here
                mLocation = location ;

                 Float speed =  location.getSpeed();
                 speed =  (float)Math.round(speed*100)/100 ;
                speedtv.setText(speed+" m/s");

                Log.d("TAG","loc:"+location.getLatitude()+" long : "+location.getLongitude());


                Double latitude = location.getLatitude() ;
                Double longitude = location.getLongitude() ;

                if (latitude!=null || longitude!=null){
                    if(!road.equals("")){

                        loc_road.setText(road);
                     prev_road=road;}
                    road=fb.getRoad(latitude,longitude);
                    myRef.child(prev_road).child(uname).removeValue();

                    CarObject co = new CarObject(location.getLatitude()+"", location.getLongitude()+"" , prev_lat+"" , prev_long+"") ;

                    myRef.child(road).child(uname).setValue(co) ;
                    prev_lat=latitude;
                    prev_long=longitude;
                    updateloc(new LatLng(location.getLatitude() , location.getLongitude()));
                }


            }
        });
        TimerTask t=new TimerTask() {
            @Override
            public void run() {
                fusedLocation.getCurrentLocation(3);

            }
        };

        Timer time=new Timer();
        time.schedule(t,0,5000);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {




                for (DataSnapshot postsnap :dataSnapshot.getChildren()){
                    if (postsnap.getKey().equals("ronak")||postsnap.getKey().equals("no road")){
                        Log.i("Tag" , "ignored") ;
                    }


                    else {
                        for (DataSnapshot postpostsnap : postsnap.getChildren()){
                            Log.i("car" , postpostsnap+"") ;
                            if (postpostsnap.getKey().equals(uname)){
                                Log.i("tag" , "Your car") ;
                            }

                            else if (postpostsnap.getKey().equals("Ambulance")){
                                CarObject obj = postpostsnap.getValue(CarObject.class) ;
                                aam.put(postpostsnap.getKey() , obj) ;
                            }

                            else {
                                CarObject obj = postpostsnap.getValue(CarObject.class) ;
                                ac.put(postpostsnap.getKey() , obj) ;


                            }
                        }
                    }
                }

                Log.i("final :" , ac+"") ;
                checkCollision(ac , "n");
                checkCollision(aam , "aam");


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }





    public void animateMarker (final Marker marker, final LatLng topostion , final boolean hideMarker) {
        final Handler handler = new Handler() ;

        final long start = SystemClock.uptimeMillis() ;
        Projection projection = gmap.getProjection() ;

        // Getting current Marker Location
        Point startpoint = projection.toScreenLocation(marker.getPosition()) ;
        final LatLng startLatlong = projection.fromScreenLocation(startpoint) ;
        final long duration = 500 ;

        final LinearInterpolator interpolator = new LinearInterpolator() ;

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis()-start ;

                float t = interpolator.getInterpolation((float)elapsed/duration) ;
                double lng = t * topostion.longitude + (1 - t)
                        * startLatlong.longitude;
                double lat = t * topostion.latitude + (1 - t)
                        * startLatlong.latitude;

                marker.setPosition(new LatLng(lat , lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }

                else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }

                }



            }
        });

    }



    public void updateloc(LatLng loc) {
        Bitmap bm = BitmapFactory.decodeResource(getResources() , R.drawable.car) ;
        Bitmap im = Bitmap.createScaledBitmap(bm , 80 , 179 , false) ;
        MarkerOptions markerop=  new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromBitmap(im)) ;

        if (flag==0 && gmap!=null) {
            marker = gmap.addMarker(markerop) ;
            gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 19.0f));
            flag=1 ;
        }

        else if (marker!=null){
            animateMarker(marker , loc , false);
            gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 19.0f));

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gmap=googleMap ;

    }




    public void checkCollision(HashMap<String , CarObject> co , String type)    {

        if (mLocation!=null) {



                Log.i("typ" , type) ;

                if (type.equals("n"))
                NearbyVehichles = new HashMap<>();

                for (HashMap.Entry<String , CarObject> entry : co.entrySet()) {

                    String key = entry.getKey();
                    CarObject obj = entry.getValue();


                    LatLng target = new LatLng(Double.parseDouble(obj.getLat()), Double.parseDouble(obj.getLon()));
                    LatLng prevtarget = new LatLng(Double.parseDouble(obj.getPrev_lat()), Double.parseDouble(obj.getPrev_lon()));
                    LatLng my = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                    LatLng prev_me = new LatLng(prev_lat, prev_long);

                    boolean b = Algo.CollisionChecker(my.latitude, my.longitude, target.latitude, target.longitude, prev_me.latitude, prev_me.longitude, prevtarget.latitude, prevtarget.longitude);

                    if (b) {
                        NearbyVehichles.put( key, target);
                    }
                }

                Log.i("tomap" , NearbyVehichles+"");
                MapNearbyVehichles();

        }


    }


    public  void addRedMarker( LatLng dangercar) {

        Bitmap bm = BitmapFactory.decodeResource(getResources() , R.drawable.redcar) ;
        Bitmap im = Bitmap.createScaledBitmap(bm , 80 , 179 , false) ;
        MarkerOptions markerop=  new MarkerOptions().position(dangercar).icon(BitmapDescriptorFactory.fromBitmap(im)) ;
        gmap.addMarker(markerop) ;
    }


    private void MapNearbyVehichles() {
        HashMap<String , Marker> tempred =   new HashMap<>();
        Set<String> keys = NearbyVehichles.keySet() ;

        for(String id : keys ){

            Log.d("red" , id) ;

            LatLng ns = NearbyVehichles.get(id)  ;
            Marker m    = markersred.get(id) ;

            if (m==null) {
                Bitmap bm=null;

                if (id.equals("Ambulance"))
                {
                    bm = BitmapFactory.decodeResource(getResources(), R.drawable.ambulance);

                }

                else   {

                    Log.e("Hello" , id) ;

                    bm = BitmapFactory.decodeResource(getResources(), R.drawable.redcar);
                }
                Bitmap im = Bitmap.createScaledBitmap(bm, 80, 179, false);
                MarkerOptions markerop = new MarkerOptions().position(ns).icon(BitmapDescriptorFactory.fromBitmap(im));
                Marker m1 = gmap.addMarker(markerop) ;
                tempred.put(id , m1) ;


            }


            else  {
                animateMarker(m ,   ns, false );
                tempred.put(id,m) ;
                markersred.remove(id) ;
                Log.e("hello2" , "id") ;

            }

            if (id.equals("Ambulance")){
                TxT("Their is an Ambulance in your path , Kindly Switch Lane");
            }

            else {
                TxT("Collision Prediction");

            }

        }


        if (markersred!=null) {
            Set<String> keys2 = markersred.keySet();

            for (String id : keys2) {

                Marker m = markersred.get(id);
                removemarker(m);

                Log.i("yay" ,"waht");
            }




        }

        markersred = tempred ;




    }


    public void removemarker ( Marker marker) {
        marker.remove();
    }

    public void TxT(String str) {


                if(flagds==0){
                    func(str);
                    flagds++ ;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            flagds=0 ;

                        }
                    },10000) ;
                }

                else{

                }
            }


    public void func(String str)
    {

        t1.speak(str, TextToSpeech.QUEUE_FLUSH, null);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    public void overspeed() {

        if(ovspd<=5){

            ovspd++ ;
        }


        else {
            ovspd=0;
            Intent nes = new Intent(this, ChallanPrompt.class);
            startActivity(nes);

        }


    }

}