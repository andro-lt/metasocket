package lt.andro.metasocket.voice;

import lt.andro.metasocket.mvp.presenter.LightsListenerPresenter;

/**
 * @author Vilius Kraujutis
 * @since 2017-03-09
 */
public interface LightsListener extends ai.api.AIListener {

    /**
     * Launch listening from the microphone via the startListening method.
     * The SDK will start listening for the microphone input of the mobile device.
     */
    void startListening(LightsListenerPresenter presenter);

    /**
     * To stop listening and start the request to the API.AI service using the current recognition results
     */
    void stopListening();

    /**
     * To cancel the listening process without sending a request to the API.AI service,
     * call the cancel method of the AIService class.
     */
    void cancelListening();
}
