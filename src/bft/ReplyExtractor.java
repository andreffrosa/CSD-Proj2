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
				//System.out.println(replies[i].serializedMessageSignature);
				System.out.println(new String(java.util.Base64.getEncoder().encode(new String(replies[i].serializedMessageSignature, replies[i].serializedMessageSignature.length).getBytes())));
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

		byte[] content = new byte[0];
		try {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutput objOut = new ObjectOutputStream(byteOut);

			objOut.writeInt(sameContent);

			int written = 0;
			for( int i = 0; i < replies.length && written < sameContent; i++ ) {
				if(replies[i] != null) {
					objOut.writeInt(replies[i].getSender());
					objOut.writeObject(replies[i].getContent());
					objOut.writeObject(replies[i].serializedMessageSignature);
					System.out.println("wiritng " + i + " ...");
					written++;
				}
			}
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
