package lt.andro.metasocket;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lt.andro.metasocket.mvp.presenter.MainActivityPresenter;
import lt.andro.metasocket.mvp.presenter.MainActivityPresenterImpl;
import lt.andro.metasocket.mvp.view.MainActivityView;
import lt.andro.metasocket.permission.PermissionsController;
import lt.andro.metasocket.permission.PermissionsControllerImpl;
import lt.andro.metasocket.voice.LightsListener;
import lt.andro.metasocket.voice.LightsListenerImpl;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements MainActivityView {

    @BindView(R.id.main_progress_bar)
    public ProgressBar progressBar;
    @BindView(R.id.main_button_on)
    public Button buttonOn;
    @BindView(R.id.main_button_off)
    public Button buttonOff;
    @BindView(R.id.main_voice_button)
    public Button voiceButton;

    MainActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Timber.i("Activity created");

        LightsListener listener = new LightsListenerImpl(this.getApplicationContext());
        PermissionsController permissionsController = new PermissionsControllerImpl(this, this);
        presenter = new MainActivityPresenterImpl(this, this, listener, permissionsController);
        presenter.onAttach();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDetach();
    }

    @OnClick(R.id.main_button_on)
    public void onButtonOnClicked(View button) {
        presenter.onButtonOnClicked();
    }

    @OnClick(R.id.main_button_off)
    public void onButtonOffClicked(View button) {
        presenter.onButtonOffClicked();
    }

    @Override
    public void showOnOffButtons(boolean visible) {
        buttonOn.setVisibility(visible ? VISIBLE : GONE);
        buttonOff.setVisibility(visible ? VISIBLE : GONE);
    }

    @Override
    public void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? VISIBLE : GONE);
    }

    @Override
    public void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showListening(boolean listening) {
        voiceButton.setText(listening ? R.string.main_voice_button_listening : R.string.main_voice_button_listen);
        int left = listening ? R.drawable.ic_mic_off_black_24dp : R.drawable.ic_mic_black_24dp;
        voiceButton.setCompoundDrawablesWithIntrinsicBounds(left, 0, 0, 0);
    }

    @OnClick(R.id.main_voice_button)
    public void onVoiceControlButtonClicked(View button) {
        presenter.onVoiceControlButtonClicked();
    }
}
