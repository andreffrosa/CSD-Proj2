package rest;

import bft.InvalidRepliesException;

public interface RequestHandler<T> {
	T execute(String arg) throws InvalidRepliesException;
}