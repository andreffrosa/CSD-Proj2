package bft;


import wallet.Wallet;

public class BFTReplicatedWallet implements Wallet {

	BFTWalletServer server;
	Wallet wallet;
	int id;

	public BFTReplicatedWallet(int id) {
		wallet = new BFTWalletClient(id /*+ 1000*/);
		new Thread( () -> {server = new BFTWalletServer(id); }).start(); // TODO: Assim ou pode ter o mesmo id?
		
		this.id = id;
	}

	@Override
	public int createMoney(String who, int amount) {
		return wallet.createMoney(who, amount);
	}

	@Override
	public boolean transfer(String from, String to, int amount) {
		return wallet.transfer(from, to, amount);
	}

	@Override
	public int currentAmount(String who) {
		return wallet.currentAmount(who); // TODO: pedir directamente ao server?
	}

}
