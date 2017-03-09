package lt.andro.metasocket.mvp.view;

/**
 * @author Vilius Kraujutis
 * @since 2017-03-08
 */
public interface MainActivityView extends MessagePresentingView {
    void showOnOffButtons(boolean visible);

    void showLoading(boolean loading);

    void showListening(boolean listening);
}
