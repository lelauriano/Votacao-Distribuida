package common;

import java.io.Serializable;

public class Candidate implements Serializable {
    private static final long serialVersionUID = 1L;
    private int code;
    private String name;
    private int votes;

    public Candidate(int code, String name) {
        this.code = code;
        this.name = name;
        this.votes = 0;
    }

    public void addVote() { this.votes++; }
    public int getVotes() { return votes; }
    public String getName() { return name; }
    public int getCode() { return code; }
    
    @Override
    public String toString() { return name; }
}
