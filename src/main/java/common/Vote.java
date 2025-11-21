package common;

import java.io.Serializable;

/**
 * Simple Vote message: client chooses an option index (1-based) or option text.
 */
public class Vote implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String option;

    public Vote(String option) {
        this.option = option;
    }

    public String getOption() { return option; }
}
