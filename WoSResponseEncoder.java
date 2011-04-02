import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;


public class WoSResponseEncoder extends ProtocolEncoderAdapter {
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        String temp = message.toString();
        int capacity = temp.length() + 1;
        ByteBuffer buffer = ByteBuffer.allocate(capacity, false);
        buffer.put(temp.getBytes(Charset.forName("us-ascii")));
        buffer.put((byte) 13);
        buffer.flip();
        out.write(buffer);
    }
}
