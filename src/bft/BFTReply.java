package bft;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BFTReply {

	private int n_replies;
	private byte[] reply;
	private byte[][] signatures;
	private int[] ids;

	public BFTReply() {}

	public BFTReply(int n_replies, byte[] reply, byte[][] signatures, int[] ids) {
		this.n_replies = n_replies;
		this.reply = reply;
		this.signatures = signatures;
		this.ids = ids;
	}

	public byte[][] getSignatures() {
		return signatures;
	}

	public int[] getids() {
		return ids;
	}

	public int getRepliesNumber() {
		return n_replies;
	}

	public byte[] getReply() {
		return reply;
	}

	public boolean validateSignatures() {
		// TODO
		System.out.println("Replies verification not implemented!");
		return true;
	}

	public int getReplyAsInt() {
		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				ObjectInput objIn = new ObjectInputStream(byteIn)) {

			return objIn.readInt();
		} catch(IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public boolean getReplyAsBoolean() {
		try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				ObjectInput objIn = new ObjectInputStream(byteIn)) {

			return objIn.readBoolean();
		} catch(IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
