package py.com.sodep.mobileforms.web.activation;

public class ActivationResponse {
    private boolean isSentEmail;

    public ActivationResponse() {
    }

    public ActivationResponse(boolean isSentEmail) {
        this.isSentEmail = isSentEmail;
    }

    public boolean isSentEmail() {
        return isSentEmail;
    }

    public void setSentEmail(boolean sentEmail) {
        isSentEmail = sentEmail;
    }
}
