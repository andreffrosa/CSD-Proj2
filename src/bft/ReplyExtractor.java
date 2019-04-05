package bft;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.core.messages.TOMMessageType;
import bftsmart.tom.util.Extractor;

public class ReplyExtractor implements Extractor {

	public ReplyExtractor() {}

	@Override
	public TOMMessage extractResponse(TOMMessage[] replies, int sameContent, int lastReceived) {

		int sender = replies[lastReceived].getSender();
		int session = replies[lastReceived].getSession();
		int sequence = replies[lastReceived].getSequence();
		int operationid = replies[lastReceived].getOperationId();
		int view = replies[lastReceived].getViewID();
		TOMMessageType type = replies[lastReceived].getReqType();

		byte[] content = null;
		try {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutput objOut = new ObjectOutputStream(byteOut);

			objOut.writeInt(sameContent); System.out.println("sameContent: " + sameContent);
			System.out.println("replies.length: " + replies.length);

			int written = 0;
			for( int i = 0; i < replies.length && written < sameContent; i++ ) {
				if(replies[i] != null) {
					objOut.writeInt(replies[i].getSender());
					objOut.writeObject(replies[i].getContent());
					objOut.writeObject(replies[i].serializedMessageSignature);
					written++;
					System.out.println("reply["+i+"] is not null");
				} else
					System.out.println("reply["+i+"] is null");
			} System.out.println("written: " + written);
			objOut.flush();
			byteOut.flush();
			
			content = byteOut.toByteArray();

			objOut.close();
			byteOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		TOMMessage reply = new TOMMessage(sender, session, sequence, operationid, content, view, type);

		return reply;
	}
}
