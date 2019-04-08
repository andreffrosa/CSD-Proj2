package wallet.operations;

public class WalletOperationResult<T> {

	private T result;
	private String op_hash;

	public WalletOperationResult() {
	}

	public WalletOperationResult(T result, String op_hash) {
		this.result = result;
		this.op_hash = op_hash;
	}

	public T getResult() {
		return result;
	}

	public String getOpHash() {
		return op_hash;
	}

	public boolean isValid(WalletOperation op) {
		return op.getHash().equals(op_hash);
	}

}
