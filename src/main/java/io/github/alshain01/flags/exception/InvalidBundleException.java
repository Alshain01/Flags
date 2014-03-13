package io.github.alshain01.flags.exception;

/*
 * Exception that indicates an area was used that had a null underlying instance.
 */
public class InvalidBundleException extends IllegalArgumentException {
    public InvalidBundleException() {
        super("The provided bundle name does not exist.");
    }
}
