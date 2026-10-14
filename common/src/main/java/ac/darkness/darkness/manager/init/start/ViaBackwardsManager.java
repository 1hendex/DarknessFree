package ac.darkness.darkness.manager.init.start;

public class ViaBackwardsManager implements StartableInitable {
    @Override
    public void start() {
        System.setProperty("com.viaversion.handlePingsAsInvAcknowledgements", "true");
    }
}
