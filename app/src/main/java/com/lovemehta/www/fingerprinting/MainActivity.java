package com.lovemehta.www.fingerprinting;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    ArrayList<String> allLocations = new ArrayList<String>();
    ArrayList<String> completeStuff = new ArrayList<String>();
    ArrayList<String> ssidList = new ArrayList<String>();
    ArrayList<String> bssidList= new ArrayList<String>();
    ArrayList<String> cap= new ArrayList<String>();
    ArrayList<String> level= new ArrayList<String>();
    ArrayList<String> freq= new ArrayList<String>();
    ArrayList<String> timestmp= new ArrayList<String>();
    ArrayList<String> dist= new ArrayList<String>();
    ArrayList<String> distSd= new ArrayList<String>();

    TextView mainText, recentlyAdded;
    WifiManager mainWifi;
    //WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();
    Button addNewEntry, refresh;
    ListView listView ;

    private static final int CAMERA_REQUEST = 1888, ENTRY_REQUEST = 1999;
    private ImageView imageView;
    private String entry;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        listView = (ListView) findViewById(R.id.list);

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, allLocations){

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(android.R.id.text1);

            /*YOUR CHOICE OF COLOR*/
                textView.setTextColor(Color.WHITE);

                return view;
            }
        };;


        mainText = (TextView) findViewById(R.id.mainText);
        recentlyAdded = (TextView) findViewById(R.id.heading1);

        addNewEntry = (Button) findViewById(R.id.button);

        refresh = (Button) findViewById(R.id.button2);


        this.imageView = (ImageView)this.findViewById(R.id.imageView);


        addNewEntry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), generate_fingerprint.class);
                startActivityForResult(intent, ENTRY_REQUEST);

                //new FileInputStream(new File(new File(getFilesDir(),"")+File.separator+filename)


            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainText.setText("All Landmarks");
                String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                File folder = new File(root+"/netapp/");
                File[] listOfFiles = folder.listFiles();

                allLocations.clear();

                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isFile()) {
                        String fname = listOfFiles[i].getName();
                        String[] piece = fname.split("\\.");

                        if(piece[piece.length-1].equalsIgnoreCase("data"))
                           allLocations.add(piece[0]);

                        //Toast.makeText(getApplicationContext(), piece[0], Toast.LENGTH_SHORT).show();
                    }
                }

                listView.setAdapter(adapter);
                //new FileInputStream(new File(new File(getFilesDir(),"")+File.separator+filename)


            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                String clicked=(String)arg0.getItemAtPosition(arg2);

                mainText.setText("Wifi data: "+clicked);
                recentlyAdded.setText("Image data: "+clicked);

                try {
                    String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                    FileInputStream in = new FileInputStream(root+"/netapp/"+clicked+".data");
                    ObjectInputStream input = new ObjectInputStream(in);
                    ArrayList<String> obj = (ArrayList<String>) input.readObject();

                    allLocations.clear();

                    for (int i = 1; i < obj.size(); i=i+3) {

                        String temp = "SSID: "+obj.get(i)+", Level: "+obj.get(i+2);

                        allLocations.add(temp);

                            //Toast.makeText(getApplicationContext(), piece[0], Toast.LENGTH_SHORT).show(;
                    }

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(root+"/netapp/"+clicked+".jpg", options);
                    imageView.setImageBitmap(bitmap);

                    listView.setAdapter(adapter);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
        else if(requestCode == ENTRY_REQUEST && resultCode == RESULT_OK) {
                entry = data.getStringExtra("RESULT_STRING");

            try {
                    String root = Environment.getExternalStorageDirectory().getAbsolutePath();
                    FileInputStream in = new FileInputStream(root+"/netapp/"+entry+".data");
                    ObjectInputStream input = new ObjectInputStream(in);
                    ArrayList<String> obj = (ArrayList<String>) input.readObject();

                    Toast.makeText(getApplicationContext(), "Fingerprint '"+entry+"' saved", Toast.LENGTH_SHORT).show();

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap bitmap = BitmapFactory.decodeFile(root+"/netapp/"+entry+".jpg", options);
                    mainText.setText("Recent - "+entry);
                    imageView.setImageBitmap(bitmap);
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        //menu.add(0, 0, 0, "Refresh");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        //mainWifi.startScan();
        //mainText.setText("Starting Scan");
        return super.onMenuItemSelected(featureId, item);
    }

    protected void onPause() {
        //unregisterReceiver(receiverWifi);
        super.onPause();
    }

    protected void onResume() {
        //registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

}



