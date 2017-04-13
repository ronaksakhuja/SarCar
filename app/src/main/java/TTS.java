import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

import sodevan.sarcar2.R;

/**
 * Created by kartiksharma on 13/04/17.
 */

public class TTS {
    TextToSpeech t1;
    EditText ed1;
    Button b1;
    int flag=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ed1=(EditText)findViewById(R.id.et1);
        b1=(Button)findViewById(R.id.btn1);
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag==0){
                    func();
                    flag++ ;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            flag=0 ;

                        }
                    },5000) ;
                }

                else{

                }


//                if(t1.isSpeaking()==true){
//                    flag++;
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    Log.d("TAG",flag+"");
//                }
            }
        });


    }
    public void onPause(){
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }
    public void func()
    {
        String toSpeak = ed1.getText().toString();
        Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();

        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
    }
}
