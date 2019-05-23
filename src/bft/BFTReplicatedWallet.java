package bft;

import com.google.gson.GsonBuilder;

import rest.DistributedWallet;
import rest.entities.AtomicTransferRequest;
import rest.entities.BalanceRequest;
import rest.entities.CompareRequest;
import rest.entities.CondUpdRequest;
import rest.entities.CreateRequest;
import rest.entities.GetBetweenRequest;
import rest.entities.GetRequest;
import rest.entities.LedgerRequest;
import rest.entities.SetRequest;
import rest.entities.SumRequest;
import rest.entities.TransferRequest;
import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidOperationException;
import wallet.exceptions.InvalidTypeException;

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
		return wallet.ledger(new GsonBuilder().create().fromJson(request, LedgerRequest.class).getNonce());
	}

	@Override
	public byte[] create(String request) {
		CreateRequest req = new GsonBuilder().create().fromJson(request, CreateRequest.class);
		return wallet.create(req.type, req.id, req.initial_value, req.getNonce());
	}

	@Override
	public byte[] get(String request) throws InvalidAddressException {
		GetRequest req = new GsonBuilder().create().fromJson(request, GetRequest.class);
		return wallet.get(req.type, req.id, req.getNonce());
	}

	@Override
	public byte[] getBetween(String request) {
		GetBetweenRequest req = new GsonBuilder().create().fromJson(request, GetBetweenRequest.class);
		return wallet.getBetween(req.ops, req.getNonce());
	}

	@Override
	public byte[] set(String request) throws InvalidTypeException {
		SetRequest req = new GsonBuilder().create().fromJson(request, SetRequest.class);
		return wallet.set(req.type, req.id, req.value, req.getNonce());
	}

	@Override
	public byte[] sum(String request) throws InvalidAddressException, InvalidTypeException {
		SumRequest req = new GsonBuilder().create().fromJson(request, SumRequest.class);
		return wallet.sum(req.type, req.id, req.amount, req.arg, req.getNonce());
	}

	@Override
	public byte[] compare(String request)
			throws InvalidAddressException, InvalidTypeException, InvalidOperationException {
		CompareRequest req = new GsonBuilder().create().fromJson(request, CompareRequest.class);
		return wallet.compare(req.cond_type, req.cond_id, req.cond, req.cond_val, req.getNonce());
	}

	@Override
	public byte[] cond_upd(String request)
			throws InvalidAddressException, InvalidTypeException, InvalidOperationException {
		CondUpdRequest req = new GsonBuilder().create().fromJson(request, CondUpdRequest.class);
		return wallet.cond_upd(req.cond_type, req.cond_id, req.cond, req.cond_val, req.cond_cipheredKey, req.ops, req.getNonce());
	}

}
