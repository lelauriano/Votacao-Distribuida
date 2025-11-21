// package common;
package common;

import java.io.Serializable;
import java.util.*;

/**
 * Represents an election with question, options and thread-safe vote counts.
 * Added "open" flag so the election can be closed (no more votes accepted).
 *
 * Methods are synchronized to be safe for concurrent use by multiple server threads.
 */
public class Election implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String question;
    private final List<String> options;
    private final Map<String, Integer> counts;

    // whether the election is still accepting votes
    private boolean open = true;

    public Election(String question, List<String> options) {
        this.question = question;
        this.options = Collections.unmodifiableList(new ArrayList<>(options));
        this.counts = new LinkedHashMap<>();
        for (String o : options) counts.put(o, 0);
    }

    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }

    /**
     * Thread-safe vote registration. Returns true if vote accepted.
     * Now returns false if election is closed or option invalid.
     */
    public synchronized boolean vote(String option) {
        if (!open) return false;               // do not accept votes when closed
        if (!counts.containsKey(option)) return false;
        counts.put(option, counts.get(option) + 1);
        return true;
    }

    /**
     * Returns a snapshot of the results (thread-safe).
     */
    public synchronized Map<String, Integer> getResultsSnapshot() {
        return new LinkedHashMap<>(counts);
    }

    /**
     * Close the election: after this, votes are rejected.
     */
    public synchronized void close() {
        open = false;
    }

    /**
     * Returns whether the election is still accepting votes.
     */
    public synchronized boolean isOpen() {
        return open;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Question: ").append(question).append(System.lineSeparator());
        int i = 1;
        for (String o : options) sb.append(i++).append(") ").append(o).append(System.lineSeparator());
        sb.append("Status: ").append(open ? "OPEN" : "CLOSED").append(System.lineSeparator());
        return sb.toString();
    }
}
