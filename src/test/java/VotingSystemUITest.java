import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class VotingSystemUITest {

    private VotingClientUI votingClientUI;

    @BeforeEach
    void setUp() {
        votingClientUI = new VotingClientUI();
    }

    @Test
    void testDisplayElectionQuestion() {
        String question = "Which option do you prefer?";
        votingClientUI.displayElectionQuestion(question);
        assertEquals(question, votingClientUI.getDisplayedQuestion());
    }

    @Test
    void testCollectVote() {
        String cpf = "12345678901";
        int optionIndex = 0;
        votingClientUI.collectVote(cpf, optionIndex);
        assertEquals(cpf, votingClientUI.getLastVoterCpf());
        assertEquals(optionIndex, votingClientUI.getLastVoteOptionIndex());
    }

    @Test
    void testInvalidCpfHandling() {
        String invalidCpf = "123";
        votingClientUI.collectVote(invalidCpf, 0);
        assertTrue(votingClientUI.isErrorDisplayed());
    }
}