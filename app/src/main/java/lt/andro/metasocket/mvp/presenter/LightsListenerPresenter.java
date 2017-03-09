package lt.andro.metasocket.mvp.presenter;

/**
 * @author Vilius Kraujutis
 * @since 2017-03-09
 */
public interface LightsListenerPresenter {
    void showListening(boolean listening);

    void onTurnOnLightsCommandReceived();

    void onTurnOffLightsCommandReceived();

    void onError(Throwable throwable);
}
