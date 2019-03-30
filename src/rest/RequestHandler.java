package rest;

public interface RequestHandler<T> {
	T execute(String arg);
}