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

	@SuppressWarnings("deprecation")
	@Override
	public TOMMessage extractResponse(TOMMessage[] replies, int sameContent, int lastReceived) {
		
		System.out.println("sameContent: " + sameContent);

		for( int i = 0; i < replies.length; i++ ) {
			if(replies[i] != null) {
				int id = replies[i].getOperationId();
				boolean signed = replies[i].signed;
				System.out.println(replies[i].serializedMessageSignature);
				int sender = replies[i].getSender();
				System.out.println("id: " + id + " signed: " + signed + " sender: " + sender);
				System.out.println(new String(java.util.Base64.getEncoder().encode(new String(replies[i].serializedMessage, replies[i].serializedMessage.length).getBytes())));
				//System.out.println(new String(Base64.encode(replies[i].serializedMessage)));
			} else
				System.out.println("reply[" + i + "] is null!");
		}

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
			objOut.writeInt(sameContent);
			objOut.writeObject(replies[lastReceived].getContent());
			for( int i = 0; i < replies.length; i++ ) {
				if(replies[i] != null) {
					objOut.writeObject(replies[i].serializedMessageSignature);
					objOut.writeInt(replies[i].getId());
				}
			}
			objOut.flush();
			byteOut.flush();
			content = byteOut.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		TOMMessage reply = new TOMMessage(sender, session, sequence, operationid, content, view, type);
		
		return reply;
	}
}
