package net.vrllogistics.hikvision.activities;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import net.vrllogistics.hikvision.R;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public final String ACTION_USB_PERMISSION = "net.vrllogistics.hikvision.USB_PERMISSION";
    static UsbManager manager = null;
    static UsbDevice device = null;
    static UsbSerialDevice serialPort=null;
    static UsbDeviceConnection connection=null;



    abstract class MCallBack implements UsbSerialInterface.UsbReadCallback {

        @Override
        public void onReceivedData(byte[] bytes) {

                playVideo(bytes);

        }

        public abstract void playVideo(byte[] bytes);
    }


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = manager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(new MCallBack() {
                                @Override
                                public void playVideo(final byte[] bytes) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String ss = new String(bytes);
                                            Toast.makeText(MainActivity.this, ss , Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            });

                        } else {
                            Log.e("SERIAL", "PORT NOT OPEN");
                            // Toast.makeText(ContactlessCardActivity.this, "PORT NOT OPEN", Toast.LENGTH_SHORT).show();
                            showAlertMessage("PORT NOT OPEN","Warning","Reload");
                        }
                    } else {
                        Log.e("SERIAL", "PORT IS NULL");
                        //Toast.makeText(ContactlessCardActivity.this, "PORT IS NULL", Toast.LENGTH_SHORT).show();
                        showAlertMessage("PORT IS NULL","Warning","Reload");
                    }
                } else {
                    //Toast.makeText(ContactlessCardActivity.this, "SERIAL PERM NOT GRANTED", Toast.LENGTH_SHORT).show();
                    showAlertMessage("SERIAL PERM NOT GRANTED","Warning","Reload");
                    Log.e("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {

                Toast.makeText(MainActivity.this, "USB ATTACHED HV", Toast.LENGTH_SHORT).show();

            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {

                Toast.makeText(MainActivity.this, "USB DETACHED HV", Toast.LENGTH_SHORT).show();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
        createFolder();
        init();
        getDevice();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             startActivity(new Intent(MainActivity.this,VideoListActivity.class));
            }
        });
    }

   public void init() {

   }

   public void createFolder() {
       String folder_main = "HikVision";
       File hikVisionFolder = new File(Environment.getExternalStorageDirectory()+"/"+folder_main);
       if (!hikVisionFolder.exists()) {
           hikVisionFolder.mkdirs();
           Log.e("TAG","Folder Created...AT "+hikVisionFolder.getPath());
       } else {
           Log.e("TAG","Folder Already exits");
       }
   }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void getDevice() {
        try {
            manager = (UsbManager) getSystemService(Context.USB_SERVICE);
            HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
            for (Map.Entry<String, UsbDevice> entry : deviceList.entrySet()) {
                UsbDevice d = entry.getValue();
               // int deviceId=d.getVendorId();
                // 9025 Arduino Vendor ID
                //if ( deviceId == 9025) {
                    //found our Arduino device
                    device=d;
                    PendingIntent pi = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    manager.requestPermission(device, pi);
                    String messageToast = "Device Connected is: "+device.getDeviceName()+" Ven Id : "+device.getVendorId()+" Prod Id : "+device.getProductId();
                    Toast.makeText(MainActivity.this, messageToast , Toast.LENGTH_SHORT).show();
             //   }
            }
        }catch (Exception e){
            Toast.makeText(this,"Error in getDevice : "+e.getLocalizedMessage(),Toast.LENGTH_LONG).show();}
    }



    public void showAlertMessage(String message,String title, String buttonName){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton(buttonName, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this,MainActivity.class));
                MainActivity.this.finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
