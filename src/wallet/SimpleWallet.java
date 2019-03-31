package wallet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Cryptography;

public class SimpleWallet implements Wallet {

	private Map<String, Double> accounts;
	private List<String> admins;

	public SimpleWallet() {
		accounts = new HashMap<>();
		admins = loadAdmins("");
	}

	private List<String> loadAdmins(String path) {
		List<String> admins = new ArrayList<>(1);

		//TODO: Fazer load dos admins

		//temp
		admins.add("god");

		return admins;
	}

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
		return admins.contains(address);
	}

	private boolean validateSignature(String from, String to, double amount, String signature) {
		byte[] message = (from + to + amount).getBytes(); // Assinar um hash ou não vale a pena?
		boolean valid = Cryptography.validateSignature(message, signature, from);

		// temp
		if(from.equals("god"))
			valid = true;

		System.out.println("Signature is " + valid);

		return valid;
	}

	@Override
	public boolean transfer(String from, String to, double amount, String signature) throws InvalidNumberException {

		if( amount < 0 )
			throw new InvalidNumberException("Transfered amount cannot be negative");

		Double from_balance = accounts.get(from);

		// If the signature is valid
		if( validateSignature(from, to,amount, signature) ) {
			// If the transaction came from an admin
			if(isAdmin(from)) {
				createMoney(to, amount);
				return true;
			} else {
				if( from_balance != null ) {
					// If there's enough money in the account
					if( from_balance.intValue() >= amount ) {
						double new_balance = from_balance.doubleValue() - amount;

						if(new_balance > 0)
							accounts.put(from, new_balance);
						else
							accounts.remove(from);

						createMoney(to, amount);
						return true;
					}
				}
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
	public boolean atomicTransfer(List<Transaction> transactions) throws InvalidNumberException {

		Map<String, Double> temp = new HashMap<>(accounts); // Backup

		int counter = 1;
		
		boolean result = true;
		try {
			for(Transaction t : transactions) {

				// Correct
				/*if( t.isValid() ) {
					result = this.transfer(t.getFrom(), t.getTo(), t.getAmount(), t.getSignature());
				} else {
					result = false;
				}*/
				
				//temp
				result = this.transfer(t.getFrom(), t.getTo(), t.getAmount(), t.getSignature());
				
				System.out.println("Transction " + counter++ + " " + result);

				if(!result) {
					accounts = temp; // Rollback
					break;
				}
			}
		} catch(InvalidNumberException e) {
			accounts = temp; // Rollback
			throw e;
		}

		return result;
	}

}
