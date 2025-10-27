package com.gestion.eventos.Security;

import java.util.regex.Pattern;

public class SqlInjectionValidator {

    // Lista básica de patrones sospechosos
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(\\b(SELECT|INSERT|DELETE|UPDATE|DROP|UNION|EXEC|ALTER|CREATE|TRUNCATE)\\b|--|;|'|\\|\\||\\*|#)",
            Pattern.CASE_INSENSITIVE);

    /**
     * Verifica si el texto contiene posibles intentos de inyección SQL.
     * 
     * @param input Texto a validar
     * @return true si se detecta un patrón sospechoso
     */
    public static boolean contieneInyeccion(String input) {
        if (input == null) return false;
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }
}
