package wallet;

import java.util.HashMap;
import java.util.Map;

public class SimpleWallet implements Wallet {

	private Map<String, Integer> accounts;

	public SimpleWallet() {
		accounts = new HashMap<>();
	}

	@Override
	public int createMoney(String who, int amount) {
		Integer old = accounts.get(who);
		if( old == null ) {
			if(amount > 0)
				accounts.put(who, amount);

			return amount;
		} else {
			if(amount > 0)
				accounts.put(who, old + amount);

			return old + amount;
		}
	}

	@Override
	public boolean transfer(String from, String to, int amount) throws InvalidNumberException {
		
		if( amount < 0 )
			throw new InvalidNumberException("Transfered amount cannot be negative");
			
		Integer from_balance = accounts.get(from);

		if( from_balance != null ) {
			if( from_balance.intValue() >= amount ) {
				int new_balance = from_balance.intValue() - amount;

				if(new_balance > 0)
					accounts.put(from, new_balance);
				else
					accounts.remove(from);

				createMoney(to, amount);
				return true;
			}
		}

		return false;
	}

	@Override
	public int currentAmount(String who) {
		Integer balance = accounts.get(who);

		if(balance == null)
			return 0;

		return balance.intValue();
	}

}
