package com.JProgressQuest.util;

import java.util.Map;
import static java.util.Map.entry;
import java.util.regex.Pattern;

/**
 * Utilitaires pour la manipulation des chaînes de caractères.
 * Convertit les fonctions JavaScript originales vers Java avec les améliorations modernes.
 */
public final class StringUtils {
    
    // Patterns compilés pour de meilleures performances (Java 1.4+)
    private static final Pattern HTML_ESCAPE_PATTERN = Pattern.compile("[&<>]");
    
    // Map pour les conversions romaines - Map.of() (Java 9+) pour des maps immuables
    private static final Map<Integer, String> ROMAN_NUMERALS = Map.ofEntries(
        entry(1000, "M"), entry(900, "CM"), entry(500, "D"), entry(400, "CD"),
        entry(100, "C"),  entry(90, "XC"),  entry(50, "L"),  entry(40, "XL"),
        entry(10, "X"),   entry(9, "IX"),   entry(5, "V"),   entry(4, "IV"), entry(1, "I")
    );
    
    private static final Map<String, Integer> ROMAN_TO_ARABIC = Map.ofEntries(
        entry("M", 1000), entry("CM", 900), entry("D", 500), entry("CD", 400),
        entry("C", 100),  entry("XC", 90),  entry("L", 50),  entry("XL", 40),
        entry("X", 10),   entry("IX", 9),   entry("V", 5),   entry("IV", 4), entry("I", 1)
    );
    
    // Constructeur privé pour empêcher l'instanciation
    private StringUtils() {
        throw new UnsupportedOperationException("Classe utilitaire ne peut pas être instanciée");
    }
    
    /**
     * Échappe les caractères HTML spéciaux.
     * Équivalent à String.prototype.escapeHtml du JavaScript original.
     */
    public static String escapeHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // StringBuilder pour de meilleures performances
        return HTML_ESCAPE_PATTERN.matcher(input).replaceAll(match -> 
            switch (match.group()) {
                case "&" -> "&amp;";
                case "<" -> "&lt;";
                case ">" -> "&gt;";
                default -> match.group(); // Ne devrait jamais arriver avec notre pattern
            }
        );
    }
    
    /**
     * Applique un template simple avec substitution de variables.
     * Remplace les variables $nom par leurs valeurs.
     * 
     * @param template Le template avec des variables comme $variable
     * @param data Map contenant les valeurs des variables
     * @return Le template avec les variables substituées
     */
    public static String applyTemplate(String template, Map<String, Object> data) {
        if (template == null) {
            return null;
        }
        
        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String placeholder = "$" + entry.getKey();
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }
    
    /**
     * Retourne une sous-chaîne comme la fonction Copy du JavaScript.
     * 
     * @param source La chaîne source
     * @param start Position de début (1-based comme en Basic)
     * @param length Longueur à extraire
     * @return La sous-chaîne extraite
     */
    public static String copy(String source, int start, int length) {
        if (source == null || start < 1 || length < 0) {
            return "";
        }
        
        int startIndex = start - 1; // Conversion 1-based vers 0-based
        int endIndex = Math.min(startIndex + length, source.length());
        
        if (startIndex >= source.length()) {
            return "";
        }
        
        return source.substring(startIndex, endIndex);
    }
    
    /**
     * Vérifie si une chaîne commence par un préfixe.
     */
    public static boolean starts(String source, String prefix) {
        return source != null && prefix != null && source.startsWith(prefix);
    }
    
    /**
     * Vérifie si une chaîne se termine par un suffixe.
     */
    public static boolean ends(String source, String suffix) {
        return source != null && suffix != null && source.endsWith(suffix);
    }
    
    /**
     * Forme le pluriel d'un mot selon les règles anglaises.
     * Convertit la fonction Plural du JavaScript original.
     */
    public static String plural(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        
        String lower = word.toLowerCase();
        
        // Règles de pluralisation anglaise
        if (ends(lower, "y") && word.length() > 1 && !isVowel(word.charAt(word.length() - 2))) {
            return copy(word, 1, word.length() - 1) + "ies";
        } else if (ends(lower, "us")) {
            return copy(word, 1, word.length() - 2) + "i";
        } else if (ends(lower, "ch") || ends(lower, "x") || ends(lower, "s") || ends(lower, "sh")) {
            return word + "es";
        } else if (ends(lower, "f")) {
            return copy(word, 1, word.length() - 1) + "ves";
        } else if (ends(lower, "fe")) {
            return copy(word, 1, word.length() - 2) + "ves";
        } else if (ends(lower, "man")) {
            return copy(word, 1, word.length() - 2) + "en";
        } else {
            return word + "s";
        }
    }
    
    /**
     * Forme l'article indéfini approprié (a/an) avec un mot.
     */
    public static String indefinite(String word, int quantity) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        
        if (quantity == 1) {
            char firstChar = Character.toLowerCase(word.charAt(0));
            if (isVowel(firstChar)) {
                return "an " + word;
            } else {
                return "a " + word;
            }
        } else {
            return quantity + " " + plural(word);
        }
    }
    
    /**
     * Forme l'article défini avec un mot.
     */
    public static String definite(String word, int quantity) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        
        if (quantity > 1) {
            word = plural(word);
        }
        return "the " + word;
    }
    
    /**
     * Ajoute un préfixe à un mot selon un niveau de puissance.
     */
    public static String prefix(String[] prefixes, int level, String word, String separator) {
        if (prefixes == null || word == null || separator == null) {
            return word;
        }
        
        int adjustedLevel = Math.abs(level);
        if (adjustedLevel < 1 || adjustedLevel > prefixes.length) {
            return word; // En cas d'erreur, retourne le mot original
        }
        
        return prefixes[adjustedLevel - 1] + separator + word;
    }
    
    /**
     * Version surchargée avec séparateur par défaut (espace).
     */
    public static String prefix(String[] prefixes, int level, String word) {
        return prefix(prefixes, level, word, " ");
    }
    
    /**
     * Convertit un entier en chiffres romains.
     */
    public static String toRoman(int number) {
        if (number == 0) {
            return "N"; // Représentation du zéro
        }
        
        StringBuilder result = new StringBuilder();
        int remaining = Math.abs(number);
        
        if (number < 0) {
            result.append("-");
        }
        
        // Gestion des grands nombres (extensions non standard)
        while (remaining >= 10000) {
            result.append("T");
            remaining -= 10000;
        }
        
        // Conversion standard
        // On trie les clés par ordre décroissant pour garantir le bon fonctionnement de l'algorithme glouton
        var sortedKeys = ROMAN_NUMERALS.keySet().stream().sorted(java.util.Comparator.reverseOrder()).toList();
        
        for (Integer value : sortedKeys) {
            String numeral = ROMAN_NUMERALS.get(value);
            
            while (remaining >= value) {
                result.append(numeral);
                remaining -= value;
            }
        }
        
        return result.toString();
    }
    
    /**
     * Convertit des chiffres romains en entier.
     */
    public static int fromRoman(String roman) {
        if (roman == null || roman.trim().isEmpty()) {
            return 0;
        }
        
        String upperRoman = roman.trim().toUpperCase();
        
        if ("N".equals(upperRoman)) {
            return 0;
        }
        
        int result = 0;
        int i = 0;
        
        // Gestion des extensions pour les grands nombres
        while (i < upperRoman.length() && upperRoman.charAt(i) == 'T') {
            result += 10000;
            i++;
        }
        
        // Traitement des symboles composés en premier
        String[] compounds = {"CM", "CD", "XC", "XL", "IX", "IV"};
        for (String compound : compounds) {
            while (i <= upperRoman.length() - compound.length() && 
                   upperRoman.substring(i, i + compound.length()).equals(compound)) {
                result += ROMAN_TO_ARABIC.get(compound);
                i += compound.length();
            }
        }
        
        // Traitement des symboles simples
        String[] simples = {"M", "D", "C", "L", "X", "V", "I"};
        for (String simple : simples) {
            while (i < upperRoman.length() && upperRoman.charAt(i) == simple.charAt(0)) {
                result += ROMAN_TO_ARABIC.get(simple);
                i++;
            }
        }
        
        return result;
    }
    
    /**
     * Formate un temps en secondes en chaîne lisible.
     * Équivalent à la fonction RoughTime du JavaScript.
     */
    public static String formatTime(int seconds) {
        if (seconds < 120) {
            return seconds + " seconds";
        } else if (seconds < 60 * 120) {
            return (seconds / 60) + " minutes";
        } else if (seconds < 60 * 60 * 48) {
            return (seconds / 3600) + " hours";
        } else if (seconds < 60 * 60 * 24 * 60) {
            return (seconds / (3600 * 24)) + " days";
        } else if (seconds < 60 * 60 * 24 * 30 * 24) {
            return (seconds / (3600 * 24 * 30)) + " months";
        } else {
            return (seconds / (3600 * 24 * 30 * 12)) + " years";
        }
    }
    
    /**
     * Trouve la position d'une sous-chaîne (1-based comme en Basic).
     * Équivalent à la fonction Pos du JavaScript.
     */
    public static int pos(String needle, String haystack) {
        if (needle == null || haystack == null) {
            return 0;
        }
        int index = haystack.indexOf(needle);
        return index == -1 ? 0 : index + 1; // Conversion 0-based vers 1-based
    }
    
    /**
     * Divise une chaîne et retourne le champ spécifié.
     * Équivalent à la fonction Split du JavaScript.
     */
    public static String split(String source, int fieldIndex, String separator) {
        if (source == null) {
            return "";
        }
        
        String sep = separator != null ? separator : "|";
        String[] parts = source.split(Pattern.quote(sep), -1); // -1 pour inclure les champs vides
        
        if (fieldIndex < 0 || fieldIndex >= parts.length) {
            return "";
        }
        
        return parts[fieldIndex];
    }
    
    /**
     * Version surchargée avec séparateur par défaut (|).
     */
    public static String split(String source, int fieldIndex) {
        return split(source, fieldIndex, "|");
    }
    
    /**
     * Convertit en minuscules - wrapper pour la lisibilité.
     */
    public static String lowerCase(String input) {
        return input != null ? input.toLowerCase() : null;
    }
    
    /**
     * Met en forme avec la première lettre en majuscule.
     * Équivalent à ProperCase du JavaScript.
     */
    public static String properCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        return Character.toUpperCase(input.charAt(0)) + 
               (input.length() > 1 ? input.substring(1).toLowerCase() : "");
    }
    
    /**
     * Encode une URL selon les standards RFC.
     * Équivalent à UrlEncode du JavaScript mais plus moderne.
     */
    public static String urlEncode(String input) {
        if (input == null) {
            return null;
        }
        
        try {
            // URLEncoder.encode (Java 1.2+) avec UTF-8
            return java.net.URLEncoder.encode(input, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("%20", "+"); // Convention pour les espaces dans les forms
        } catch (Exception e) {
            // Fallback si l'encodage échoue
            return input.replace(" ", "+");
        }
    }
    
    /**
     * Génère une séquence ADN à partir d'un nombre.
     * Convertit la fonction ToDna du JavaScript original.
     */
    public static String toDna(String input) {
        if (input == null) {
            return "";
        }
        
        // Map des conversions - utilisation d'un record (Java 14+) serait aussi possible
        Map<Character, String> dnaCode = Map.ofEntries(
            entry('0', "AT"), entry('1', "AG"), entry('2', "AC"), entry('3', "TA"), entry('4', "TG"),
            entry('5', "TC"), entry('6', "GA"), entry('7', "GT"), entry('8', "GC"), entry('9', "CA"),
            entry(',', "CT"), entry('.', "CG")
        );
        
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            String code = dnaCode.get(c);
            
            if (code != null) {
                result.append(code);
                
                // Ajout d'un espace tous les 4 groupes (8 caractères)
                if (i > 0 && (i % 4) == 0) {
                    result.append(" ");
                }
            }
        }
        
        return result.toString();
    }
    
    /**
     * Applique des modificateurs à un nom de monstre selon sa "maladie".
     * Convertit la fonction Sick du JavaScript.
     */
    public static String applySickModifier(int level, String baseName) {
        String[] sickModifiers = {"dead", "comatose", "crippled", "sick", "undernourished"};
        int adjustedLevel = 6 - Math.abs(level);
        return prefix(sickModifiers, adjustedLevel, baseName);
    }
    
    /**
     * Applique des modificateurs d'âge à un nom de monstre.
     * Convertit la fonction Young du JavaScript.
     */
    public static String applyYoungModifier(int level, String baseName) {
        String[] youngModifiers = {"foetal", "baby", "preadolescent", "teenage", "underage"};
        int adjustedLevel = 6 - Math.abs(level);
        return prefix(youngModifiers, adjustedLevel, baseName);
    }
    
    /**
     * Applique des modificateurs de taille à un nom de monstre.
     * Convertit la fonction Big du JavaScript.
     */
    public static String applyBigModifier(int level, String baseName) {
        String[] bigModifiers = {"greater", "massive", "enormous", "giant", "titanic"};
        return prefix(bigModifiers, level, baseName);
    }
    
    /**
     * Applique des modificateurs spéciaux à un nom de monstre.
     * Convertit la fonction Special du JavaScript.
     */
    public static String applySpecialModifier(int level, String baseName) {
        String[] specialModifiers;
        String separator;
        
        if (baseName.contains(" ")) {
            specialModifiers = new String[]{"veteran", "cursed", "warrior", "undead", "demon"};
            separator = " ";
        } else {
            specialModifiers = new String[]{"Battle-", "cursed ", "Were-", "undead ", "demon "};
            separator = "";
        }
        
        return prefix(specialModifiers, level, baseName, separator);
    }
    
    /**
     * Nettoie et valide une chaîne d'entrée utilisateur.
     * Utilitaire moderne pour la sécurité.
     */
    public static String sanitizeInput(String input, int maxLength) {
        if (input == null) {
            return "";
        }
        
        // Suppression des caractères de contrôle et trim
        String cleaned = input.replaceAll("\\p{Cntrl}", "").trim();
        
        // Limitation de longueur
        if (maxLength > 0 && cleaned.length() > maxLength) {
            cleaned = cleaned.substring(0, maxLength);
        }
        
        return cleaned;
    }
    
    /**
     * Vérifie si un caractère est une voyelle.
     */
    private static boolean isVowel(char c) {
        return "aeiouAEIOUÀÈÌÒÙàèìòù".indexOf(c) != -1;
    }
    
    /**
     * Formate un nom avec la première lettre en majuscule.
     * Version thread-safe et optimisée.
     */
    public static String capitalizeName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        
        // StringBuilder pour éviter les créations de String multiples
        StringBuilder result = new StringBuilder(name.length());
        result.append(Character.toUpperCase(name.charAt(0)));
        
        if (name.length() > 1) {
            result.append(name.substring(1).toLowerCase());
        }
        
        return result.toString();
    }
    
    /**
     * Calcule la distance de Levenshtein entre deux chaînes.
     * Utile pour la comparaison de noms similaires.
     */
    public static int levenshteinDistance(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return s1 == s2 ? 0 : Math.max(
                s1 != null ? s1.length() : 0,
                s2 != null ? s2.length() : 0
            );
        }
        
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        // Initialisation
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        // Calcul de la distance
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1),     // insertion
                    dp[i - 1][j - 1] + cost // substitution
                );
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Valide qu'une chaîne est un nom valide pour un personnage.
     * 
     * @param name Le nom à valider
     * @return true si le nom est valide
     */
    public static boolean isValidCharacterName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = name.trim();
        
        // Vérifications de base
        if (trimmed.length() < 2 || trimmed.length() > 20) {
            return false;
        }
        
        // Ne doit contenir que des lettres, espaces, apostrophes et tirets
        if (!trimmed.matches("[a-zA-ZÀ-ÿ '\\-]+")) {
            return false;
        }
        
        // Ne doit pas commencer ou finir par un espace, apostrophe ou tiret
        if (trimmed.matches("^[\\s'\\-].*|.*[\\s'\\-]$")) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Version Java moderne de String.repeat() pour les versions antérieures à Java 11.
     * String.repeat() est disponible nativement depuis Java 11.
     */
    public static String repeat(String str, int times) {
        if (str == null) {
            return null;
        }
        if (times < 0) {
            throw new IllegalArgumentException("Le nombre de répétitions ne peut pas être négatif");
        }
        if (times == 0 || str.isEmpty()) {
            return "";
        }
        
        // Depuis Java 11, on pourrait simplement utiliser: return str.repeat(times);
        // Pour la compatibilité, voici une implémentation:
        StringBuilder result = new StringBuilder(str.length() * times);
        for (int i = 0; i < times; i++) {
            result.append(str);
        }
        return result.toString();
    }
    
    /**
     * Convertit une chaîne en "slug" utilisable dans les URLs.
     * Utile pour les noms de sauvegarde ou les identifiants.
     */
    public static String toSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        
        return input.trim()
            .toLowerCase()
            .replaceAll("[àáâäæãåā]", "a")
            .replaceAll("[çćč]", "c")
            .replaceAll("[èéêë]", "e")
            .replaceAll("[ìíîï]", "i")
            .replaceAll("[ñń]", "n")
            .replaceAll("[òóôöõø]", "o")
            .replaceAll("[ùúûü]", "u")
            .replaceAll("[ýÿ]", "y")
            .replaceAll("[^a-z0-9\\s\\-]", "") // Supprime les caractères spéciaux
            .replaceAll("\\s+", "-")          // Remplace les espaces par des tirets
            .replaceAll("-+", "-")            // Supprime les tirets multiples
            .replaceAll("^-|-$", "");         // Supprime les tirets en début/fin
    }
} 
