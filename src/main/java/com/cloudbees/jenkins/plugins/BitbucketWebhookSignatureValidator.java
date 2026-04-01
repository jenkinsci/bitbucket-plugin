package com.cloudbees.jenkins.plugins;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import edu.umd.cs.findbugs.annotations.NonNull;

final class BitbucketWebhookSignatureValidator {

    private BitbucketWebhookSignatureValidator() {
    }

    static boolean isValidSignature(@NonNull String header, @NonNull String secret, @NonNull byte[] payload) {
        int separator = header.indexOf('=');
        if (separator <= 0 || separator == header.length() - 1) {
            LOGGER.log(Level.FINE, "Bitbucket webhook signature header is malformed");
            return false;
        }

        String method = header.substring(0, separator).trim().toLowerCase(Locale.ROOT);
        String signature = header.substring(separator + 1).trim();
        if (signature.isEmpty()) {
            LOGGER.log(Level.FINE, "Bitbucket webhook signature header does not contain a signature value");
            return false;
        }

        String algorithm = getMacAlgorithm(method);
        if (algorithm == null) {
            LOGGER.log(Level.FINE, "Bitbucket webhook signature uses unsupported method: {0}", method);
            return false;
        }

        LOGGER.log(Level.FINER, "Validating Bitbucket webhook signature using method: {0}", method);
        byte[] expected = computeHmac(algorithm, secret, payload);
        byte[] actual = decodeHex(signature);
        if (actual == null) {
            LOGGER.log(Level.FINE, "Bitbucket webhook signature is not valid hexadecimal");
            return false;
        }

        boolean matches = MessageDigest.isEqual(expected, actual);
        if (!matches) {
            LOGGER.log(Level.FINE, "Bitbucket webhook signature did not match the computed HMAC");
        } else {
            LOGGER.log(Level.FINE, "Bitbucket webhook HMAC secret validated successfully");
        }
        return matches;
    }

    static String createSignatureHeader(@NonNull String method, @NonNull String secret, @NonNull String payload) {
        String normalizedMethod = method.trim().toLowerCase(Locale.ROOT);
        String algorithm = getMacAlgorithm(normalizedMethod);
        if (algorithm == null) {
            throw new IllegalArgumentException("Unsupported signature method: " + method);
        }

        byte[] digest = computeHmac(algorithm, secret, payload.getBytes(StandardCharsets.UTF_8));
        return normalizedMethod + "=" + toHex(digest);
    }

    private static String getMacAlgorithm(String method) {
        return switch (method) {
            case "sha1" -> "HmacSHA1";
            case "sha256" -> "HmacSHA256";
            case "sha384" -> "HmacSHA384";
            case "sha512" -> "HmacSHA512";
            default -> null;
        };
    }

    private static byte[] computeHmac(String algorithm, String secret, byte[] payload) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm));
            return mac.doFinal(payload);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not compute webhook signature", e);
        }
    }

    private static byte[] decodeHex(String value) {
        if ((value.length() & 1) != 0) {
            return null;
        }

        byte[] result = new byte[value.length() / 2];
        for (int i = 0; i < value.length(); i += 2) {
            int high = Character.digit(value.charAt(i), 16);
            int low = Character.digit(value.charAt(i + 1), 16);
            if (high < 0 || low < 0) {
                return null;
            }
            result[i / 2] = (byte) ((high << 4) + low);
        }
        return result;
    }

    private static String toHex(byte[] value) {
        StringBuilder builder = new StringBuilder(value.length * 2);
        for (byte b : value) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketWebhookSignatureValidator.class.getName());
}
