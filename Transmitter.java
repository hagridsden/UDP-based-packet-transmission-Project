package projectPackage;
import java.io.*;
import java.util.*;
import java.net.*;

public class Transmitter
{
	static final private int MAX_PAYLOAD_SIZE = 30;
	static final private int PORT_NUM = 4000;
	static final private int PORT_NUM2 = 4001;

	//	static int k =0;	// used for TRIAL CASES
	
	public static void sendTxToRx(Message msg, InetAddress IP,DatagramSocket PCKTsocket, int PORT_NUM,DatagramSocket ACKSocket,UUID key)throws Exception
	{
		//Converting msg object to Byte stream :
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(bos);
	    dos.writeByte(msg.getPacketType());
		
	    dos.write(msg.getSeqNumber());
	    dos.writeByte(msg.getLength());
	    dos.write(msg.getData());
		
		if(msg.totalLength()-4%4!=0) 		// not multiple of 4
		{
			int diff = (msg.totalLength()-4)%4;
			byte[] paddedByteSeq = new byte[msg.totalLength()-4+diff];
			
			
			for(int i=0;i<msg.totalLength()-4;i++)
				paddedByteSeq[i]=bos.toByteArray()[i];
		
			msg.setIntegrityCheck(RC4(key,paddedByteSeq));	//encrypting using RC4
		}
		else		//already multiple of 4
		{
			byte[] paddedByteSeq = new byte[msg.totalLength()-4];
			
			for(int i=0;i<msg.totalLength()-4;i++)
				paddedByteSeq[i]=bos.toByteArray()[i];
		
			msg.setIntegrityCheck(RC4(key,paddedByteSeq));	//encrypting using RC4
			}
		/*
		 // TRIAL CASE FOR SENDING CORRUPT PACKET (SHOWN IN OUTPUT SECTION OF REPORT)
		  
	    if((msg.getSeqNumberDec()==Key.getSeqNo()+MAX_PAYLOAD_SIZE)&&(k<2)) 	// TRIAL CASE --> 2nd packet corrupt
	    {
	    	dos.write(msg.getWrongIntegrityCheck());
	    	k++;
	    	System.out.println("Sending corrupt Message Packet!");
	    }
	    else
	    */
		dos.write(msg.getIntegrityCheck());	// adding Integrity Check Bytes
				
		DatagramPacket pcktOut = new DatagramPacket(bos.toByteArray(),bos.toByteArray().length,IP,PORT_NUM);
		
		/* // TRIAL CASE FOR PACKET LOST SCENARIO (SHOWN IN OUTPUT SECTION OF REPORT)
		  if((msg.getSeqNumberDec()==Key.getSeqNo()+MAX_PAYLOAD_SIZE)&&(k==0)) 	// TRIAL CASE --> 2nd packet LOST
		    {
			  System.out.println("2nd Packet not sent (LOST)!");
		    }
		  else
		*/
		PCKTsocket.send(pcktOut);	
	}
	
	public static byte[] RC4(UUID key, byte[] paddedByteSeq)throws Exception
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(bos);
	    dos.writeLong(key.getMostSignificantBits());
	    dos.writeLong(key.getLeastSignificantBits());
	    
	    int keyLen=bos.toByteArray().length;
	    byte[] T = new byte[256];
	    byte[] S = new byte[256];
	    
	    for(int i=0;i<256;i++)
	    {
	    	S[i]=(byte)i;
	 		
	    	T[i]=bos.toByteArray()[i%keyLen];	 		
	    }
	    byte temp;
	    
	    for(int i=0,j=0;i<256;i++)
	    {
	    	if((S[i]<0)&&((T[i]<0)))
	    		j=(j+S[i]+T[i]+256+256)%256;
	    	else if(((S[i]<0)&&(T[i]>0))||((S[i]>0)&&(T[i]<0)))
	    		j=(j+S[i]+T[i]+256)%256;
	    	else
	    		j=(j+S[i]+T[i])%256;
	
	    	temp=S[i];
	    	S[i]=S[j];		//swapping
	    	S[j]=temp;
	    }
	    
	    byte[] encryptedByteSeq = new byte[paddedByteSeq.length];
	    
	    for(int p=0,i=0,j=0;p<paddedByteSeq.length;p++,i++,j++)
	    {
	    	int t=0;
	    	byte k=0;
	    	i=(i+1)%256;
	    	if(S[i] < 0)
	    	{
	    		j=(j+S[i]+256)%256;
	    	}
	    	else
	    	j=(j+S[i])%256;
	    	
	    	temp=S[i];
	    	S[i]=S[j];		//swapping
	    	S[j]=temp;
	    	if(((int)S[i]<0)&&(((int)S[j]<0)))
	    	{
	    		t=(S[i]+S[j]+256+256)%256;
	    	    
	    	}
	    	else if(((S[i]<0)&&(S[j]>=0))||((S[i]>=0)&&(S[j]<0)))
	    	{
	    		t=(S[i]+S[j]+256)%256;
	    	}
	    	else
	    		t=(S[i]+S[j])%256;
		    	k=S[t];
	    	encryptedByteSeq[p]=(byte)(0xff & (((int)paddedByteSeq[p])^(int)k)); 	// XOR with k to encrypt byte sequence
	    }
	    
	    // CREATING COMPRESSED SEQUENCE
	    
	    byte[] C = new byte[encryptedByteSeq.length];
	    
	    for(int x=0;x<4;x++)	 
	    {
	    	C[x]=(encryptedByteSeq[x]);
	    	for(int y=x+4;y<encryptedByteSeq.length;y+=4)
	    		{
	    			C[x]^=(byte) (encryptedByteSeq[y]);
	    		}
	    }
	    return C;
	}
	
	public static void main (String[] args) throws Exception
	{
		UUID secretKey=Key.getKey();
		int seqNumber=Key.getSeqNo();
		
		System.out.println("Running Transmitter");
			
		//GENERATING 500 BYTES OF DATA
		
		byte[] randomData = new byte[500];
		new Random().nextBytes(randomData);
		byte[] TXIC = new byte[4];
		DatagramSocket ACKSocket = new DatagramSocket(PORT_NUM2);
		DatagramSocket PCKTsocket = new DatagramSocket();
		Message msg;
		
		InetAddress IP = InetAddress.getByName("localhost");
		for(int i=0;i<500;)
		{	
			if(i<(500-MAX_PAYLOAD_SIZE))	//till second last packet
			{
				byte[] payload = new byte[MAX_PAYLOAD_SIZE];
				
				for(int j=0;j<MAX_PAYLOAD_SIZE;j++,i++)
					payload[j]=randomData[i];		//copying 30 bytes from 500 bytes data into payload
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
			    DataOutputStream seqByte = new DataOutputStream(bos);
			    seqByte.writeInt(seqNumber);
			  
			    msg = new Message((byte)0X55,bos.toByteArray(),(byte)payload.length,payload);	//0X55 --> 85 in decimal	
				sendTxToRx(msg,IP,PCKTsocket,PORT_NUM,ACKSocket,secretKey);
				msg.printMsg();	
				
				ACKSocket.setSoTimeout(1000); 	// 1 sec time out
				for(int j=0;j<4;j++)
				{
					TXIC[j]= msg.getIntegrityCheck()[j];
				}
			}
			else	//last packet
			{
				int size = randomData.length-i;
				byte[] payload = new byte[size];
		
				for(int j=0;j<size;j++,i++)
					payload[j]=randomData[i];
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
			    DataOutputStream seqByte = new DataOutputStream(bos);
			    seqByte.writeInt(seqNumber);
			 
			    msg = new Message((byte)0Xaa,bos.toByteArray(),(byte)payload.length,payload);	//0Xaa --> signed decimal is -86			
			    sendTxToRx(msg,IP,PCKTsocket,PORT_NUM,ACKSocket,secretKey);
				ACKSocket.setSoTimeout(1000); 	// 1 sec time out
				msg.printMsg();
				for(int j=0;j<4;j++)
				{
					TXIC[j]= msg.getIntegrityCheck()[j];
				}
			}
			
			int reTX=0;
			int flag=0,ackNumberDec=0;
			int time = 1000; 	// initial time out value
			byte[] AckReceived = new byte[9];
		
			do
			{
				try
				{
					DatagramPacket pcktIn = new DatagramPacket(AckReceived,AckReceived.length,IP,PORT_NUM2);	
					ACKSocket.receive(pcktIn);
					int firstAck = AckReceived[1] >= 0 ? AckReceived[1] : AckReceived[1] + 256;
					int secondAck = AckReceived[2] >= 0 ? AckReceived[2] : AckReceived[2] + 256;
					int thirdAck = AckReceived[3] >= 0 ? AckReceived[3] : AckReceived[3] + 256;
					int fourthAck = AckReceived[4] >= 0 ? AckReceived[4] : AckReceived[4] + 256;
					ackNumberDec = (firstAck << 24) + (secondAck << 16) + (thirdAck << 8) + fourthAck;// MISSING - Check if the ACK num is correct
					flag =0;
				
					byte [] integrityCheckAckTx = new byte[4];
					byte [] integrityCheckAckRx = new byte[4];
					byte [] paddedByteSeq = new byte [8];
				
					// Creating a padded sequence for ACK
					for(int j = 0; j < AckReceived.length-4; j++)
					{
						paddedByteSeq[j] = AckReceived[j];
					}
				
					// Calculating the integrity check for the ACK
					integrityCheckAckRx = RC4(secretKey, paddedByteSeq);
				
					// Storing the value of the integrity check received from the ACK
					for(int k = 0; k < 4; k++)
					{
						integrityCheckAckTx[k] = AckReceived[AckReceived.length-4+k];
					}
				
					// Checking if the integrity check calculated and the integrity check value received are equal
					for(int j=0;j<4;j++)
					{
						if(integrityCheckAckTx[j] != integrityCheckAckRx[j])
						{	
							flag=1;		// Integrity Check fails
						}
					}
	
					// Conditions to reject ACK 
					if((AckReceived[0]!=(byte)0xff)||(flag!=0)||(ackNumberDec-msg.getSeqNumberDec()!=msg.getLength())) 	
					{
						reTX++;
					
						if(reTX<=4)
						{
							sendTxToRx(msg,IP,PCKTsocket,PORT_NUM,ACKSocket,secretKey); 	// Re-sending packet
							time=time*2;
							ACKSocket.setSoTimeout(time); 	// 1 sec time out
							msg.printMsg();
						}
					}
					else	// ACK accepted
					{
						seqNumber=seqNumber+msg.getLength();
					}
				}
			catch(InterruptedIOException e)
			{
			// timeout - timer expired before receiving the response from the Receiver
				reTX++;
				
				if(reTX<=4)
				{
					System.out.println("\nRe-transmitting! ");
					sendTxToRx(msg,IP,PCKTsocket,PORT_NUM,ACKSocket,secretKey); 	// Re-sending packet
					time=time*2;
					ACKSocket.setSoTimeout(time); 	// 1 sec time out
					msg.printMsg();
				}
				else
				{
					System.out.println("\nTransmitter socket timeout due to communication failure! Exception object e : " + e);
					System.exit(0);
		
				}
			}
		}while((reTX<=4)&&((AckReceived[0]!=(byte)0xff)||(flag!=0)||(ackNumberDec-msg.getSeqNumberDec()!=msg.getLength())));	
			
		}// end of for loop		
	}// end of main()	
}//end of class
