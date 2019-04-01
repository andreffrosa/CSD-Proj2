package wallet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wallet.exceptions.InvalidAddressException;
import wallet.exceptions.InvalidAmountException;
import wallet.exceptions.InvalidSignatureException;
import wallet.exceptions.NotEnoughMoneyException;

public class SimpleWallet implements Wallet {

	private static final String ADMIN_PUB_KEY = "MEkwEwYHKoZIzj0CAQYIKoZIzj0DAQEDMgAEQOC5YdvESUZnej0W2N00UC7eUsfeEUYWr6y3bQkZPFN3+bzKZxqVRGOEGe7+3rD5";
	//private static final String ADMIN_PRIV_KEY = "MHsCAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQEEYTBfAgEBBBgDXK95Al4rQHdvRSTP8D7GfNYMmPq9z02gCgYIKoZIzj0DAQGhNAMyAARA4Llh28RJRmd6PRbY3TRQLt5Sx94RRhavrLdtCRk8U3f5vMpnGpVEY4QZ7v7esPk=";
	
	private Map<String, Double> accounts;
	//private List<String> admins;

	public SimpleWallet() {
		accounts = new HashMap<>();
		//admins = loadAdmins("");
	}

	/*private List<String> loadAdmins(String path) {
		//List<String> admins = new ArrayList<>(1);

		//TODO: Fazer load dos admins

		//temp
		admins.add("god");

		return admins;
	}*/

	private double createMoney(String who, double amount) {
		Double old = accounts.get(who);
		if( old == null ) {
			if(amount > 0.0)
				accounts.put(who, amount);

			return amount;
		} else {
			if(amount > 0.0)
				accounts.put(who, old + amount);

			return old + amount;
		}
	}

	private boolean isAdmin(String address) {
		//return admins.contains(address);
		return address.equals(ADMIN_PUB_KEY);
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
					if( from_balance.intValue() >= transaction.getAmount() ) {
						double new_balance = from_balance.doubleValue() - transaction.getAmount();

						if(new_balance > 0)
							accounts.put(transaction.getFrom(), new_balance);
						else
							accounts.remove(transaction.getFrom());

						createMoney(transaction.getTo(), transaction.getAmount());
						return true;
					} else
						throw new NotEnoughMoneyException(transaction.getFrom() + " has not enough money");
				} else
					throw new NotEnoughMoneyException(transaction.getFrom() + " has not enough money");
			}
		}

		return false;
	}

	@Override
	public double balance(String who) {
		Double balance = accounts.get(who);

		if(balance == null)
			return 0.0;

		return balance.doubleValue();
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
