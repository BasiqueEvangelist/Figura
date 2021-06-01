package net.blancworks.figura.network.messages;

import com.google.common.io.LittleEndianDataOutputStream;

import java.io.IOException;

public class AuthenticateMessageSender extends MessageSender {
    private final String token;

    public AuthenticateMessageSender(String token) {
        super(MessageIDs.AUTHENTICATE_MESSAGE_ID);
        this.token = token;
    }

    @Override
    protected void write(LittleEndianDataOutputStream stream) throws IOException {
        writeString(token, stream);
    }
}
