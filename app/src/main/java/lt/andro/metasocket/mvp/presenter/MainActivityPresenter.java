package lt.andro.metasocket.mvp.presenter;

import android.content.ServiceConnection;

/**
 * @author Vilius Kraujutis
 * @since 2017-03-08
 */
public interface MainActivityPresenter extends ServiceConnection, BasePresenter {
    void onButtonOnClicked();

    void onButtonOffClicked();
}
