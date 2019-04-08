package rest.entities;

public class BalanceRequest extends AbstractRestRequest {

	private static final long serialVersionUID = 1L;

	public String who;

	public BalanceRequest() {
	}

	public BalanceRequest(String who) {
		super();
		this.who = who;
	}

	@Override
	public String serialize() {
		return who;
	}
}
