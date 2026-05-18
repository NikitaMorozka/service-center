package ru.servicecenter.client.util;

import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class FormValidator {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?7?[-\\s()]?\\d{3}[-\\s()]?\\d{3}[-\\s()]?\\d{2}[-\\s()]?\\d{2}$|^[89]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");
    private static final Pattern DEVICE_TEXT_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s\\-_.]{2,100}$");
    private static final Pattern SERIAL_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\-_.]{0,100}$");

    private FormValidator() {
    }

    public static boolean check(Optional<String> error, Label messageLabel) {
        if (error.isPresent()) {
            UiMessages.showError(messageLabel, error.get());
            return false;
        }
        return true;
    }

    public static boolean check(Optional<String> error, Consumer<String> showError) {
        if (error.isPresent()) {
            showError.accept(error.get());
            return false;
        }
        return true;
    }

    public static Optional<String> required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return Optional.of(fieldName + " обязательно для заполнения");
        }
        return Optional.empty();
    }

    public static Optional<String> fullName(String value) {
        Optional<String> req = required(value, "ФИО");
        if (req.isPresent()) {
            return req;
        }
        String trimmed = value.trim();
        if (trimmed.length() < 3 || trimmed.length() > 200) {
            return Optional.of("ФИО: от 3 до 200 символов");
        }
        if (!trimmed.matches("^[\\p{L}\\s\\-'.]{3,200}$")) {
            return Optional.of("ФИО: только буквы, пробелы и дефис");
        }
        return Optional.empty();
    }

    public static Optional<String> phone(String value) {
        Optional<String> req = required(value, "Телефон");
        if (req.isPresent()) {
            return req;
        }
        String digits = value.replaceAll("\\D", "");
        if (digits.length() < 10 || digits.length() > 11) {
            return Optional.of("Телефон: укажите 10–11 цифр (например +79991234567)");
        }
        if (!PHONE_PATTERN.matcher(value.trim()).matches()) {
            return Optional.of("Телефон: некорректный формат");
        }
        return Optional.empty();
    }

    public static Optional<String> emailOptional(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        if (!EMAIL_PATTERN.matcher(value.trim()).matches()) {
            return Optional.of("Email: некорректный формат");
        }
        if (value.length() > 200) {
            return Optional.of("Email: не более 200 символов");
        }
        return Optional.empty();
    }

    public static Optional<String> moneyOptional(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return moneyRequired(value, fieldName);
    }

    public static Optional<String> moneyRequired(String value, String fieldName) {
        Optional<String> req = required(value, fieldName);
        if (req.isPresent()) {
            return req;
        }
        String normalized = value.trim().replace(',', '.');
        if (!normalized.matches("\\d+(\\.\\d{1,2})?")) {
            return Optional.of(fieldName + ": укажите число (до 2 знаков после запятой)");
        }
        BigDecimal amount = new BigDecimal(normalized);
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return Optional.of(fieldName + ": сумма не может быть отрицательной");
        }
        if (amount.compareTo(new BigDecimal("9999999.99")) > 0) {
            return Optional.of(fieldName + ": слишком большая сумма");
        }
        return Optional.empty();
    }

    public static Optional<String> deviceField(String value, String fieldName) {
        Optional<String> req = required(value, fieldName);
        if (req.isPresent()) {
            return req;
        }
        if (!DEVICE_TEXT_PATTERN.matcher(value.trim()).matches()) {
            return Optional.of(fieldName + ": 2–100 символов (буквы, цифры, дефис)");
        }
        return Optional.empty();
    }

    public static Optional<String> serialOptional(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        if (!SERIAL_PATTERN.matcher(value.trim()).matches()) {
            return Optional.of("Серийный номер: некорректные символы");
        }
        return Optional.empty();
    }

    public static Optional<String> problemDescription(String value) {
        Optional<String> req = required(value, "Описание неисправности");
        if (req.isPresent()) {
            return req;
        }
        String trimmed = value.trim();
        if (trimmed.length() < 10) {
            return Optional.of("Описание неисправности: минимум 10 символов");
        }
        if (trimmed.length() > 2000) {
            return Optional.of("Описание неисправности: не более 2000 символов");
        }
        return Optional.empty();
    }

    public static Optional<String> diagnosisOptional(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        if (value.trim().length() > 2000) {
            return Optional.of("Диагноз: не более 2000 символов");
        }
        return Optional.empty();
    }

    public static Optional<String> username(String value) {
        Optional<String> req = required(value, "Логин");
        if (req.isPresent()) {
            return req;
        }
        if (!USERNAME_PATTERN.matcher(value.trim()).matches()) {
            return Optional.of("Логин: 3–50 символов (латиница, цифры, ._-)");
        }
        return Optional.empty();
    }

    public static Optional<String> password(String value, boolean requiredOnCreate) {
        if (value == null || value.isBlank()) {
            return requiredOnCreate
                    ? Optional.of("Пароль обязателен для нового пользователя")
                    : Optional.empty();
        }
        if (value.length() < 6 || value.length() > 100) {
            return Optional.of("Пароль: от 6 до 100 символов");
        }
        return Optional.empty();
    }

    public static Optional<String> role(String value) {
        return required(value, "Роль");
    }

    public static Optional<String> status(String value) {
        return required(value, "Статус");
    }
}
