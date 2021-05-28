package ru.netology.data;

public class VerificationInfo {
    private VerificationInfo() {
    }

    public static class VerificationCode {
        private final String code;

        public VerificationCode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public static VerificationCode getVerificationCode() {
        return new VerificationCode("12345");
    }
}
