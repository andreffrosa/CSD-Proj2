package bft;


import rest.DistributedWallet;
import rest.entities.BalanceRequest;
import rest.entities.TransferRequest;
import wallet.Wallet;

public class BFTReplicatedWallet implements DistributedWallet {

	BFTWalletServer server;
	BFTWalletClient wallet;

	public BFTReplicatedWallet(int id) {
		wallet = new BFTWalletClient(id);
		new Thread( () -> {server = new BFTWalletServer(id); }).start();
	}

	@Override
	public byte[] transfer(TransferRequest request) {
		return wallet.transfer(request.from, request.to, request.amount, request.signature);
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
