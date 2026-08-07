package net.flectone.pulse.processing.processor;

/**
 * Decodes a message that arrived from another server and hands it to the module it belongs to.
 * @author TheFaser
 */
public interface ProxyMessageProcessor {

    /**
     * Decodes and dispatches one message.
     *
     * @param bytes the raw payload
     */
    void process(byte[] bytes);

}
