package net.blancworks.figura.network.messages.user;

import com.google.common.io.LittleEndianDataOutputStream;
import net.blancworks.figura.network.messages.MessageIDs;
import net.blancworks.figura.network.messages.MessageSender;

import java.io.IOException;
import java.util.UUID;

public class UserGetCurrentAvatarMessageSender extends MessageSender {
    public UUID id;
    
    public UserGetCurrentAvatarMessageSender(UUID id) {
        super(MessageIDs.USER_GET_CURRENT_AVATAR_MESSAGE_ID);
        this.id = id;
    }

    @Override
    protected void write(LittleEndianDataOutputStream stream) throws IOException {
        super.write(stream);
        
        writeUUID(id, stream);
    }
}
