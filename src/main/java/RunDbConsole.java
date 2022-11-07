import org.apache.derby.tools.ij;
import java.io.IOException;


public class RunDbConsole
{
    public static void main(String[] args)
    {
        try
        {
            // connect 'jdbc:derby:ChatClientDb_skB;create=true';
            ij.main(args);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
