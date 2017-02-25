package projectPackage;
import java.util.UUID;
import java.util.*;

public abstract class Key 
{
	static final private UUID secretKey = UUID.fromString("052da45a-e361-4ab0-8c4d-c13bc19b05b5");
	static final private int SeqNo=37944132;
	
	public static UUID getKey()
	{
		return secretKey;
	}
	
	public static int getSeqNo()
	{
		return SeqNo;
	}	
}
