import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.nio.charset.*;

public class WoSRequestDecoder extends CumulativeProtocolDecoder {
    private String assembly;
    
	protected boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
    	String temp = in.getString(Charset.forName("us-ascii").newDecoder());
    	if (temp.endsWith(Character.toString((char) 13))) {
    		if (assembly != null)
    			assembly = assembly + temp;
    		else
    			assembly = temp;
    		out.write(assembly);
    		assembly = null;
    		return true;
    	} else {
    		if (assembly == null)
    			assembly = temp;
    		else
    			assembly = assembly + temp;
    		return false;
    	}
	}
}
