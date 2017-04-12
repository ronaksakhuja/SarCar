package sodevan.sarcar2;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class Map extends AppCompatActivity implements OnMapReadyCallback {
    final Context context = this;
    GoogleMap gmap ;
    Marker marker;
    private int flag = 0 , flag2 =0 ;
    HashMap<String,LatLng> NearbyVehichles ;
    HashMap<String , Marker> markersred ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);


        final FusedLocation fusedLocation = new FusedLocation(context, new FusedLocation.Callback(){
            @Override
            public void onLocationResult(Location location){
                //Do as you wish with location here
                Log.d("TAG","loc:"+location.getLatitude()+" long : "+location.getLongitude());
                updateloc(new LatLng(location.getLatitude() , location.getLongitude()));

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




    public void checkCollision(LatLng user , LatLng target , LatLng prevuser , LatLng prevtarget) {

       boolean b =  Algo.CollisionChecker(user.latitude ,user.longitude , target.latitude ,target.longitude , prevuser.latitude , prevuser.longitude , prevtarget.latitude , prevtarget.longitude) ;

        if(b){
            String carid ="hello" ;
            NearbyVehichles.put(carid , target ) ;
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

                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.redcar);
                Bitmap im = Bitmap.createScaledBitmap(bm, 80, 179, false);
                MarkerOptions markerop = new MarkerOptions().position(ns).icon(BitmapDescriptorFactory.fromBitmap(im));
                Marker m1 = gmap.addMarker(markerop) ;
                tempred.put(id , m1) ;


            }


            else  {
                animateMarker(m ,   ns, false );
                tempred.put(id,m) ;
                markersred.remove(id) ;
            }
        }


        if (markersred!=null) {
            Set<String> keys2 = markersred.keySet();

            for (String id : keys2) {

                Marker m = markersred.get(id);
                removemarker(m);
            }



        }

        markersred = tempred ;




    }


    public void removemarker ( Marker marker) {
        marker.remove();
    }





}