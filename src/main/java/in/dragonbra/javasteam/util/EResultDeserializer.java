package in.dragonbra.javasteam.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import in.dragonbra.javasteam.enums.EResult;

import java.lang.reflect.Type;

/**
 * @author lngtr
 * @since 2018-02-20
 */
public class EResultDeserializer implements JsonDeserializer<EResult> {

    @Override
    public EResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        int code = json.getAsInt();
        return EResult.from(code);
    }
}
