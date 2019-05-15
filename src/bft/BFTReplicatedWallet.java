package bft;

import com.google.gson.GsonBuilder;

import rest.DistributedWallet;
import rest.entities.AtomicTransferRequest;
import rest.entities.BalanceRequest;
import rest.entities.GetBetweenOrderPreservingRequest;
import rest.entities.GetOrderPreservingRequest;
import rest.entities.LedgerRequest;
import rest.entities.PutOrderPreservingRequest;
import rest.entities.TransferRequest;

/**
 * Handler for the REST server. Contains a BFTSMaRt client and server instances.
 * 
 */

public class BFTReplicatedWallet implements DistributedWallet {

	@SuppressWarnings("unused")
	private BFTWalletServer server;
	private BFTWalletClient wallet;

	public BFTReplicatedWallet(int id) {
		this(id, false);
	}

	public BFTReplicatedWallet(int id, boolean byzantine) {
		wallet = new BFTWalletClient(id);
		new Thread(() -> {
			server = new BFTWalletServer(id, byzantine);
		}).start();
	}

	@Override
	public byte[] transfer(String request) {
		TransferRequest req = new GsonBuilder().create().fromJson(request, TransferRequest.class);
		return wallet.transfer(req.deserialize(), req.getNonce());
	}

	@Override
	public byte[] atomicTransfer(String request) {
		AtomicTransferRequest req = new GsonBuilder().create().fromJson(request, AtomicTransferRequest.class);
		return wallet.atomicTransfer(req.deserialize(), req.getNonce());
	}

	@Override
	public byte[] balance(String request) {
		BalanceRequest req = new GsonBuilder().create().fromJson(request, BalanceRequest.class);
		return wallet.balance(req.who, req.getNonce());
	}

	@Override
	public byte[] ledger(String request) {
		try {
		System.out.println(request);
		return wallet.ledger(new GsonBuilder().create().fromJson(request, LedgerRequest.class).getNonce());
		} catch(Exception e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	@Override
	public byte[] putOrderPreservingInt(String request) {
		PutOrderPreservingRequest req = new GsonBuilder().create().fromJson(request, PutOrderPreservingRequest.class);
		System.out.println(request);
		return wallet.putOrderPreservingInt(req.id, req.value, req.getNonce());
	}

	@Override
	public byte[] getOrderPreservingInt(String request) {
		GetOrderPreservingRequest req = new GsonBuilder().create().fromJson(request, GetOrderPreservingRequest.class);
		return wallet.getOrderPreservingInt(req.id, req.getNonce());
	}

	@Override
	public byte[] getBetween(String request) {
		GetBetweenOrderPreservingRequest req = new GsonBuilder().create().fromJson(request, GetBetweenOrderPreservingRequest.class);
		return wallet.getBetween(req.k1, req.k2, req.getNonce());
	}

}
