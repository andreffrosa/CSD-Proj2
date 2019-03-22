package rest;

import java.io.Console;

import wallet.Wallet;

public class WalletInteractiveClient {
	
	public static void main(String[] args) {
		/*if(args.length < 1) {
			System.out.println("Usage: rest.client.WalletInteractiveClient <client id>");
		}*/
		
		//int clientId = Integer.parseInt(args[0]);
		Wallet wallet = new RESTWalletClient("http://localhost:8080/");
		Console console = System.console();
		
		boolean exit = false;
		
		String who;
		int amount, balance;
		
		while(!exit) {
			System.out.println("Select an option: ");
			System.out.println("0 - Terminate this client (exit)");
			System.out.println("1 - Create Money");
			System.out.println("2 - Transfer");
			System.out.println("3 - Current Money");
			/*System.out.println("4 - Retrieve the size of the map");
			System.out.println("5 - List all keys available in the table");*/
			
			int cmd = Integer.parseInt(console.readLine("Option: "));
			
			switch (cmd) {
				case 0:
					//wallet.close();
					exit = true;
					break;
				case 1:
					//System.out.println("Putting value in the map");
					who = console.readLine("who: ");
					amount = Integer.parseInt(console.readLine("amount: "));
					balance = wallet.createMoney(who, amount);
					System.out.println("Balance: " + balance);
					break;
				case 2:
					//System.out.println("Reading value from the map");
					String from = console.readLine("from: ");
					String to = console.readLine("to: ");
					amount = Integer.parseInt(console.readLine("amount: "));
					boolean status =  wallet.transfer(from, to, amount);
					System.out.println("Status: " + status);
					break;
				case 3:
					//System.out.println("Removing value in the map");
					who = console.readLine("who: ");
					balance =  wallet.currentAmount(who);
					System.out.println("Balance: " + balance);
					break;/*
				case 4:
					System.out.println("Getting the map size");
					int size = map.size();
					System.out.println("Map size: " + size);
					break;
				case 5:
					System.out.println("Getting all keys");
					Set<String> keys = map.keySet();
					System.out.println("Total number of keys found: " + keys.size());
					for (String k : keys)
						System.out.println("---> " + k);
					break;*/
				default:
					break;
			}
			
			System.out.println("");
		}
	}

}
