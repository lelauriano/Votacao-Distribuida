package common;
/*representa os dados da eleição*/
import java.io.Serializable;
import java.util.List;

public class Election implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String question;
    private List<String> options;
    
    public Election(String question, List<String> options) {
        this.question = question;
        this.options = options;
    }
    
    // Getters
    public String getQuestion() { return question; }
    public List<String> getOptions() { return options; }
}