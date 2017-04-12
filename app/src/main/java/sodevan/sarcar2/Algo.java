package sodevan.sarcar2;

import android.util.Log;

/**
 * Created by GAGAN on 12-04-2017.
 */

    public class Algo {

    private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }
    public static boolean CollisionChecker(double lat1, double lon1, double lat2, double lon2,double prevlat1, double prevlon1, double prevlat2, double prevlon2 )
    {

       double d1=distance(prevlat1,prevlon1,prevlat2,prevlon2,"K");
       double d2=distance(lat1,lon1,lat2,lon2,"K");
       if(d1<d2)
       {
           Log.i("case1" , d1+"  "+d2) ;
           return false;
       }
       else
       {
          if(d2<=0.100)
          {
              Log.i("case2" , d1+"  "+d2) ;

              return true;
          }
          else
              {
              Log.i("case3" , d1+"  "+d2) ;

              return false;

          }
       }
    }
}
