package org.geotools.main;

import java.util.Arrays;
import java.util.List;

public class Validator {

    public static void checkNullPointerPassed(Object... targets) {

        for (Object target : targets) {

            if (target == null) {
                throw new IllegalArgumentException(
                        "Arguments must not be null!");
            }
        }
    }

    public static void checkNotPositive(Integer target, String variableName) {

        if (target <= 0) {
            throw new IllegalArgumentException(variableName
                    + " must be positive!");
        }
    }

    public static void checkNegative(Integer target, String variableName) {

        if (target < 0) {
            throw new IllegalArgumentException(variableName
                    + " must be non negative!");
        }
    }

    public static void checkNegative(Integer... targets) {

        for (Integer target : targets) {

            if (target < 0) {
                throw new IllegalArgumentException(
                        "Some arguments must be non negative!");
            }
        }
    }
    
    public static void checkNegative(Double... targets) {

        for (Double target : targets) {

            if (target < 0.0) {
                throw new IllegalArgumentException(
                        "Some arguments must be non negative!");
            }
        }
    }
    
    public static void checkNotPositive(Double... targets) {

        for (Double target : targets) {

            if (target < 0.0) {
                throw new IllegalArgumentException(
                        "Some arguments must be positive!");
            }
        }
    }

    public static void checkNotInitialized(Object... targets) {

        for (Object target : targets) {

            if (target == null) {
                throw new IllegalStateException(
                        "Some needed variables is null. These variables must be initialized!!");
            }
        }
    }

}
