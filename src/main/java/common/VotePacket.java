package common;

import java.io.Serializable;
import java.util.List;

// O "Envelope" de dados trocado via rede
public class VotePacket implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Command { LOGIN, LIST_CANDIDATES, VOTE, CONFIRM, ERROR, DISCONNECT }
    
    private Command command;
    private String message; // Para erros ou status
    private String payloadData; // Ex: CPF
    private int candidateCode; // Para o voto
    private List<Candidate> candidates; // Para enviar a lista ao cliente

    // Construtores utilitários
    public VotePacket(Command cmd) { this.command = cmd; }
    
    public VotePacket(Command cmd, String msg) { 
        this.command = cmd; 
        this.message = msg; 
    }
    
    public void setPayloadData(String data) { this.payloadData = data; }
    public String getPayloadData() { return payloadData; }
    
    public void setCandidateCode(int code) { this.candidateCode = code; }
    public int getCandidateCode() { return candidateCode; }
    
    public void setCandidates(List<Candidate> list) { this.candidates = list; }
    public List<Candidate> getCandidates() { return candidates; }
    
    public Command getCommand() { return command; }
    public String getMessage() { return message; }
}