package chat.randomidea.com.chatterbox;

/**
 * Handler for Incoming Text
 * Subclass for specific implementation (Unity vs Native)
 */

interface MessageHandler {
    void handleMessage(String jsonString);
}
