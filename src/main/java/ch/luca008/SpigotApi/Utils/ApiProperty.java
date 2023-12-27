package ch.luca008.SpigotApi.Utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import javax.annotation.Nullable;

public record ApiProperty(String name, String value, @Nullable String signature) {

    public Property getMojangProperty()
    {
        if(signature != null)
            return new Property(name, value, signature);
        return new Property(name, value);
    }

    public void addProperty(GameProfile profile)
    {
        profile.getProperties().put(name, getMojangProperty());
    }

}
