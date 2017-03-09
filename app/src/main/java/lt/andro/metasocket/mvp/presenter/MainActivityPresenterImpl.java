package lt.andro.metasocket.mvp.presenter;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.module.Gpio;

import hugo.weaving.DebugLog;
import lt.andro.metasocket.Config;
import lt.andro.metasocket.mvp.view.MainActivityView;
import lt.andro.metasocket.permission.PermissionsController;
import lt.andro.metasocket.voice.LightsListener;
import timber.log.Timber;

/**
 * @author Vilius Kraujutis
 * @since 2017-03-08
 */
public class MainActivityPresenterImpl implements MainActivityPresenter, LightsListenerPresenter {
    public static final String RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO;
    private MetaWearBoard mwBoard;

    private Context context;
    private MainActivityView view;
    private LightsListener lightsListener;
    private PermissionsController permissionsController;
    private boolean isListening;

    public MainActivityPresenterImpl(Context context,
                                     MainActivityView view,
                                     LightsListener lightsListener,
                                     PermissionsController permissionsController) {
        this.context = context;
        this.view = view;
        this.lightsListener = lightsListener;
        this.permissionsController = permissionsController;

        isListening = false;
    }

    @DebugLog
    @Override
    public void onButtonOnClicked() {
        turnOn();
    }

    @DebugLog
    private void turnOn() {
        turnOff();
    }

    @DebugLog
    private void turnOff() {
        setMode(Gpio.PullMode.PULL_UP);
    }

    @DebugLog
    @Override
    public void onButtonOffClicked() {
        setMode(Gpio.PullMode.PULL_DOWN);
    }


    @DebugLog
    @Override
    public void onVoiceControlButtonClicked() {
        setListening(!isListening);

        if (isListening) {
            if (permissionsController.hasPermissionGranted(RECORD_AUDIO_PERMISSION)) {
                lightsListener.startListening(this);
            } else {
                setListening(false);
                permissionsController.requestPermission(RECORD_AUDIO_PERMISSION);
            }
        } else {
            lightsListener.stopListening();
        }
    }

    @DebugLog
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
                view.showMessage("Connected");

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
                        view.showMessage(msg);
                    }
                });
            }

            @Override
            public void disconnected() {
                view.showMessage("Disconnected");
                Timber.i("Disconnected");
            }

            @Override
            public void failure(int status, final Throwable error) {
                view.showMessage(error.getLocalizedMessage());
                Timber.e(error, "Error connecting");
            }
        });

        mwBoard.connect();
    }

    @DebugLog
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }

    @DebugLog
    @Override
    public void onAttach() {

        view.showLoading(true);
        view.showOnOffButtons(false);

        ///< Bind the service when the activity is shown
        context
                .getApplicationContext()
                .bindService(new Intent(context, MetaWearBleService.class), this, Context.BIND_AUTO_CREATE);
    }

    @DebugLog
    @Override
    public void onDetach() {
        ///< Unbind the service when the activity is hidden
        context.getApplicationContext().unbindService(this);
    }


    @DebugLog
    private void setMode(Gpio.PullMode pullMode) {
        try {
            Gpio gpioModule = mwBoard.getModule(Gpio.class);
            if (gpioModule != null) {
                gpioModule.setPinPullMode(Config.RELAY_SWITCH_GPIO_PIN, pullMode);
            } else {
                onError(new IllegalStateException("Not connected to any light switches right now."));
            }
        } catch (UnsupportedModuleException e) {
            e.printStackTrace();
        }
    }

    @DebugLog
    @Override
    public void setListening(boolean listening) {
        isListening = listening;
        view.showListening(listening);
    }

    @DebugLog
    @Override
    public void onTurnOnLightsCommandReceived() {
        turnOn();
    }

    @DebugLog
    @Override
    public void onTurnOffLightsCommandReceived() {
        turnOff();
    }

    @DebugLog
    @Override
    public void onError(Throwable throwable) {
        Timber.e(throwable);
        view.showMessage(throwable.getLocalizedMessage());
    }

    @Override
    public String toString() {
        return "MainActivityPresenterImpl{" +
                "mwBoard=" + mwBoard +
                ", context=" + context +
                ", view=" + view +
                ", lightsListener=" + lightsListener +
                ", permissionsController=" + permissionsController +
                ", isListening=" + isListening +
                '}';
    }
}
