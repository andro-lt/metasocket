package lt.andro.metasocket;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lt.andro.metasocket.mvp.presenter.MainActivityPresenter;
import lt.andro.metasocket.mvp.presenter.MainActivityPresenterImpl;
import lt.andro.metasocket.mvp.view.MainActivityView;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements MainActivityView {

    @BindView(R.id.main_refresh_layout)
    SwipeRefreshLayout refreshLayout;
    @BindView(R.id.button_on)
    public Button buttonOn;
    @BindView(R.id.button_off)
    public Button buttonOff;

    MainActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Timber.i("Activity created");

        presenter = new MainActivityPresenterImpl(this, this);
        presenter.onAttach();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.onRefreshRequested();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDetach();
    }

    @OnClick(R.id.button_on)
    public void onButtonOnClicked(View button) {
        presenter.onButtonOnClicked();
    }

    @OnClick(R.id.button_off)
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
        refreshLayout.setRefreshing(loading);
    }
}
