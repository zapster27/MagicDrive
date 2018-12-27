package com.tuta.zapstertech.magicdrive;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;

public class SelectActivity extends Activity implements TextToSpeech.OnInitListener,View.OnClickListener {
    TextToSpeech mTTS = null;
    private final int ACT_CHECK_TTS_DATA = 1000;
    private RelativeLayout container;
    private int currentX;
    static String IPAddress="192.168.43.233";
    String message;
    private int currentY;
    final static String[] locs={"complab","profQs","lecQs","office","confRoom"};
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;
    int ind;
    private ImageButton compLab,depOffice,lecRooms,profRooms,confRoom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fadein, R.anim.fadeout);
        setContentView(R.layout.activity_select);
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        compLab=findViewById(R.id.ComputerLab);
        compLab.setImageResource(R.mipmap.notselected);
        compLab.setOnClickListener(SelectActivity.this);

        depOffice=findViewById(R.id.deptOffice);
        depOffice.setImageResource(R.mipmap.notselected);
        depOffice.setOnClickListener(SelectActivity.this);
        lecRooms=findViewById(R.id.lecQuaters);
        lecRooms.setImageResource(R.mipmap.notselected);
        lecRooms.setOnClickListener(SelectActivity.this);
        profRooms=findViewById(R.id.profQuaters);
        profRooms.setImageResource(R.mipmap.notselected);
        profRooms.setOnClickListener(SelectActivity.this);
        confRoom=findViewById(R.id.ConferenceRoom);
        confRoom.setImageResource(R.mipmap.notselected);
        confRoom.setOnClickListener(SelectActivity.this);

        // Check to see if we have TTS voice data
        Intent ttsIntent = new Intent();
        ttsIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(ttsIntent, ACT_CHECK_TTS_DATA);

        container = findViewById(R.id.container);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                currentX = (int) event.getRawX();
                currentY = (int) event.getRawY();
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                int x2 = (int) event.getRawX();
                int y2 = (int) event.getRawY();
                container.scrollBy(currentX - x2 , currentY - y2);
                currentX = x2;
                currentY = y2;
                break;
            }
            case MotionEvent.ACTION_UP: {
                break;
            }
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            mScaleFactor = Math.max(0.1f,
                    Math.min(mScaleFactor, 10.0f));
            container.setScaleX(mScaleFactor);
            container.setScaleY(mScaleFactor);
            return true;
        }
    }

    private void saySomething(String text, int qmode) {
        if(mTTS.isSpeaking()){
            mTTS.stop();
        }
        if (qmode == 1)
            mTTS.speak(text, TextToSpeech.QUEUE_ADD, null);
        else
            mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == ACT_CHECK_TTS_DATA) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // Data exists, so we instantiate the TTS engine
                mTTS = new TextToSpeech(this, this);
            } else {
                // Data is missing, so we start the TTS
                // installation process
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            if (mTTS != null) {
                int result = mTTS.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "TTS language is not supported", Toast.LENGTH_LONG).show();
                } else {
                    saySomething("Welcome!!",1);//ettext.getText().toString().trim(), 1);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // yourMethod();
                            saySomething("PLease select your destination",1);
                        }
                    }, 1000);
                }
            }
        } else {
            Toast.makeText(this, "TTS initialization failed",
                    Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.ComputerLab:
                if(ind!=0) {
                    confirmDestination("Computer Lab");
                    compLab.setImageResource(R.mipmap.select);
                    depOffice.setImageResource(R.mipmap.notselected);
                    lecRooms.setImageResource(R.mipmap.notselected);
                    profRooms.setImageResource(R.mipmap.notselected);
                    confRoom.setImageResource(R.mipmap.notselected);
                    LocationUpdate(ind);
                }
                break;
            case R.id.deptOffice:
                if(ind!=3) {
                    confirmDestination("Department Office");
                    depOffice.setImageResource(R.mipmap.select);
                    compLab.setImageResource(R.mipmap.notselected);
                    lecRooms.setImageResource(R.mipmap.notselected);
                    profRooms.setImageResource(R.mipmap.notselected);
                    confRoom.setImageResource(R.mipmap.notselected);
                    LocationUpdate(ind);
                }
                break;

            case R.id.lecQuaters:
                if(ind!=2) {
                    confirmDestination("Lecturer Rooms");
                    lecRooms.setImageResource(R.mipmap.select);
                    depOffice.setImageResource(R.mipmap.notselected);
                    compLab.setImageResource(R.mipmap.notselected);
                    profRooms.setImageResource(R.mipmap.notselected);
                    confRoom.setImageResource(R.mipmap.notselected);
                    LocationUpdate(ind);
                }
                break;

            case R.id.profQuaters:
                if(ind!=1) {
                    confirmDestination("Professor's Rooms");
                    profRooms.setImageResource(R.mipmap.select);
                    depOffice.setImageResource(R.mipmap.notselected);
                    lecRooms.setImageResource(R.mipmap.notselected);
                    compLab.setImageResource(R.mipmap.notselected);
                    confRoom.setImageResource(R.mipmap.notselected);
                    LocationUpdate(ind);
                }
                break;

            case R.id.ConferenceRoom:
                if(ind!=4) {
                    confirmDestination("Head of department office and Conference room");
                    confRoom.setImageResource(R.mipmap.select);
                    depOffice.setImageResource(R.mipmap.notselected);
                    lecRooms.setImageResource(R.mipmap.notselected);
                    profRooms.setImageResource(R.mipmap.notselected);
                    compLab.setImageResource(R.mipmap.notselected);
                    LocationUpdate(ind);
                }
                break;
        }
    }

    private void confirmDestination(final String Destination){
        AlertDialog d= new AlertDialog.Builder(SelectActivity.this)
                .setTitle("Confirm Destination")
                .setMessage(Destination)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saySomething("you will arrive at your destination shortly",1);
                        message=Destination;
                        send s=new send();
                        s.execute();
                    }
                }) //Set to null. We override the onclick
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

                String say="Are you sure you want to go to "+ Destination;
                saySomething(say,1);
            }
        });
        d.show();

    }

    private void LocationUpdate(int ind){
        switch(ind){
            case 0:
                compLab.setImageResource(R.mipmap.now);
                break;
            case 1:
                profRooms.setImageResource(R.mipmap.now);
                break;
            case 2:
                lecRooms.setImageResource(R.mipmap.now);
                break;
            case 3:
                depOffice.setImageResource(R.mipmap.now);
                break;
            case 4:
                confRoom.setImageResource(R.mipmap.now);
                break;
        }
    }

    private class send extends AsyncTask<Void,Void,Void> {
        Socket s;
        PrintWriter pw;
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(SelectActivity.this, "",
                    "Loading. Please wait...", true);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                s=new Socket(IPAddress,8000);
                pw=new PrintWriter(s.getOutputStream());
                pw.write(message);
                pw.flush();
                pw.close();
                s.close();

            }catch(UnknownHostException e){
                System.out.println("Fail");
                e.printStackTrace();
            }catch(IOException e){
                System.out.println("Fail");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        super.onDestroy();
    }
}
