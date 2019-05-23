package utils;

import java.math.BigInteger;

import wallet.ConditionalOperation;
import wallet.exceptions.InvalidOperationException;

public class ConditionParser {

	public static boolean evaluate(ConditionalOperation type, int a, int b) throws InvalidOperationException {
		return evaluate(type, new BigInteger(""+a), new BigInteger(""+b));
	}
	
	public static boolean evaluate(ConditionalOperation type, long a, long b) throws InvalidOperationException {
		return evaluate(type, new BigInteger(""+a), new BigInteger(""+b));
	}
	
	public static boolean evaluate(ConditionalOperation type, long a, int b) throws InvalidOperationException {
		return evaluate(type, new BigInteger(""+a), new BigInteger(""+b));
	}
	
	public static boolean evaluate(ConditionalOperation type, int a, long b) throws InvalidOperationException {
		return evaluate(type, new BigInteger(""+a), new BigInteger(""+b));
	}
	
	public static boolean evaluate(ConditionalOperation type, BigInteger a, int b) throws InvalidOperationException {
		return evaluate(type, a, new BigInteger(""+b));
	}
	
	public static boolean evaluate(ConditionalOperation type, BigInteger a, long b) throws InvalidOperationException {
		return evaluate(type, a, new BigInteger(""+b));
	}
	
	public static boolean evaluate(ConditionalOperation type, int a, BigInteger b) throws InvalidOperationException {
		return evaluate(type, new BigInteger(""+a), b);
	}
	
	public static boolean evaluate(ConditionalOperation type, long a, BigInteger b) throws InvalidOperationException {
		return evaluate(type, new BigInteger(""+a), b);
	}
	
	public static boolean evaluate(ConditionalOperation type, BigInteger a, BigInteger b) throws InvalidOperationException {
		switch(type) {
		case EQUALS: return a.equals(b);
		case NOT_EQUALS: return !a.equals(b);
		case GREATER: return a.compareTo(b) > 0;
		case GREATER_OR_EQUAL: return a.compareTo(b) >= 0;
		case LOWER: return a.compareTo(b) < 0;
		case LOWER_OR_EQUAL: return a.compareTo(b) <= 0;
		}
		
		throw new InvalidOperationException(type.name() + " is not a valid Conditional Operator!");
	}

}
