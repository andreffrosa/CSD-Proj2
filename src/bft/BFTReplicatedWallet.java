package bft;


import rest.DistributedWallet;
import rest.entities.AtomicTransferRequest;
import rest.entities.BalanceRequest;
import rest.entities.TransferRequest;

public class BFTReplicatedWallet implements DistributedWallet {

	BFTWalletServer server;
	BFTWalletClient wallet;

	public BFTReplicatedWallet(int id) {
		this(id, false);
	}
	
	public BFTReplicatedWallet(int id, boolean byzantine) {
		wallet = new BFTWalletClient(id);
		new Thread( () -> {server = new BFTWalletServer(id, byzantine); }).start();
	}

	@Override
	public byte[] transfer(TransferRequest request) {
		return wallet.transfer(request.deserialize());
	}
	
	@Override
	public byte[] atomicTransfer(AtomicTransferRequest request) {
		System.out.println("\noi\n");
		return wallet.atomicTransfer(request.deserialize());
	}

	@Override
	public byte[] balance(BalanceRequest request) {
		return wallet.balance(request.who);
	}

	@Override
	public byte[] ledger() {
		return wallet.ledger();
	}

}
