package io.github.alshain01.flags.exceptions;

/*
 * Exception that indicates an area was used that had a null underlying instance.
 */
public class InvalidAreaException extends NullPointerException {
    public InvalidAreaException() {
        super("The cuboid system returned a null area.");
    }
}
