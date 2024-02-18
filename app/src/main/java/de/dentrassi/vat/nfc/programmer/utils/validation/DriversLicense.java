package de.dentrassi.vat.nfc.programmer.utils.validation;

import androidx.annotation.NonNull;

public final class DriversLicense {
    private DriversLicense() {
    }

    private static boolean isAsciiDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isUppercaseAsciiLetter(char c) {
        return c >= 'A' && c <= 'Z';
    }

    public static boolean isValidGermanLicenseNumber(@NonNull final String licenseNumber) {
        if (licenseNumber.length() != 11) {
            return false; // The license number should have exactly 11 characters
        }

        // Extract the components
        final String authorityKey = licenseNumber.substring(0, 4);
        final String consecutiveNumber = licenseNumber.substring(4, 9);
        final char checkDigit = licenseNumber.charAt(9);
        final char incrementDigit = licenseNumber.charAt(10);

        if (!(isAsciiDigit(incrementDigit) || isUppercaseAsciiLetter(incrementDigit))) {
            return false;
        }
        if (incrementDigit == '0') {
            // starts with 1
            return false;
        }

        // Calculate the check digit using the described algorithm
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            char character = licenseNumber.charAt(i);
            int weight = 9 - i;
            int value;

            if (Character.isDigit(character)) {
                value = Character.getNumericValue(character);
            } else {
                // Calculate the value of a letter by adding 9 to its position in the alphabet
                value = character - 'A' + 10;
            }

            sum += value * weight;
        }

        int remainder = sum % 11;
        int calculatedCheckDigit = (remainder == 10) ? 'X' : (char) (remainder + '0');

        // Check if the calculated check digit matches the provided check digit
        return checkDigit == calculatedCheckDigit;
    }
}
