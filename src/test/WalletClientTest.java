package test;

import java.util.LinkedList;
import java.util.List;

import wallet.ConditionalOperation;
import wallet.DataType;
import wallet.UpdOp;
import wallet.UpdOperation;
import wallet.client.WalletClient;

public class WalletClientTest {

	public static void main(String[] args) throws Exception {
		
		WalletClient myWallet = new WalletClient(null);
		
		myWallet.create(DataType.WALLET, "WALLET-1", 100);
		myWallet.create(DataType.WALLET, "WALLET-2", 100);
		myWallet.create(DataType.HOMO_ADD, "HOMO_ADD-1", 100);
		myWallet.create(DataType.HOMO_ADD, "HOMO_ADD-2", 100);
		myWallet.create(DataType.HOMO_OPE_INT, "OPI-1", 100);
		myWallet.create(DataType.HOMO_OPE_INT, "OPI-2", 100);
		
		myWallet.get("", 50, 150).forEach(System.out::println);
		
		System.out.println(myWallet.get(DataType.WALLET, "WALLET-1"));
		System.out.println(myWallet.get(DataType.WALLET, "WALLET-2"));
		System.out.println(myWallet.get(DataType.HOMO_ADD, "HOMO_ADD-1"));
		System.out.println(myWallet.get(DataType.HOMO_ADD, "HOMO_ADD-2"));
		System.out.println(myWallet.get(DataType.HOMO_OPE_INT, "OPI-1"));
		System.out.println(myWallet.get(DataType.HOMO_OPE_INT, "OPI-2"));
		
		myWallet.set(DataType.WALLET, "WALLET-1", 50);
		myWallet.set(DataType.WALLET, "WALLET-2", 50);
		myWallet.set(DataType.HOMO_ADD, "HOMO_ADD-1", 50);
		myWallet.set(DataType.HOMO_ADD, "HOMO_ADD-2", 50);
		myWallet.set(DataType.HOMO_OPE_INT, "OPI-1", 50);
		myWallet.set(DataType.HOMO_OPE_INT, "OPI-2", 50);
		
		System.out.println(myWallet.get(DataType.WALLET, "WALLET-1"));
		System.out.println(myWallet.get(DataType.WALLET, "WALLET-2"));
		System.out.println(myWallet.get(DataType.HOMO_ADD, "HOMO_ADD-1"));
		System.out.println(myWallet.get(DataType.HOMO_ADD, "HOMO_ADD-2"));
		System.out.println(myWallet.get(DataType.HOMO_OPE_INT, "OPI-1"));
		System.out.println(myWallet.get(DataType.HOMO_OPE_INT, "OPI-2"));
		
		myWallet.sum(DataType.WALLET, "WALLET-1", 50);
		myWallet.sum(DataType.WALLET, "WALLET-2", 50);
		myWallet.sum(DataType.HOMO_ADD, "HOMO_ADD-1", 50);
		myWallet.sum(DataType.HOMO_ADD, "HOMO_ADD-2", 50);
		myWallet.sum(DataType.HOMO_OPE_INT, "OPI-1", 50);
		myWallet.sum(DataType.HOMO_OPE_INT, "OPI-2", 50);
		
		System.out.println(myWallet.get(DataType.WALLET, "WALLET-1"));
		System.out.println(myWallet.get(DataType.WALLET, "WALLET-2"));
		System.out.println(myWallet.get(DataType.HOMO_ADD, "HOMO_ADD-1"));
		System.out.println(myWallet.get(DataType.HOMO_ADD, "HOMO_ADD-2"));
		System.out.println(myWallet.get(DataType.HOMO_OPE_INT, "OPI-1"));
		System.out.println(myWallet.get(DataType.HOMO_OPE_INT, "OPI-2"));
		
		System.out.println(myWallet.compare(DataType.WALLET, "WALLET-1", ConditionalOperation.EQUALS, 100));
		System.out.println(myWallet.compare(DataType.WALLET, "WALLET-1", ConditionalOperation.NOT_EQUALS, 100));
		System.out.println(myWallet.compare(DataType.WALLET, "WALLET-1", ConditionalOperation.LOWER, 100));
		System.out.println(myWallet.compare(DataType.WALLET, "WALLET-1", ConditionalOperation.LOWER_OR_EQUAL, 100));
		System.out.println(myWallet.compare(DataType.WALLET, "WALLET-1", ConditionalOperation.GREATER, 100));
		System.out.println(myWallet.compare(DataType.WALLET, "WALLET-1", ConditionalOperation.GREATER_OR_EQUAL, 100));
		
		System.out.println(myWallet.compare(DataType.HOMO_ADD, "HOMO_ADD-1", ConditionalOperation.EQUALS, 100));
		System.out.println(myWallet.compare(DataType.HOMO_ADD, "HOMO_ADD-1", ConditionalOperation.NOT_EQUALS, 100));
		System.out.println(myWallet.compare(DataType.HOMO_ADD, "HOMO_ADD-1", ConditionalOperation.LOWER, 100));
		System.out.println(myWallet.compare(DataType.HOMO_ADD, "HOMO_ADD-1", ConditionalOperation.LOWER_OR_EQUAL, 100));
		System.out.println(myWallet.compare(DataType.HOMO_ADD, "HOMO_ADD-1", ConditionalOperation.GREATER, 100));
		System.out.println(myWallet.compare(DataType.HOMO_ADD, "HOMO_ADD-1", ConditionalOperation.GREATER_OR_EQUAL, 100));
		
		System.out.println(myWallet.compare(DataType.HOMO_OPE_INT, "OPI-1", ConditionalOperation.EQUALS, 100));
		System.out.println(myWallet.compare(DataType.HOMO_OPE_INT, "OPI-1", ConditionalOperation.NOT_EQUALS, 100));
		System.out.println(myWallet.compare(DataType.HOMO_OPE_INT, "OPI-1", ConditionalOperation.LOWER, 100));
		System.out.println(myWallet.compare(DataType.HOMO_OPE_INT, "OPI-1", ConditionalOperation.LOWER_OR_EQUAL, 100));
		System.out.println(myWallet.compare(DataType.HOMO_OPE_INT, "OPI-1", ConditionalOperation.GREATER, 100));
		System.out.println(myWallet.compare(DataType.HOMO_OPE_INT, "OPI-1", ConditionalOperation.GREATER_OR_EQUAL, 100));
		
		List<UpdOp> ops = new LinkedList<>();
		ops.add(new UpdOp(UpdOperation.SET, DataType.WALLET, "WALLET-2", 1));
		myWallet.cond_upd(DataType.WALLET, "WALLET-1", ConditionalOperation.GREATER_OR_EQUAL, 100, ops);
		
		System.out.println(myWallet.get(DataType.WALLET, "WALLET-2"));
		
		ops = new LinkedList<>();
		ops.add(new UpdOp(UpdOperation.SUM, DataType.HOMO_ADD, "HOMO_ADD-2", 1));
		myWallet.cond_upd(DataType.HOMO_ADD, "HOMO_ADD-1", ConditionalOperation.EQUALS, 100, ops);
		
		System.out.println(myWallet.get(DataType.HOMO_ADD, "HOMO_ADD-2"));
		
		ops = new LinkedList<>();
		ops.add(new UpdOp(UpdOperation.SUM, DataType.HOMO_OPE_INT, "OPI-2", 10));
		myWallet.cond_upd(DataType.HOMO_OPE_INT, "OPI-1", ConditionalOperation.EQUALS, 100, ops);
		
		System.out.println(myWallet.get(DataType.HOMO_OPE_INT, "OPI-2"));
	}
	
}
