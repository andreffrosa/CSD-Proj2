package secureModule;

import java.math.BigInteger;

public class CompareRequest {

	public BigInteger v1;
	public BigInteger v2;
	public String cipheredKey;

	public CompareRequest(BigInteger v1, BigInteger v2, String cipheredKey) {
		this.v1 = v1;
		this.v2 = v2;
		this.cipheredKey = cipheredKey;
	}

}
