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
import android.widget.Toast;
import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.module.IBeacon;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
    private MetaWearBleService.LocalBinder serviceBinder;
    private MetaWearBoard mwBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        final String MW_MAC_ADDRESS= "C5:24:07:C3:20:9F";

        final BluetoothManager btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        serviceBinder.executeOnUiThread();

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
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) { }
}
