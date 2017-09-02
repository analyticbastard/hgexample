package hgexample;

import java.util.UUID;

/**
 * Created by javier on 17/09/2017.
 */
public class SimplePk implements Pk {
    private UUID id;

    private SimplePk(UUID id) {
        this.id = id;
    }

    public static SimplePk next() {
        return new SimplePk(UUID.randomUUID());
    }

    public boolean equals(Object other) {
        return other instanceof SimplePk && id.equals(((SimplePk) other).id);
    }
}
