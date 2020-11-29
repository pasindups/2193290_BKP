package com.kathri;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final int MESSAGE_STATE_CHANGE = 1;
    private String vehicleId = "MKH253";

    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    protected final static char[] dtcLetters = {'P', 'C', 'B', 'U'};
    protected final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private static final String[] PIDS = {
            "01", "02", "03", "04", "05", "06", "07", "08",
            "09", "0A", "0B", "0C", "0D", "0E", "0F", "10",
            "11", "12", "13", "14", "15", "16", "17", "18",
            "19", "1A", "1B", "1C", "1D", "1E", "1F", "20"};


    private static final int REQUEST_CONNECT_DEVICE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final float APPBAR_ELEVATION = 14f;
    private static boolean actionbar = true;
    final List<String> commandslist = new ArrayList<String>();
    ;
    final List<Double> avgconsumption = new ArrayList<Double>();
    final List<String> troubleCodesArray = new ArrayList<String>();
    MenuItem itemtemp;
    GaugeSpeed speed;
    GaugeRpm rpm;
    BluetoothDevice currentdevice;
    boolean commandmode = false, initialized = false, m_getPids = false, tryconnect = false, defaultStart = false;
    String devicename = null, deviceprotocol = null;

    String[] initializeCommands;
    Intent serverIntent = null;
    TroubleCodes troubleCodes;
    String VOLTAGE = "ATRV",
            PROTOCOL = "ATDP",
            RESET = "ATZ",
            ENGINE_COOLANT_TEMP = "0105",  //A-40
            ENGINE_RPM = "010C",  //((A*256)+B)/4
            ENGINE_LOAD = "0104",  // A*100/255
            VEHICLE_SPEED = "010D",  //A
            INTAKE_AIR_TEMP = "010F",  //A-40
            MAF_AIR_FLOW = "0110", //MAF air flow rate 0 - 655.35	grams/sec ((256*A)+B) / 100  [g/s]
            ENGINE_OIL_TEMP = "015C",  //A-40
            FUEL_RAIL_PRESSURE = "0122", // ((A*256)+B)*0.079
            INTAKE_MAN_PRESSURE = "010B", //Intake manifold absolute pressure 0 - 255 kPa
            CONT_MODULE_VOLT = "0142",  //((A*256)+B)/1000
            AMBIENT_AIR_TEMP = "0146",  //A-40
            CATALYST_TEMP_B1S1 = "013C",  //(((A*256)+B)/10)-40
            STATUS_DTC = "0101", //Status since DTC Cleared
            THROTTLE_POSITION = "0111", //Throttle position 0 -100 % A*100/255
            OBD_STANDARDS = "011C", //OBD standards this vehicle
            PIDS_SUPPORTED = "0120"; //PIDs supported
    Toolbar toolbar;
    AppBarLayout appbar;
    String trysend = null;
    private PowerManager.WakeLock wl;
    private Menu menu;
    private EditText mOutEditText;
    private Button mSendButton, mPidsButton, mTroublecodes, mClearTroublecodes, mClearlist;
    private ListView mConversationView;
    private TextView engineLoad, Fuel, voltage, coolantTemperature, Status, Loadtext, Volttext, Temptext, Centertext, Info, Airtemp_text, airTemperature, Maf_text, Maf;
    private String mConnectedDeviceName = "Ecu";
    private int rpmval = 0, intakeairtemp = 0, ambientairtemp = 0, coolantTemp = 0, mMaf = 0,
            engineoiltemp = 0, b1s1temp = 0, Enginetype = 0, FaceColor = 0,
            whichCommand = 0, m_dedectPids = 0, connectcount = 0, trycount = 0;
    private int mEnginedisplacement = 1500;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothService mBtService = null;

    StringBuilder inStream = new StringBuilder();
    private ArrayAdapter<String> mConversationArrayAdapter;


    private final Handler mBtHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:

                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:

                            Status.setText(getString(R.string.title_connected_to, mConnectedDeviceName));
                            Info.setText(R.string.title_connected);
                            try {
                                itemtemp = menu.findItem(R.id.menu_connect_bt);
                                itemtemp.setTitle(R.string.disconnectbt);
                                Info.setText(R.string.title_connected);
                            } catch (Exception e) {
                            }

                            tryconnect = false;
                            resetvalues();
                            sendEcuMessage(RESET);

                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Status.setText(R.string.title_connecting);
                            Info.setText(R.string.tryconnectbt);
                            break;
                        case BluetoothService.STATE_LISTEN:

                        case BluetoothService.STATE_NONE:

                            Status.setText(R.string.title_not_connected);
                            itemtemp = menu.findItem(R.id.menu_connect_bt);
                            itemtemp.setTitle(R.string.connectbt);
                            if (tryconnect) {
                                mBtService.connect(currentdevice);
                                connectcount++;
                                if (connectcount >= 2) {
                                    tryconnect = false;
                                }
                            }
                            resetvalues();

                            break;
                    }
                    break;
                case MESSAGE_WRITE:

                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);

                    if (commandmode || !initialized) {
                        mConversationArrayAdapter.add("Command:  " + writeMessage);
                    }

                    break;
                case MESSAGE_READ:

                    String tmpmsg = clearMsg(msg);

                    Info.setText(tmpmsg);

                    if (commandmode || !initialized) {
                        mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + tmpmsg);
                    }

                    analysMsg(msg);

                    break;
                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void removePID(String pid)
    {
        int index = commandslist.indexOf(pid);

        if (index != -1)
        {
            commandslist.remove(index);
            Info.setText("Removed pid: " + pid);
        }
    }

    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                     if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        sendEcuMessage(message);
                    }
                    return true;
                }
            };

    public static boolean isHexadecimal(String text) {
        text = text.trim();

        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B', 'C', 'D', 'E', 'F'};

        int hexDigitsCount = 0;

        for (char symbol : text.toCharArray()) {
            for (char hexDigit : hexDigits) {
                if (symbol == hexDigit) {
                    hexDigitsCount++;
                    break;
                }
            }
        }

        return true ? hexDigitsCount == text.length() : false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gauges);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        appbar = (AppBarLayout) findViewById(R.id.appbar);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl.acquire();

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Status = (TextView) findViewById(R.id.Status);
        engineLoad = (TextView) findViewById(R.id.Load);
        Fuel = (TextView) findViewById(R.id.Fuel);
        coolantTemperature = (TextView) findViewById(R.id.Temp);
        voltage = (TextView) findViewById(R.id.Volt);
        Loadtext = (TextView) findViewById(R.id.Load_text);
        Temptext = (TextView) findViewById(R.id.Temp_text);
        Volttext = (TextView) findViewById(R.id.Volt_text);
        Centertext = (TextView) findViewById(R.id.Center_text);
        Info = (TextView) findViewById(R.id.info);
        Airtemp_text = (TextView) findViewById(R.id.Airtemp_text);
        airTemperature = (TextView) findViewById(R.id.Airtemp);
        Maf_text = (TextView) findViewById(R.id.Maf_text);
        Maf = (TextView) findViewById(R.id.Maf);
        speed = (GaugeSpeed) findViewById(R.id.GaugeSpeed);
        rpm = (GaugeRpm) findViewById(R.id.GaugeRpm);



        troubleCodes = new TroubleCodes();

        invisiblecmd();


        initializeCommands = new String[]{"ATZ", "ATL0", "ATE1", "ATH1", "ATAT1", "ATSTFF", "ATI", "ATDP", "ATSP0", "ATSP0"};

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", Toast.LENGTH_LONG).show();
        }
        else
        {
            if (mBtService != null) {
                if (mBtService.getState() == BluetoothService.STATE_NONE) {
                    mBtService.start();
                }
            }
        }

         mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                 View view = super.getView(position, convertView, parent);
                 TextView tv = (TextView) view.findViewById(R.id.listText);
                    tv.setTextColor(Color.parseColor("#3ADF00"));
                tv.setTextSize(10);
                return view;
            }
        };

        mConversationView.setAdapter(mConversationArrayAdapter);

        mPidsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String sPIDs = "0100";
                m_getPids = false;
                sendEcuMessage(sPIDs);
            }
        });

        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = mOutEditText.getText().toString();
                sendEcuMessage(message);
            }
        });

        mClearTroublecodes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String clearCodes = "04";
                sendEcuMessage(clearCodes);
            }
        });

        mClearlist.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mConversationArrayAdapter.clear();
            }
        });

        mTroublecodes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String troubleCodes = "03";
                sendEcuMessage(troubleCodes);
            }
        });

        mOutEditText.setOnEditorActionListener(mWriteListener);

        RelativeLayout rlayout = (RelativeLayout) findViewById(R.id.mainscreen);
        rlayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    ActionBar actionBar = getSupportActionBar();
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) Status.getLayoutParams();
                    if (actionbar) {
                        actionBar.hide();
                        actionbar = false;

                        lp.setMargins(0, 5, 0, 0);
                    } else {
                        actionBar.show();
                        actionbar = true;
                        lp.setMargins(0, 0, 0, 0);
                    }

                    setgaugesize();
                    Status.setLayoutParams(lp);

                } catch (Exception e) {
                }
            }
        });

        getPreferences();

        resetgauges();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        this.menu = menu;

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_connect_bt:


                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    return false;
                }

                if (mBtService == null) setupChat();

                if (item.getTitle().equals("ConnectBT")) {
                    serverIntent = new Intent(this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                } else {
                    if (mBtService != null)
                    {
                        mBtService.stop();
                        item.setTitle(R.string.connectbt);
                    }
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
               if (resultCode == MainActivity.RESULT_OK) {
                    connectDevice(data);
                }
                break;

            case REQUEST_ENABLE_BT:

                if (mBtService == null) setupChat();

                if (resultCode == MainActivity.RESULT_OK) {
                    serverIntent = new Intent(this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                } else {
                    Toast.makeText(getApplicationContext(), "BT device not enabled", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setDefaultOrientation();
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        getPreferences();
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBtService != null) mBtService.stop();

        wl.release();
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferences();
        setDefaultOrientation();
        resetvalues();

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            if (!commandmode) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage("Are you sure you want exit?");
                alertDialogBuilder.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                exit();
                            }
                        });

                alertDialogBuilder.setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else {
                commandmode = false;
                invisiblecmd();
                sendEcuMessage(VOLTAGE);
            }

            return false;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (mBtService != null) mBtService.stop();
        wl.release();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void getPreferences() {

            SharedPreferences preferences = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());

            FaceColor = Integer.parseInt(preferences.getString("FaceColor", "0"));

            rpm.setFace(FaceColor);
            speed.setFace(FaceColor);

            mEnginedisplacement = Integer.parseInt(preferences.getString("Enginedisplacement", "1500"));

            m_dedectPids = Integer.parseInt(preferences.getString("DedectPids", "0"));

            if (m_dedectPids == 0) {

                commandslist.clear();

                int i = 0;

                commandslist.add(i, VOLTAGE);

                if (preferences.getBoolean("checkboxENGINE_RPM", true)) {
                    commandslist.add(i, ENGINE_RPM);
                    i++;
                }

                if (preferences.getBoolean("checkboxVEHICLE_SPEED", true)) {
                    commandslist.add(i, VEHICLE_SPEED);
                    i++;
                }

                if (preferences.getBoolean("checkboxENGINE_LOAD", true)) {
                    commandslist.add(i, ENGINE_LOAD);
                    i++;
                }

                if (preferences.getBoolean("checkboxENGINE_COOLANT_TEMP", true)) {
                    commandslist.add(i, ENGINE_COOLANT_TEMP);
                    i++;
                }

                if (preferences.getBoolean("checkboxINTAKE_AIR_TEMP", true)) {
                    commandslist.add(i, INTAKE_AIR_TEMP);
                    i++;
                }

                if (preferences.getBoolean("checkboxMAF_AIR_FLOW", true)) {
                    commandslist.add(i, MAF_AIR_FLOW);
                }

                whichCommand = 0;
            }
    }

    private void setDefaultOrientation() {

        try {

            settextsixe();
            setgaugesize();

        } catch (Exception e) {
        }
    }

    private void settextsixe() {
        int txtsize = 14;
        int sttxtsize = 12;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        Status.setTextSize(sttxtsize);
        Fuel.setTextSize(txtsize + 2);
        coolantTemperature.setTextSize(txtsize);
        engineLoad.setTextSize(txtsize);
        voltage.setTextSize(txtsize);
        Temptext.setTextSize(txtsize);
        Loadtext.setTextSize(txtsize);
        Volttext.setTextSize(txtsize);
        Airtemp_text.setTextSize(txtsize);
        airTemperature.setTextSize(txtsize);
        Maf_text.setTextSize(txtsize);
        Maf.setTextSize(txtsize);
        Info.setTextSize(sttxtsize);
    }

    public void invisiblecmd() {
        mConversationView.setVisibility(View.INVISIBLE);
        mOutEditText.setVisibility(View.INVISIBLE);
        mSendButton.setVisibility(View.INVISIBLE);
        mPidsButton.setVisibility(View.INVISIBLE);
        mTroublecodes.setVisibility(View.INVISIBLE);
        mClearTroublecodes.setVisibility(View.INVISIBLE);
        mClearlist.setVisibility(View.INVISIBLE);
        rpm.setVisibility(View.VISIBLE);
        speed.setVisibility(View.VISIBLE);
        engineLoad.setVisibility(View.VISIBLE);
        Fuel.setVisibility(View.VISIBLE);
        voltage.setVisibility(View.VISIBLE);
        coolantTemperature.setVisibility(View.VISIBLE);
        Loadtext.setVisibility(View.VISIBLE);
        Volttext.setVisibility(View.VISIBLE);
        Temptext.setVisibility(View.VISIBLE);
        Centertext.setVisibility(View.VISIBLE);
        Info.setVisibility(View.VISIBLE);
        Airtemp_text.setVisibility(View.VISIBLE);
        airTemperature.setVisibility(View.VISIBLE);
        Maf_text.setVisibility(View.VISIBLE);
        Maf.setVisibility(View.VISIBLE);
    }

    public void visiblecmd() {
        rpm.setVisibility(View.INVISIBLE);
        speed.setVisibility(View.INVISIBLE);
        engineLoad.setVisibility(View.INVISIBLE);
        Fuel.setVisibility(View.INVISIBLE);
        voltage.setVisibility(View.INVISIBLE);
        coolantTemperature.setVisibility(View.INVISIBLE);
        Loadtext.setVisibility(View.INVISIBLE);
        Volttext.setVisibility(View.INVISIBLE);
        Temptext.setVisibility(View.INVISIBLE);
        Centertext.setVisibility(View.INVISIBLE);
        Info.setVisibility(View.INVISIBLE);
        Airtemp_text.setVisibility(View.INVISIBLE);
        airTemperature.setVisibility(View.INVISIBLE);
        Maf_text.setVisibility(View.INVISIBLE);
        Maf.setVisibility(View.INVISIBLE);
        mConversationView.setVisibility(View.VISIBLE);
        mOutEditText.setVisibility(View.VISIBLE);
        mSendButton.setVisibility(View.VISIBLE);
        mPidsButton.setVisibility(View.VISIBLE);
        mTroublecodes.setVisibility(View.VISIBLE);
        mClearTroublecodes.setVisibility(View.VISIBLE);
        mClearlist.setVisibility(View.VISIBLE);
    }

    private void setgaugesize() {
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        int width = 0;
        int height = 0;

        width = display.getWidth();
        height = display.getHeight();

        if (width > height) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(height, height);

            lp.addRule(RelativeLayout.BELOW, findViewById(R.id.Load).getId());
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            lp.setMargins(0, 0, 50, 0);
            rpm.setLayoutParams(lp);
            rpm.getLayoutParams().height = height;
            rpm.getLayoutParams().width = (int) (width - 100) / 2;

            lp = new RelativeLayout.LayoutParams(height, height);
            lp.addRule(RelativeLayout.BELOW, findViewById(R.id.Load).getId());
            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            lp.setMargins(50, 0, 0, 0);
            speed.setLayoutParams(lp);
            speed.getLayoutParams().height = height;
            speed.getLayoutParams().width = (int) (width - 100) / 2;

        } else if (width < height) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, width);

            lp.addRule(RelativeLayout.BELOW, findViewById(R.id.Fuel).getId());
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            lp.setMargins(25, 5, 25, 5);
            rpm.setLayoutParams(lp);
            rpm.getLayoutParams().height = height/2;
            rpm.getLayoutParams().width = (int) (width);

            lp = new RelativeLayout.LayoutParams(width, width);
            lp.addRule(RelativeLayout.BELOW, findViewById(R.id.GaugeRpm).getId());
            //lp.addRule(RelativeLayout.ABOVE,findViewById(R.id.info).getId());
            lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
            lp.setMargins(25, 5, 25, 5);
            speed.setLayoutParams(lp);
            speed.getLayoutParams().height = height/2;
            speed.getLayoutParams().width = (int) (width);
        }
    }

    public void resetgauges() {

        speed.setTargetValue(220);
        rpm.setTargetValue(80);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        speed.setTargetValue(0);
                        rpm.setTargetValue(0);
                    }
                });
            }
        }).start();
    }

    public void resetvalues() {

        engineLoad.setText("0 %");
        voltage.setText("0 V");
        coolantTemperature.setText("0 C°");
        Info.setText("");
        airTemperature.setText("0 C°");
        Maf.setText("0 g/s");
        Fuel.setText("0 - 0 l/h");

        m_getPids = false;
        whichCommand = 0;
        trycount = 0;
        initialized = false;
        defaultStart = false;
        avgconsumption.clear();
        mConversationArrayAdapter.clear();

        resetgauges();

        sendEcuMessage(RESET);
    }

    private void connectDevice(Intent data) {
        tryconnect = true;

        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        try {
            mBtService.connect(device);
            currentdevice = device;

        } catch (Exception e) {
        }
    }

    private void setupChat() {

       mBtService = new BluetoothService(this, mBtHandler);

    }

    private void sendEcuMessage(String message) {

        if (mBtService != null)
        {
            if (mBtService.getState() != BluetoothService.STATE_CONNECTED) {
                return;
            }
            try {
                if (message.length() > 0) {

                    message = message + "\r";
                     byte[] send = message.getBytes();
                    mBtService.write(send);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendInitCommands() {
        if (initializeCommands.length != 0) {

            if (whichCommand < 0) {
                whichCommand = 0;
            }

            String send = initializeCommands[whichCommand];
            sendEcuMessage(send);

            if (whichCommand == initializeCommands.length - 1) {
                initialized = true;
                whichCommand = 0;
                sendDefaultCommands();
            } else {
                whichCommand++;
            }
        }
    }

    private void sendDefaultCommands() {

        if (commandslist.size() != 0) {

            if (whichCommand < 0) {
                whichCommand = 0;
            }

            String send = commandslist.get(whichCommand);
            sendEcuMessage(send);

            if (whichCommand >= commandslist.size() - 1) {
                whichCommand = 0;
            } else {
                whichCommand++;
            }
        }
    }

    private String clearMsg(Message msg) {
        String tmpmsg = msg.obj.toString();

        tmpmsg = tmpmsg.replace("null", "");
        tmpmsg = tmpmsg.replaceAll("\\s", "");
        tmpmsg = tmpmsg.replaceAll(">", "");
        tmpmsg = tmpmsg.replaceAll("SEARCHING...", "");
        tmpmsg = tmpmsg.replaceAll("ATZ", "");
        tmpmsg = tmpmsg.replaceAll("ATI", "");
        tmpmsg = tmpmsg.replaceAll("atz", "");
        tmpmsg = tmpmsg.replaceAll("ati", "");
        tmpmsg = tmpmsg.replaceAll("ATDP", "");
        tmpmsg = tmpmsg.replaceAll("atdp", "");
        tmpmsg = tmpmsg.replaceAll("ATRV", "");
        tmpmsg = tmpmsg.replaceAll("atrv", "");

        return tmpmsg;
    }

    private void checkPids(String tmpmsg) {
        if (tmpmsg.indexOf("41") != -1) {
            int index = tmpmsg.indexOf("41");

            String pidmsg = tmpmsg.substring(index, tmpmsg.length());

            if (pidmsg.contains("4100")) {

                setPidsSupported(pidmsg);
                return;
            }
        }
    }

    private void analysMsg(Message msg) {

        String tmpmsg = clearMsg(msg);

        generateVolt(tmpmsg);

        getElmInfo(tmpmsg);

        if (!initialized) {

            sendInitCommands();

        } else {

            checkPids(tmpmsg);

            if (!m_getPids && m_dedectPids == 1) {
                String sPIDs = "0100";
                sendEcuMessage(sPIDs);
                return;
            }

            if (commandmode) {
                getFaultInfo(tmpmsg);
                return;
            }

            try {
                analysPIDS(tmpmsg);
            } catch (Exception e) {
                Info.setText("Error : " + e.getMessage());
            }

            sendDefaultCommands();
        }
    }

    private void getFaultInfo(String tmpmsg) {

            String substr = "43";

            int index = tmpmsg.indexOf(substr);

            if (index == -1)
            {
                substr = "47";
                index = tmpmsg.indexOf(substr);
            }

            if (index != -1) {

                tmpmsg = tmpmsg.substring(index, tmpmsg.length());

                if (tmpmsg.substring(0, 2).equals(substr)) {

                    performCalculations(tmpmsg);

                    String faultCode = null;
                    String faultDesc = null;

                    if (troubleCodesArray.size() > 0) {

                        for (int i = 0; i < troubleCodesArray.size(); i++) {
                            faultCode = troubleCodesArray.get(i);
                            faultDesc = troubleCodes.getFaultCode(faultCode,vehicleId);

                            Log.e(TAG, "Fault Code: " + substr + " : " + faultCode + " desc: " + faultDesc);

                            if (faultCode != null && faultDesc != null) {
                                mConversationArrayAdapter.add(mConnectedDeviceName + ":  TroubleCode -> " + faultCode + "\n" + faultDesc);
                            } else if (faultCode != null && faultDesc == null) {
                                mConversationArrayAdapter.add(mConnectedDeviceName + ":  TroubleCode -> " + faultCode +
                                        "\n" + "Definition not found for code: " + faultCode);
                            }
                        }
                    } else {
                        faultCode = "No error found...";
                        mConversationArrayAdapter.add(mConnectedDeviceName + ":  TroubleCode -> " + faultCode);
                    }
                }
            }
    }

    protected void performCalculations(String fault) {

        final String result = fault;
        String workingData = "";
        int startIndex = 0;
        troubleCodesArray.clear();

        try{

            if(result.indexOf("43") != -1)
            {
                workingData = result.replaceAll("^43|[\r\n]43|[\r\n]", "");
            }else if(result.indexOf("47") != -1)
            {
                workingData = result.replaceAll("^47|[\r\n]47|[\r\n]", "");
            }

            for (int begin = startIndex; begin < workingData.length(); begin += 4) {
                String dtc = "";
                byte b1 = hexStringToByteArray(workingData.charAt(begin));
                int ch1 = ((b1 & 0xC0) >> 6);
                int ch2 = ((b1 & 0x30) >> 4);
                dtc += dtcLetters[ch1];
                dtc += hexArray[ch2];
                dtc += workingData.substring(begin + 1, begin + 4);

                if (dtc.equals("P0000")) {
                    continue;
                }

                troubleCodesArray.add(dtc);
            }
        }catch(Exception e)
        {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    private byte hexStringToByteArray(char s) {
        return (byte) ((Character.digit(s, 16) << 4));
    }

    private void getElmInfo(String tmpmsg) {

        if (tmpmsg.contains("ELM") || tmpmsg.contains("elm")) {
            devicename = tmpmsg;
        }

        if (tmpmsg.contains("SAE") || tmpmsg.contains("ISO")
                || tmpmsg.contains("sae") || tmpmsg.contains("iso") || tmpmsg.contains("AUTO")) {
            deviceprotocol = tmpmsg;
        }

        if (deviceprotocol != null && devicename != null) {
            devicename = devicename.replaceAll("STOPPED", "");
            deviceprotocol = deviceprotocol.replaceAll("STOPPED", "");
            Status.setText(devicename + " " + deviceprotocol);
        }
    }


    private void setPidsSupported(String buffer) {

        Info.setText("Trying to get available pids : " + String.valueOf(trycount));
        trycount++;

        StringBuilder flags = new StringBuilder();
        String buf = buffer.toString();
        buf = buf.trim();
        buf = buf.replace("\t", "");
        buf = buf.replace(" ", "");
        buf = buf.replace(">", "");

        if (buf.indexOf("4100") == 0 || buf.indexOf("4120") == 0) {

            for (int i = 0; i < 8; i++) {
                String tmp = buf.substring(i + 4, i + 5);
                int data = Integer.valueOf(tmp, 16).intValue();
//                String retStr = Integer.toBinaryString(data);
                if ((data & 0x08) == 0x08) {
                    flags.append("1");
                } else {
                    flags.append("0");
                }

                if ((data & 0x04) == 0x04) {
                    flags.append("1");
                } else {
                    flags.append("0");
                }

                if ((data & 0x02) == 0x02) {
                    flags.append("1");
                } else {
                    flags.append("0");
                }

                if ((data & 0x01) == 0x01) {
                    flags.append("1");
                } else {
                    flags.append("0");
                }
            }

            commandslist.clear();
            commandslist.add(0, VOLTAGE);
            int pid = 1;

            StringBuilder supportedPID = new StringBuilder();
            supportedPID.append("Supported PIDS:\n");
            for (int j = 0; j < flags.length(); j++) {
                if (flags.charAt(j) == '1') {
                    supportedPID.append(" " + PIDS[j] + " ");
                    if (!PIDS[j].contains("11") && !PIDS[j].contains("01") && !PIDS[j].contains("20")) {
                        commandslist.add(pid, "01" + PIDS[j]);
                        pid++;
                    }
                }
            }
            m_getPids = true;
            mConversationArrayAdapter.add(mConnectedDeviceName + ": " + supportedPID.toString());
            whichCommand = 0;
            sendEcuMessage("ATRV");

        } else {

            return;
        }
    }

    private double calculateAverage(List<Double> listavg) {
        Double sum = 0.0;
        for (Double val : listavg) {
            sum += val;
        }
        return sum.doubleValue() / listavg.size();
    }

    private void analysPIDS(String dataRecieved) {

        int A = 0;
        int B = 0;
        int PID = 0;

        if ((dataRecieved != null) && (dataRecieved.matches("^[0-9A-F]+$"))) {

            dataRecieved = dataRecieved.trim();

            int index = dataRecieved.indexOf("41");

            String tmpmsg = null;

            if (index != -1) {

                tmpmsg = dataRecieved.substring(index, dataRecieved.length());

                if (tmpmsg.substring(0, 2).equals("41")) {

                    PID = Integer.parseInt(tmpmsg.substring(2, 4), 16);
                    A = Integer.parseInt(tmpmsg.substring(4, 6), 16);
                    B = Integer.parseInt(tmpmsg.substring(6, 8), 16);

                    calculateEcuValues(PID, A, B);
                }
            }
        }
    }

    private void generateVolt(String msg) {

        String VoltText = null;

        if ((msg != null) && (msg.matches("\\s*[0-9]{1,2}([.][0-9]{1,2})\\s*"))) {

            VoltText = msg + "V";

            mConversationArrayAdapter.add(mConnectedDeviceName + ": " + msg + "V");

        } else if ((msg != null) && (msg.matches("\\s*[0-9]{1,2}([.][0-9]{1,2})?V\\s*"))) {

            VoltText = msg;

            mConversationArrayAdapter.add(mConnectedDeviceName + ": " + msg);
        }

        if (VoltText != null) {
            voltage.setText(VoltText);
        }
    }

    private void calculateEcuValues(int PID, int A, int B) {

        double val = 0;
        int intval = 0;
        int tempC = 0;

        switch (PID) {

            case 4:


                val = A * 100 / 255;
                int calcLoad = (int) val;

                engineLoad.setText(Integer.toString(calcLoad) + " %");
                mConversationArrayAdapter.add("Engine Load: " + Integer.toString(calcLoad) + " %");

                double FuelFlowLH = (mMaf * calcLoad * mEnginedisplacement / 1000.0 / 714.0) + 0.8;

                if(calcLoad == 0)
                    FuelFlowLH = 0;

                avgconsumption.add(FuelFlowLH);

                Fuel.setText(String.format("%10.1f", calculateAverage(avgconsumption)).trim() + " l/h");
                mConversationArrayAdapter.add("Fuel Consumption: " + String.format("%10.1f", calculateAverage(avgconsumption)).trim() + " l/h");
                break;

            case 5:
                tempC = A - 40;
                coolantTemp = tempC;
                coolantTemperature.setText(Integer.toString(coolantTemp) + " C°");
                mConversationArrayAdapter.add("Enginetemp: " + Integer.toString(tempC) + " C°");

                break;

            case 11:
                mConversationArrayAdapter.add("Intake Man Pressure: " + Integer.toString(A) + " kPa");

                break;

            case 12:
                val = ((A * 256) + B) / 4;
                intval = (int) val;
                rpmval = intval;
                rpm.setTargetValue(intval / 100);

                break;


            case 13:
                speed.setTargetValue(A);

                break;

            case 15:
                tempC = A - 40;
                intakeairtemp = tempC;
                airTemperature.setText(Integer.toString(intakeairtemp) + " C°");
                mConversationArrayAdapter.add("Intakeairtemp: " + Integer.toString(intakeairtemp) + " C°");

                break;

            case 16:
                val = ((256 * A) + B) / 100;
                mMaf = (int) val;
                Maf.setText(Integer.toString(intval) + " g/s");
                mConversationArrayAdapter.add("Maf Air Flow: " + Integer.toString(mMaf) + " g/s");

                break;

            case 17:
                val = A * 100 / 255;
                intval = (int) val;
                mConversationArrayAdapter.add(" Throttle position: " + Integer.toString(intval) + " %");

                break;

            case 35:
                val = ((A * 256) + B) * 0.079;
                intval = (int) val;
                mConversationArrayAdapter.add("Fuel Rail Pressure: " + Integer.toString(intval) + " kPa");

                break;

            case 49:
                val = (A * 256) + B;
                intval = (int) val;
                mConversationArrayAdapter.add("Distance traveled: " + Integer.toString(intval) + " km");

                break;

            case 70:
                tempC = A - 40;
                ambientairtemp = tempC;
                mConversationArrayAdapter.add("Ambientairtemp: " + Integer.toString(ambientairtemp) + " C°");

                break;

            case 92:
                tempC = A - 40;
                engineoiltemp = tempC;
                mConversationArrayAdapter.add("Engineoiltemp: " + Integer.toString(engineoiltemp) + " C°");

                break;

            default:
        }
    }

    enum RSP_ID {
        PROMPT(">"),
        OK("OK"),
        MODEL("ELM"),
        NODATA("NODATA"),
        SEARCH("SEARCHING"),
        ERROR("ERROR"),
        NOCONN("UNABLE"),
        NOCONN_MSG("UNABLE TO CONNECT"),
        NOCONN2("NABLETO"),
        CANERROR("CANERROR"),
        CONNECTED("ECU CONNECTED"),
        BUSBUSY("BUSBUSY"),
        BUSY("BUSY"),
        BUSERROR("BUSERROR"),
        BUSINIERR("BUSINIT:ERR"),
        BUSINIERR2("BUSINIT:BUS"),
        BUSINIERR3("BUSINIT:...ERR"),
        BUS("BUS"),
        FBERROR("FBERROR"),
        DATAERROR("DATAERROR"),
        BUFFERFULL("BUFFERFULL"),
        STOPPED("STOPPED"),
        RXERROR("<"),
        QMARK("?"),
        UNKNOWN("");
        private String response;

        RSP_ID(String response) {
            this.response = response;
        }

        @Override
        public String toString() {
            return response;
        }
    }
}