package lt.andro.metasocket.voice;

import android.content.Context;

import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import hugo.weaving.DebugLog;
import lt.andro.metasocket.Config;
import lt.andro.metasocket.mvp.presenter.LightsListenerPresenter;

/**
 * @author Vilius Kraujutis
 * @since 2017-03-09
 */
public class LightsListenerImpl implements LightsListener {

    private static final String ACTION_LIGHT_SWITCH = "light.switch";
    private static final String PARAMETER_LIGHT_STATE = "light-state";
    private static final String VALUE_LIGHT_ON = "on";
    private static final String VALUE_LIGHT_OFF = "on";

    private final AIConfiguration config;
    private final AIService aiService;
    private LightsListenerPresenter presenter;

    @DebugLog
    public LightsListenerImpl(Context context) {
        config = new AIConfiguration(Config.CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(context, config);
        aiService.setListener(this);
    }

    @DebugLog
    @Override
    public void startListening(LightsListenerPresenter presenter) {
        this.presenter = presenter;
        aiService.startListening();
    }

    @DebugLog
    @Override
    public void stopListening() {
        aiService.stopListening();
    }

    @DebugLog
    @Override
    public void cancelListening() {
        aiService.cancel();
    }

    @DebugLog
    @Override
    public void onResult(AIResponse result) {
        Result res = result.getResult();
        String action = res.getAction();
        if (action.equals(ACTION_LIGHT_SWITCH)) {
            String lightState = res.getParameters().get(PARAMETER_LIGHT_STATE).getAsString();
            if (lightState.equals(VALUE_LIGHT_ON)) {
                presenter.onTurnOnLightsCommandReceived();
            } else if (lightState.equals(VALUE_LIGHT_OFF)) {
                presenter.onTurnOffLightsCommandReceived();
            } else {
                IllegalArgumentException exception = new IllegalArgumentException("Not expected light-state parameter value: " + lightState);
                presenter.onError(exception);
            }
        }
    }

    @DebugLog
    @Override
    public void onError(AIError error) {
        presenter.onError(new Exception(error.getMessage()));
    }

    @DebugLog
    @Override
    public void onAudioLevel(float level) {

    }

    @DebugLog
    @Override
    public void onListeningStarted() {
        presenter.showListening(true);
    }

    @DebugLog
    @Override
    public void onListeningCanceled() {
        presenter.showListening(false);
    }

    @DebugLog
    @Override
    public void onListeningFinished() {
        presenter.showListening(false);
    }

    @Override
    public String toString() {
        return "LightsListenerImpl{" +
                "config=" + config +
                ", aiService=" + aiService +
                '}';
    }
}
