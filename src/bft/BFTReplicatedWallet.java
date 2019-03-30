package bft;


import rest.DistributedWallet;

public class BFTReplicatedWallet implements DistributedWallet {

	BFTWalletServer server;
	DistributedWallet wallet;
	int id;

	public BFTReplicatedWallet(int id) {
		wallet = new BFTWalletClient(id /*+ 1000*/);
		new Thread( () -> {server = new BFTWalletServer(id); }).start(); // TODO: Assim ou pode ter o mesmo id?
		
		this.id = id;
	}

	@Override
	public BFTReply createMoney(String who, int amount) {
		return wallet.createMoney(who, amount);
	}

	@Override
	public BFTReply transfer(String from, String to, int amount) {
		return wallet.transfer(from, to, amount);
	}

	@Override
	public BFTReply currentAmount(String who) {
		return wallet.currentAmount(who); // TODO: pedir directamente ao server?
	}

}
