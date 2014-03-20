package io.github.alshain01.flags.exception;

/**
 * Exception that indicates a subdivision area was used that had a null underlying instance.
 */
public class InvalidSubdivisionException extends NullPointerException {
    public InvalidSubdivisionException() {
        super("The cuboid system returned a null parent area.");
    }
}
