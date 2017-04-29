package com.lovemehta.www.fingerprinting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class generate_fingerprint extends AppCompatActivity {

    EditText locafield;
    Button take, save;

    int CAMERA_REQUEST = 1888;
    ImageView imageView;

    Bitmap photo = null;
    String loca = null;

    ArrayList<String> completeStuff = new ArrayList<String>();
    ArrayList<String> ssidList = new ArrayList<String>();
    ArrayList<String> bssidList= new ArrayList<String>();
    ArrayList<String> cap= new ArrayList<String>();
    ArrayList<String> level= new ArrayList<String>();
    ArrayList<String> freq= new ArrayList<String>();
    ArrayList<String> timestmp= new ArrayList<String>();
    ArrayList<String> dist= new ArrayList<String>();
    ArrayList<String> distSd= new ArrayList<String>();

    ArrayList<String> lmark= new ArrayList<String>();


    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();
    AlertDialog dialogBox = null;

    TextView takePic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_fingerprint);
        takePic = (TextView) findViewById(R.id.heading2);

        take = (Button) findViewById(R.id.take);
        this.imageView = (ImageView)this.findViewById(R.id.picture);

        save = (Button) findViewById(R.id.save);
        locafield = (EditText)findViewById(R.id.location);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, completeStuff);
        mainWifi = (WifiManager) getSystemService(generate_fingerprint.this.WIFI_SERVICE);

        if (mainWifi.isWifiEnabled() == false)
        {
            Toast.makeText(getApplicationContext(), "Enabling Wi-Fi",
                    Toast.LENGTH_LONG).show();

            mainWifi.setWifiEnabled(true);
        }

        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();


        Toast.makeText(getApplicationContext(), "Please wait", Toast.LENGTH_SHORT).show();

        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                takePic.setText("Click on Save Fingerprint to save The landmark");

            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainWifi.startScan();

                loca =  locafield.getText().toString();

                lmark.add(0, loca);

                String stringData=null, imageData=null;

                String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                File dir = new File(root+"/netapp");
                dir.mkdirs();

                stringData = loca+".data";
                final File stringFile = new File(dir, stringData);

                imageData = loca+".jpg";
                final File imageFile = new File(dir, imageData);


                //Toast.makeText(getApplicationContext(), stringData, Toast.LENGTH_SHORT).show();

                if(stringFile.exists())
                {
                    dialogBox = new AlertDialog.Builder(generate_fingerprint.this)
                            .setTitle("Replace Entry")
                            .setMessage("An entry with the name " + loca + " already exists. Are you sure you want to replace this entry?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                    stringFile.delete();

                                    try {
                                        FileOutputStream out = new FileOutputStream(stringFile);
                                        ObjectOutputStream os = new ObjectOutputStream(out);
                                        os.writeObject(lmark);

                                        out = new FileOutputStream(imageFile);

                                        if (photo != null)
                                            photo.compress(Bitmap.CompressFormat.JPEG, 90, out);

                                        os.close();
                                        out.flush();
                                        out.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    Intent intent = new Intent();
                                    intent.putExtra("RESULT_STRING", loca);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .show();
                }
                else
                {
                    try {
                        FileOutputStream out = new FileOutputStream(stringFile);
                        ObjectOutputStream os = new ObjectOutputStream(out);
                        os.writeObject(lmark);

                        out = new FileOutputStream(imageFile);

                        if(photo != null)
                            photo.compress(Bitmap.CompressFormat.JPEG, 90, out);

                        os.close();
                        out.flush();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Intent intent=new Intent();
                    intent.putExtra("RESULT_STRING", loca);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        try {
            if (dialogBox != null && dialogBox.isShowing()) {
                dialogBox.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
    }


    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {

            wifiList = mainWifi.getScanResults();

            lmark.clear();

            for(int i = 0; i < wifiList.size(); i++){
                lmark.add(wifiList.get(i).SSID.toString());
                lmark.add(wifiList.get(i).BSSID.toString());
                lmark.add(String.valueOf(wifiList.get(i).level));
            }

        }

    }
}
