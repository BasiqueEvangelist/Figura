package net.blancworks.figura.network.messages;

import com.google.common.io.LittleEndianDataOutputStream;

import java.io.IOException;

public class AuthenticateMessageSender extends MessageSender {
    private final String token;

    public AuthenticateMessageSender(String token) {
        this.token = token;
    }

    @Override
    public String getProtocolName() {
        return "figura_v1:authenticate";
    }

    @Override
    protected void write(LittleEndianDataOutputStream stream) throws IOException {
        writeString(token, stream);
    }
}
