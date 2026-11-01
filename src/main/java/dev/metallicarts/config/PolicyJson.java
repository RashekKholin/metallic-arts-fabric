package dev.metallicarts.config;
import com.google.gson.*;
/** Reject Gson's implicit string/boolean coercion and fractional-to-integer truncation in host policies. */
final class PolicyJson {
 static void validate(JsonObject json,Class<?> type){for(var field:type.getRecordComponents()){var value=json.get(field.getName());if(value==null)continue;var t=field.getType();if(value.isJsonNull())throw new IllegalArgumentException("Null policy value: "+field.getName());if(t.isRecord()){if(!value.isJsonObject())throw new IllegalArgumentException("Expected object: "+field.getName());validate(value.getAsJsonObject(),t);}else if(t==boolean.class){if(!value.isJsonPrimitive()||!value.getAsJsonPrimitive().isBoolean())throw new IllegalArgumentException("Expected boolean: "+field.getName());}else if(t==int.class||t==long.class||t==double.class){if(!value.isJsonPrimitive()||!value.getAsJsonPrimitive().isNumber())throw new IllegalArgumentException("Expected number: "+field.getName());if(t==int.class)value.getAsBigDecimal().intValueExact();else if(t==long.class)value.getAsBigDecimal().longValueExact();else if(!Double.isFinite(value.getAsDouble()))throw new IllegalArgumentException("Expected finite number");}}}
 private PolicyJson(){}
}
