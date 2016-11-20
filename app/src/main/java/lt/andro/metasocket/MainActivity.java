package lt.andro.metasocket;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.module.Gpio;
import com.mbientlab.metawear.module.IBeacon;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
    final byte GPIO_PIN = 0;

    private MetaWearBleService.LocalBinder serviceBinder;
    private MetaWearBoard mwBoard;
    private ProgressBar progressBar;
    private Button buttonOn;
    private Button buttonOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("test", "Activity created");
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        buttonOn = (Button) findViewById(R.id.button_on);
        buttonOff = (Button) findViewById(R.id.button_off);

        buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Gpio gpioModule = mwBoard.getModule(Gpio.class);
                    gpioModule.clearDigitalOut(GPIO_PIN);
                } catch (UnsupportedModuleException e) {
                    e.printStackTrace();
                }
            }
        });

        buttonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Gpio gpioModule = mwBoard.getModule(Gpio.class);
                    gpioModule.setDigitalOut(GPIO_PIN);
                } catch (UnsupportedModuleException e) {
                    e.printStackTrace();
                }
            }
        });

        ///< Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class),
                this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ///< Unbind the service when the activity is destroyed
        getApplicationContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ///< Typecast the binder to the service's LocalBinder class
        serviceBinder = (MetaWearBleService.LocalBinder) service;

        //final String MW_MAC_ADDRESS= "D0:92:E2:8C:30:BA";
        final String MW_MAC_ADDRESS= "D5:CD:CA:4B:66:23";

        final BluetoothManager btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        serviceBinder.executeOnUiThread();

        Log.i("test", "Service connected");

        mwBoard = serviceBinder.getMetaWearBoard(remoteDevice);
        mwBoard.setConnectionStateHandler(new MetaWearBoard.ConnectionStateHandler() {
            @Override
            public void connected() {
                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();

                Log.i("test", "Connected");
                Log.i("test", "MetaBoot? " + mwBoard.inMetaBootMode());

                mwBoard.readDeviceInformation().onComplete(new AsyncOperation.CompletionHandler<MetaWearBoard.DeviceInformation>() {
                    @Override
                    public void success(MetaWearBoard.DeviceInformation result) {
                        Log.i("test", "Device Information: " + result.toString());
                        progressBar.setVisibility(View.GONE);

                        buttonOn.setVisibility(View.VISIBLE);
                        buttonOff.setVisibility(View.VISIBLE);

                        final Gpio gpioModule;
                        try {
                            gpioModule = mwBoard.getModule(Gpio.class);
                            gpioModule.setPinPullMode(GPIO_PIN, Gpio.PullMode.PULL_DOWN);
                        } catch (UnsupportedModuleException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failure(Throwable error) {
                        Log.e("test", "Error reading device information", error);
                    }
                });
            }

            @Override
            public void disconnected() {
                Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_LONG).show();
                Log.i("test", "Disconnected");
            }

            @Override
            public void failure(int status, final Throwable error) {
                Toast.makeText(MainActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Log.e("test", "Error connecting", error);
            }
        });

        mwBoard.connect();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) { }
}
