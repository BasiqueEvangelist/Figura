package net.blancworks.figura.network.messages;

import com.google.common.io.LittleEndianDataInputStream;
import net.blancworks.figura.FiguraMod;
import net.minecraft.util.Util;

import java.util.UUID;

public class AuthenticateResponseHandler extends MessageHandler {
    @Override
    public String getProtocolName() {
        return "figura_v1:authenticate";
    }

    @Override
    public void handleMessage(LittleEndianDataInputStream stream) throws Exception {
        super.handleMessage(stream);

        UUID uuid = readUUID(stream);
        if (uuid.equals(Util.NIL_UUID))
            FiguraMod.LOGGER.error("Authentication failed!");
    }
}
