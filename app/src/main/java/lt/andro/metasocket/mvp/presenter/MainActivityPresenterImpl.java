package lt.andro.metasocket.mvp.presenter;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.module.Gpio;

import lt.andro.metasocket.mvp.view.MainActivityView;
import timber.log.Timber;

/**
 * @author Vilius Kraujutis
 * @since 2017-03-08
 */
public class MainActivityPresenterImpl implements MainActivityPresenter {
    private MetaWearBoard mwBoard;
    private final byte GPIO_PIN = 0;

    private Context context;
    private MainActivityView view;

    public MainActivityPresenterImpl(Context context, MainActivityView view) {
        this.context = context;
        this.view = view;
    }

    @Override
    public void onButtonOnClicked() {
        setMode(Gpio.PullMode.PULL_UP);
    }

    @Override
    public void onButtonOffClicked() {
        setMode(Gpio.PullMode.PULL_DOWN);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ///< Typecast the binder to the service's LocalBinder class
        MetaWearBleService.LocalBinder serviceBinder = (MetaWearBleService.LocalBinder) service;

        //final String MW_MAC_ADDRESS= "D0:92:E2:8C:30:BA";
        final String MW_MAC_ADDRESS = "D5:CD:CA:4B:66:23";

        final BluetoothManager btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice(MW_MAC_ADDRESS);

        serviceBinder.executeOnUiThread();

        Timber.i("Service connected");

        mwBoard = serviceBinder.getMetaWearBoard(remoteDevice);
        mwBoard.setConnectionStateHandler(new MetaWearBoard.ConnectionStateHandler() {
            @Override
            public void connected() {
                Toast.makeText(context, "Connected", Toast.LENGTH_LONG).show();

                Timber.i("Connected");
                Timber.i("MetaBoot? %s", mwBoard.inMetaBootMode());

                mwBoard.readDeviceInformation().onComplete(new AsyncOperation.CompletionHandler<MetaWearBoard.DeviceInformation>() {
                    @Override
                    public void success(MetaWearBoard.DeviceInformation result) {
                        Timber.i("Device information: %s", result.toString());
                        view.showLoading(false);
                        view.showOnOffButtons(true);
                    }

                    @Override
                    public void failure(Throwable error) {
                        String msg = "Error reading device information: " + error.getLocalizedMessage();
                        Timber.e(error, msg);
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void disconnected() {
                Toast.makeText(context, "Disconnected", Toast.LENGTH_LONG).show();
                Timber.i("Disconnected");
            }

            @Override
            public void failure(int status, final Throwable error) {
                Toast.makeText(context, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                Timber.e(error, "Error connecting");
            }
        });

        mwBoard.connect();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }

    @Override
    public void onAttach() {

        view.showLoading(true);
        view.showOnOffButtons(false);

        ///< Bind the service when the activity is shown
        context
                .getApplicationContext()
                .bindService(new Intent(context, MetaWearBleService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        ///< Unbind the service when the activity is hidden
        context.getApplicationContext().unbindService(this);
    }


    private void setMode(Gpio.PullMode pullMode) {
        try {
            Gpio gpioModule = mwBoard.getModule(Gpio.class);
            gpioModule.setPinPullMode(GPIO_PIN, pullMode);
        } catch (UnsupportedModuleException e) {
            e.printStackTrace();
        }
    }

}
