package utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CodeUtils {

    private static List<String> staticTypes =
            ImmutableList.of("boolean", "byte", "int", "short", "long", "float", "double", "char");

    private static Map<String, String> defaultValueMap = ImmutableMap.<String, String>builder()
            .put("boolean", "false")
            .put("java.lang.Boolean", "false")
            .put("int", "0")
            .put("byte", "(byte)0")
            .put("java.lang.Byte", "(byte)0")
            .put("java.lang.Integer", "0")
            .put("java.lang.String", "\"\"")
            .put("java.math.BigDecimal", "new BigDecimal(\"0\")")
            .put("java.lang.Long", "0L")
            .put("long", "0L")
            .put("short", "(short)0")
            .put("java.lang.Short", "(short)0")
            .put("java.util.Date", "new Date()")
            .put("float", "0.0F")
            .put("java.lang.Float", "0.0F")
            .put("double", "0.0D")
            .put("java.lang.Double", "0.0D")
            .put("java.lang.Character", "\'\'")
            .put("char", "\'\'")
            .put("java.time.LocalDateTime", "LocalDateTime.now()")
            .put("java.time.LocalDate", "LocalDate.now()")
            .put("java.time.OffsetDateTime", "OffsetDateTime.now()")
            .put("java.util.Optional", "Optional.empty()")
            .put("java.util.List", "new ArrayList()")
            .put("java.util.ArrayList", "new ArrayList()")
            .put("java.util.Collection", "new ArrayList()")
            .put("java.util.Set", "new HashSet()")
            .put("java.util.HashSet", "new HashSet()")
            .put("java.util.Map", "new HashMap()")
            .put("java.util.HashMap", "new HashMap()")
            .build();

    private static Map<String, String> defaultImports = ImmutableMap.<String, String>builder()
            .put("java.util.List", "java.util.ArrayList")
            .put("java.util.Set", "java.util.HashSet")
            .put("java.util.Map", "java.util.HashMap")
            .build();

    /**
     * Get the default implementation and package through the class declaration path
     *
     * @param packagePath
     * @return
     */
    public static Optional<Pair<String, String>> getDefaultValueAndDefaultImport(String packagePath) {
        if (StringUtils.isBlank(packagePath)) {
            return Optional.empty();
        }
        String defaultValue = defaultValueMap.get(packagePath);
        if (StringUtils.isBlank(defaultValue)) {
            return Optional.empty();
        }
        String defaultImport = Optional.ofNullable(defaultImports.get(packagePath)).orElse(packagePath);
        return Optional.of(Pair.of(defaultValue, defaultImport));
    }

    public static boolean isNeedToDeclareClasses(String packagePath) {
        return !(packagePath.startsWith("java.lang") || staticTypes.contains(packagePath));
    }
}
