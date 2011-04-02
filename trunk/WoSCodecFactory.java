import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;


public class WoSCodecFactory implements ProtocolCodecFactory {
    private final ProtocolEncoder encoder;
    private final ProtocolDecoder decoder;

    public WoSCodecFactory() {
    	encoder = new WoSResponseEncoder();
       	decoder = new WoSRequestDecoder();
    }

    public ProtocolEncoder getEncoder() throws Exception {
        return encoder;
    }

    public ProtocolDecoder getDecoder() throws Exception {
        return decoder;
    }

}
