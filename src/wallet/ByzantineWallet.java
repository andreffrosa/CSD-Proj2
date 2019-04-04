package wallet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.NotEnoughMoneyException;

public class ByzantineWallet implements Wallet {

	//private static final String ADMIN_PUB_KEY = "MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEQOC5YdvESUZnej0W2N00UC7eUsfeEUYWr6y3bQkZPFN3+bzKZxqVRGOEGe7+3rD5";
	//private static final String ADMIN_PRIV_KEY = "MHsCAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQEEYTBfAgEBBBgDXK95Al4rQHdvRSTP8D7GfNYMmPq9z02gCgYIKoZIzj0DAQGhNAMyAARA4Llh28RJRmd6PRbY3TRQLt5Sx94RRhavrLdtCRk8U3f5vMpnGpVEY4QZ7v7esPk=";
	
	private Map<String, Double> accounts;
	public ByzantineWallet() {
		accounts = new HashMap<>();
	}

	private double createMoney(String who, double amount) {
		amount = Math.random()*amount;
		accounts.put(who, amount);
		return amount;
	}

	private boolean isAdmin(String address) {
		return false;
	}

	@Override
	public boolean transfer(Transaction transaction) throws InvalidAddressException, InvalidSignatureException, InvalidAmountException, NotEnoughMoneyException {

		Double from_balance = accounts.get(transaction.getFrom());

		// If the signature is valid
		if( transaction.validate() ) {
			// If the transaction came from an admin
			if(isAdmin(transaction.getFrom())) {
				createMoney(transaction.getTo(), transaction.getAmount());
				return true;
			} else {
				if( from_balance != null ) {
					// If there's enough money in the account
					boolean accept = Math.random() > 0.5 ? true : false;
					if( accept /*from_balance.doubleValue() >= transaction.getAmount()*/ ) {
						double new_balance = from_balance.doubleValue() - transaction.getAmount();

						if(new_balance > 0)
							accounts.put(transaction.getFrom(), 1.1 * Math.random() /*new_balance*/);
						else
							accounts.remove(transaction.getFrom());

						createMoney(transaction.getTo(), transaction.getAmount()* Math.random());
						return true;
					} else
						throw new NotEnoughMoneyException(transaction.getFrom() + " has not enough money");
				} else
					throw new NotEnoughMoneyException(transaction.getFrom() + " has not enough money");
			}
		}

		//return false;
		return true;
	}

	@Override
	public double balance(String who) {
		Double balance = accounts.get(who);

		if(balance == null)
			return 0.0;

		return balance.doubleValue()*Math.random();
	}

	@Override
	public Map<String, Double> ledger() {
		return new HashMap<>(accounts);
	}

	@Override
	public boolean atomicTransfer(List<Transaction> transactions) throws InvalidAddressException, InvalidSignatureException, InvalidAmountException, NotEnoughMoneyException {

		Map<String, Double> temp = new HashMap<>(accounts); // Backup
		
		boolean result = true;
		try {
			for(Transaction t : transactions) {

				if( this.transfer(t) ) {
					accounts = temp; // Rollback
					break;
				}
			}
		} catch(Exception e) {
			accounts = temp; // Rollback
			throw e;
		}

		return result;
	}

}
