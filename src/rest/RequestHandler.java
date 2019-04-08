package rest;

import bft.reply.InvalidRepliesException;

public interface RequestHandler<T> {
	T execute(String arg) throws InvalidRepliesException;
}