package common;
/*classe base para todas as mensagens*/
import java.io.Serializable;

public abstract class NetworkMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String type;
    
    public String getType() {
        return type;
    }
}