package com.github.doghere.util;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by dog on 12/16/16.
 * <p>
 * A help class to create set.
 *
 * @author dogHere@tutamail.com
 */
public class H {
    public static Set make(Object... os) {
        Set set = new HashSet<>();
        for (Object o : os) {
            set.add(o);
        }
        return set;
    }
}
