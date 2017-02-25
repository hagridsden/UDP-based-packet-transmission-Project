package projectPackage;
import java.io.*;

public class Message 
{
	private byte packetType;
	private byte[] sequenceNumber = new byte[4];
	private byte length;
	private byte[] data;
	private byte[] integrityCheck = new byte[4];
	
	public Message(byte newPacketType, byte[] newSequenceNumber, byte newLength, byte[] newData)
	{
		packetType=newPacketType;
		for(int i=0;i<4;i++)
			sequenceNumber[i]=newSequenceNumber[i];
		length=newLength;
		data = new byte[length];
		for(int i=0;i<(int)length;i++)
		{
			data[i]=newData[i];		// check if it is deep copy <<<>>>>>
		}
	}
	
	public byte getPacketType()
	{
		return(packetType);
	}
	
	public int getSeqNumberDec()
	{
		int seq;
		int one=sequenceNumber[0] > 0 ? sequenceNumber[0]:sequenceNumber[0]+256;
		int two=sequenceNumber[1] > 0 ? sequenceNumber[1]:sequenceNumber[1]+256;
		int three=sequenceNumber[2] > 0 ? sequenceNumber[2]:sequenceNumber[2]+256;
		int four=sequenceNumber[3] > 0 ? sequenceNumber[3]:sequenceNumber[3]+256;
		
		seq = (one<<24)+(two<<16)+(three<<8)+(four);
		return seq;
	}
	
	public byte[] getSeqNumber()
	{
		return(sequenceNumber);
	}
	/*// TRIAL function
	public byte[] getWrongIntegrityCheck()
	{
		byte[] w = {4,4,4,4};
		return(w);
	}
	*/
	public byte getLength()
	{
		return(length);
	}
	
	public byte[] getData()
	{
		return(data);
	}
	
	public byte[] getIntegrityCheck()
	{
		return(integrityCheck);
	}
	
	public void setPacketType(byte newPacketType)
	{
		packetType = newPacketType;
	}
	
	public void setLength(byte newLength)
	{
		length = newLength;
	}
	
	public void setData(byte[] newData)
	{
		for(int i=0;i<length;i++)
		{
			data[i]=newData[i];		// check if it is deep copy <<<>>>>>
		}
	}
	
	public void setIntegrityCheck(byte[] newIntegrityCheck)
	{
		for(int i=0;i<4;i++)
			integrityCheck[i] = newIntegrityCheck[i];
	}	
	
	public void printMsg()
	{
		System.out.print(packetType+" ");
		
		for(int i=0;i<4;i++)
			System.out.print(sequenceNumber[i]+" ");
		
		System.out.print(length+" ");
		for(int i=0;i<length;i++)
			System.out.print(data[i]+" ");
	    
	   	for(int i=0;i<4;i++)
	   		System.out.print(integrityCheck[i]+" ");
		
		System.out.print("\n");
		
	}
	
	public int totalLength()
	{
		return(10+length); 
	}
}
