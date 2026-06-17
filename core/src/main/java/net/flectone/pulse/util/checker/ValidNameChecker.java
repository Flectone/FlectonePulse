package net.flectone.pulse.util.checker;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ValidNameChecker {

    // https://github.com/PaperMC/adventure/blob/main/5/api/src/main/java/net/kyori/adventure/text/object/PlayerHeadObjectContentsImpl.java
    public boolean check(@Nullable String name) {
        if (StringUtils.isEmpty(name)) return false;

        if (name.length() > 16) {
            return false;
        }

        // allowed ! @ # $ % ^ & * ( ) - + = . , /
        return name.chars().filter(c -> c <= 32 || c >= 126).findAny().isEmpty();
    }

}
