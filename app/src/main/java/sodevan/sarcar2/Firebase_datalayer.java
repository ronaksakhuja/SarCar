package sodevan.sarcar2;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import sodevan.sarcar2.Models.NearbyStreets;
import sodevan.sarcar2.Service.NSservice;

/**
 * Created by ronaksakhuja on 13/04/17.
 */

public class Firebase_datalayer {
    String uname;
    Firebase_datalayer(String uname){
        this.uname=uname;
    }

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://api.geonames.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    NSservice apiservice = retrofit.create(NSservice.class);
    String res=null;

    FirebaseDatabase database = FirebaseDatabase.getInstance();


    public String getRoad(double lat,double lon){
        Call<NearbyStreets> call=apiservice.getstreets(String.valueOf(lat),String.valueOf(lon),"kartik_sharma");
        call.enqueue(new Callback<NearbyStreets>() {
            @Override
            public void onResponse(Call<NearbyStreets> call, Response<NearbyStreets> response) {
                Log.d("TAG",response.body().getStreetSegment().get(0).getName());
                res= response.body().getStreetSegment().get(0).getName();

            }

            @Override
            public void onFailure(Call<NearbyStreets> call, Throwable t) {

            }
        });
        return res;
    }
    public void savelocation(double lat,double lon){
        String road=getRoad(lat,lon);
        DatabaseReference myRef = database.getReference(road);
        myRef.child(road).child(uname).child("lat").setValue(lat);
        myRef.child(road).child(uname).child("lon").setValue(lon);

    }
}
