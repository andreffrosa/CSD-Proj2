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
			accounts.put(who, amount);
			return amount;
		} else {
			accounts.put(who, old + amount);
			return old + amount;
		}
	}

	@Override
	public boolean transfer(String from, String to, int amount) {
		Integer from_balance = accounts.get(from);
		
		if( from_balance < amount || from_balance == null )
			return false;
		
		accounts.put(from, from_balance - amount);
		createMoney(to, amount);
		return true;
	}

	@Override
	public int currentAmount(String who) {
		Integer balance = accounts.get(who);
		
		if(balance == null)
			 return 0;
		
		return balance.intValue();
	}

}
