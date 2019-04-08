package wallet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.NotEnoughMoneyException;

public class ByzantineWallet implements Wallet {

	public ByzantineWallet() {
	}

	@Override
	public boolean transfer(Transaction transaction)
			throws InvalidAddressException, InvalidSignatureException, InvalidAmountException, NotEnoughMoneyException {
		return Math.random() > 0.5;
	}

	@Override
	public double balance(String who) {
		return Math.random() * Double.MAX_VALUE;
	}

	@Override
	public Map<String, Double> ledger() {
		return new HashMap<>();
	}

	@Override
	public boolean atomicTransfer(List<Transaction> transactions)
			throws InvalidAddressException, InvalidSignatureException, InvalidAmountException, NotEnoughMoneyException {
		return Math.random() > 0.5;
	}

}
