package bft;

import rest.DistributedWallet;
import rest.entities.AbstractRestRequest;
import rest.entities.AtomicTransferRequest;
import rest.entities.BalanceRequest;
import rest.entities.TransferRequest;

public class BFTReplicatedWallet implements DistributedWallet {

	@SuppressWarnings("unused")
	private BFTWalletServer server;
	private BFTWalletClient wallet;	

	public BFTReplicatedWallet(int id) {
		this(id, false);
	}
	
	public BFTReplicatedWallet(int id, boolean byzantine) {
		wallet = new BFTWalletClient(id);
		new Thread( () -> {server = new BFTWalletServer(id, byzantine); }).start();
	}

	@Override
	public byte[] transfer(TransferRequest request) {
		return wallet.transfer(request.deserialize(), request.getNonce());
	}
	
	@Override
	public byte[] atomicTransfer(AtomicTransferRequest request) {
		return wallet.atomicTransfer(request.deserialize(), request.getNonce());
	}

	@Override
	public byte[] balance(BalanceRequest request) {
		return wallet.balance(request.who, request.getNonce());
	}

	@Override
	public byte[] ledger(AbstractRestRequest request) {
		return wallet.ledger(request.getNonce());
	}

}
