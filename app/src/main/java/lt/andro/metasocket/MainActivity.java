package lt.andro.metasocket;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.module.Gpio;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements ServiceConnection {
    final byte GPIO_PIN = 0;

    private MetaWearBoard mwBoard;

    @BindView(R.id.main_progress_bar)
    public ProgressBar progressBar;
    @BindView(R.id.button_on)
    public Button buttonOn;
    @BindView(R.id.button_off)
    public Button buttonOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Timber.i("Activity created");
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button_on)
    public void onButtonOnClicked(View button) {
        setMode(Gpio.PullMode.PULL_UP);
    }

    @OnClick(R.id.button_off)
    public void onButtonOffClicked(View button) {
        setMode(Gpio.PullMode.PULL_DOWN);
    }

    private void setMode(Gpio.PullMode pullMode) {
        try {
            Gpio gpioModule = mwBoard.getModule(Gpio.class);
            gpioModule.setPinPullMode(GPIO_PIN, pullMode);
        } catch (UnsupportedModuleException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ///< Unbind the service when the activity is hidden
        getApplicationContext().unbindService(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        progressBar.setVisibility(VISIBLE);
        buttonOff.setVisibility(GONE);
        buttonOn.setVisibility(GONE);

        ///< Bind the service when the activity is shown
        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class),
                this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ///< Typecast the binder to the service's LocalBinder class
        MetaWearBleService.LocalBinder serviceBinder = (MetaWearBleService.LocalBinder) service;

        //final String MW_MAC_ADDRESS= "D0:92:E2:8C:30:BA";
        final String MW_MAC_ADDRESS = "D5:CD:CA:4B:66:23";

        final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        serviceBinder.executeOnUiThread();

        Timber.i("Service connected");

        mwBoard = serviceBinder.getMetaWearBoard(remoteDevice);
        mwBoard.setConnectionStateHandler(new MetaWearBoard.ConnectionStateHandler() {
            @Override
            public void connected() {
                Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_LONG).show();

                Timber.i("Connected");
                Timber.i("MetaBoot? %s", mwBoard.inMetaBootMode());

                mwBoard.readDeviceInformation().onComplete(new AsyncOperation.CompletionHandler<MetaWearBoard.DeviceInformation>() {
                    @Override
                    public void success(MetaWearBoard.DeviceInformation result) {
                        Timber.i("Device information: %s", result.toString());
                        progressBar.setVisibility(GONE);

                        buttonOn.setVisibility(VISIBLE);
                        buttonOff.setVisibility(VISIBLE);
                    }

                    @Override
                    public void failure(Throwable error) {
                        String msg = "Error reading device information: " + error.getLocalizedMessage();
                        Timber.e(error, msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void disconnected() {
                Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_LONG).show();
                Timber.i("Disconnected");
            }

            @Override
            public void failure(int status, final Throwable error) {
                Toast.makeText(MainActivity.this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Timber.e(error, "Error connecting");
            }
        });

        mwBoard.connect();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }
}
