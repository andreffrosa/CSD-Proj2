package rest;

import java.io.Console;

import wallet.Wallet;

public class WalletInteractiveClient {
	
	public static void main(String[] args) {
		
		Wallet wallet = new RESTWalletClient("https://localhost:8080/");
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
			
			int cmd = Integer.parseInt(console.readLine("Option: "));
			
			switch (cmd) {
				case 0:
					exit = true;
					break;
				case 1:
					//System.out.println("Putting value in the map");
					who = console.readLine("\twho: ");
					amount = Integer.parseInt(console.readLine("\tamount: "));
					balance = wallet.createMoney(who, amount);
					System.out.println("-> Balance: " + balance);
					break;
				case 2:
					//System.out.println("Reading value from the map");
					String from = console.readLine("from: ");
					String to = console.readLine("to: ");
					amount = Integer.parseInt(console.readLine("amount: "));
					boolean status =  wallet.transfer(from, to, amount);
					System.out.println("-> Status: " + status);
					break;
				case 3:
					//System.out.println("Removing value in the map");
					who = console.readLine("who: ");
					balance =  wallet.currentAmount(who);
					System.out.println("-> Balance: " + balance);
					break;
				default:
					break;
			}
			
			System.out.println("");
		}
	}

}
