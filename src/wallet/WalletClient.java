package wallet;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class WalletClient {
	
	private Map<String, Entry<String, Double>> addresses; // PK -> (SK, €)
	
	
	
	public WalletClient() {
		addresses = new HashMap<>();
	}
	
	public void transfer(String to, double amount) {
		
		// agrupar €€ suficiente de vários endereços
		List<Transaction> transactions = new LinkedList<>();
		
		double current_amount = 0;
		for( Entry<String, Entry<String, Double>> e : addresses.entrySet() ) {
			if( current_amount < amount ) {
				String from = e.getKey();
				double balance = e.getValue().getValue();
				double temp_amount = Math.min(amount, balance);
				String privateKey = e.getValue().getKey();
				
				transactions.add(new Transaction(from, to, temp_amount, privateKey));
				current_amount += temp_amount;
			} else
				break;
		}
		
		if( current_amount < amount ) {
			throw new RuntimeException("Not enough money");
		}
		
		// Fazer pedido REST enviando a lista de transações e esperar a resposta
	}
	
	public String getKeyToRcv() {
		
		// Gerar novo par de chaves
		String publicKey = "";
		String privateKey = "";
		
		// Adicionar à lista de pares de chaves
		addresses.put(publicKey, new AbstractMap.SimpleEntry<String, Double>(privateKey, 0.0));
		
		return publicKey;
	}
	
	public double getBalance() {
		double balance = 0;
		for( Entry<String, Entry<String, Double>> e : addresses.entrySet() ) {
			balance += e.getValue().getValue();
		}
		
		return balance;
	}
	
	public Map<String, Entry<String, Double>> getAddresses() {
		return null; // TODO
	}
}
