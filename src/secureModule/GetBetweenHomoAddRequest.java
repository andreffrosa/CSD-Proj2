package secureModule;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import wallet.GetBetweenOP;

public class GetBetweenHomoAddRequest {

	public Map<String, BigInteger> homo_add_variables;
	public List<GetBetweenOP> ops;

	public GetBetweenHomoAddRequest(Map<String, BigInteger> homo_add_variables, List<GetBetweenOP> ops) {
		this.homo_add_variables = homo_add_variables;
		this.ops = ops;
	}

}
