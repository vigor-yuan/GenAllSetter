package utils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CodeUtils {

    private static Map<String, String> typeGeneratedMap = new HashMap<String, String>() {
        {
            put("boolean", "false");
            put("java.lang.Boolean", "false");
            put("int", "0");
            put("byte", "(byte)0");
            put("java.lang.Byte", "(byte)0");
            put("java.lang.Integer", "0");
            put("java.lang.String", "\"\"");
            put("java.math.BigDecimal", "new BigDecimal(\"0\")");
            put("java.lang.Long", "0L");
            put("long", "0L");
            put("short", "(short)0");
            put("java.lang.Short", "(short)0");
            put("java.util.Date", "new Date()");
            put("float", "0.0F");
            put("java.lang.Float", "0.0F");
            put("double", "0.0D");
            put("java.lang.Double", "0.0D");
            put("java.lang.Character", "\'\'");
            put("char", "\'\'");
            put("java.time.LocalDateTime", "LocalDateTime.now()");
            put("java.time.LocalDate", "LocalDate.now()");
            put("java.util.Optional", "Optional.empty");
        }
    };

    public static Optional<String> getDefaultValue(String className) {
        if (StringUtils.isBlank(className)) {
            return Optional.empty();
        }
        //普通类型
        //如果是List,Map,Collection，也要特殊处理
        return Optional.ofNullable(typeGeneratedMap.get(className));
    }
}
