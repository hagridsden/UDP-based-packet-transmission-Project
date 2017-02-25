package projectPackage;

public class Acknowledgment 
{

	private byte packetType;
	private byte[] acknowledgementNumber = new byte[4];
	private byte[] integrityCheck = new byte[4];
	
	public Acknowledgment(byte newPacketType, byte[] newAckNumber,byte[] newIntegrityCheck)
	{
		packetType = newPacketType;
		for(int i=0;i<4;i++)
		{
			acknowledgementNumber[i]=newAckNumber[i];
		}
		for(int i=0;i<4;i++)
		{
			integrityCheck[i] = newIntegrityCheck[i];
		}
	}
	public byte getPacketType()
	{
		return packetType;
	}
	public byte[] getAckNumber()
	{
		return acknowledgementNumber;
	}
	public int getAckNumberDec()
	{
		int ACK;
		int one=acknowledgementNumber[0] > 0 ? acknowledgementNumber[0]:acknowledgementNumber[0]+256;
		int two=acknowledgementNumber[1] > 0 ? acknowledgementNumber[1]:acknowledgementNumber[1]+256;
		int three=acknowledgementNumber[2] > 0 ? acknowledgementNumber[2]:acknowledgementNumber[2]+256;
		int four=acknowledgementNumber[3] > 0 ? acknowledgementNumber[3]:acknowledgementNumber[3]+256;
		
		ACK = (one<<24)+(two<<16)+(three<<8)+(four);
		return ACK;
	}
	public byte[] getIntegrityCheck()
	{
		return integrityCheck;
	}
	public void setPacketType(byte newPacketType)
	{
		packetType = newPacketType;
	}
	public void setAckNumber(byte[] newAckNumber)
	{
		for(int i=0;i<4;i++)
		{
			acknowledgementNumber[i]=newAckNumber[i];
		}
	}
	public void setIntegrityCheck(byte[] newIntegrityCheck)
	{
		for(int i=0;i<4;i++)
		{
			integrityCheck[i] = newIntegrityCheck[i];
		}
	}
	
}
