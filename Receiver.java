package projectPackage;
import java.io.*;
import java.net.*;
import java.util.UUID;

public class Receiver
{
	static final private int MAX_PAYLOAD_SIZE = 30;
	static final private int PORT_NUM = 4000;
	static final private int PORT_NUM2 = 4001;

//	static int k=0;			// used for TRIAL CASES
	
	public static void sendAck(Acknowledgment newAck, InetAddress IP, DatagramSocket socket) throws Exception
	{
		// Converting the ACK into Byte Stream
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    DataOutputStream dos = new DataOutputStream(bos);
	    dos.writeByte(newAck.getPacketType());
	    dos.write(newAck.getAckNumber());
	    
	    byte [] paddedByteSeq = new byte [8];
	    for(int i = 0; i < bos.toByteArray().length; i++)
	    {
	    	paddedByteSeq[i] = bos.toByteArray()[i];
	    }
	    
	    // Integrity Check for the ACK
	    byte[] integrityCheckAck = new byte[4];
		integrityCheckAck = RC4(Key.getKey(), paddedByteSeq);
		dos.write(integrityCheckAck);
	    
	    DatagramPacket sentPacket = new DatagramPacket(bos.toByteArray(), bos.toByteArray().length, IP, PORT_NUM2);
	
	    /*
		// TRIAL CASE FOR ACK LOST SCENARIO (SHOWN IN OUTPUT SECTION OF REPORT)
	    
		  if((newAck.getAckNumberDec()==Key.getSeqNo()+2*MAX_PAYLOAD_SIZE)&&(k==0)) 	// TRIAL CASE --> 2nd ACK LOST
		    {
			  k++;
			  System.out.println("2nd ACK not sent (LOST)!");
		    }
		  else
		 */   
	    
	    socket.send(sentPacket);
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
		
		System.out.println("Running Receiver");
		
		DatagramSocket receiverSocket = new DatagramSocket(PORT_NUM);
		DatagramSocket senderSocket = new DatagramSocket();
		
		InetAddress transmitterAddress = InetAddress.getByName("localhost");
		
		int ackNumber = Key.getSeqNo();
		int k = 0;
		
		// CREATING A BYTE ARRAY TO STORE THE RECEIVED DATA
	    byte[] dataBuffer = new byte[500];
	    Acknowledgment sentAck;
	  
		while(true)
		{			
			byte[] integrityCheckTX = new byte[4];
			byte[] integrityCheckRX = new byte[4];
			byte[] receivedData = new byte[MAX_PAYLOAD_SIZE+10];
			byte[] paddedByteSeq;
			
			DatagramPacket receivePacket = new DatagramPacket(receivedData, receivedData.length);
			receiverSocket.receive(receivePacket);
				
			int flag=0;
				
			if((receivedData[5]+6)!=0) 		// not multiple of 4
			{
				int diff = (receivedData[5]+6)%4;
				paddedByteSeq = new byte[(receivedData[5]+6)+diff];
				
				
				for(int i=0;i<(receivedData[5]+6);i++)
					paddedByteSeq[i]=receivedData[i];
			}
			else		//already multiple of 4
			{
				paddedByteSeq = new byte[(receivedData[5]+6)];
				
				for(int i=0;i<(receivedData[5]+6);i++)
					paddedByteSeq[i]=receivedData[i];
			}
					
			for(int i=0;i<4;i++)
			{
				integrityCheckTX[i]=receivedData[(receivedData[5]+6)+i];	// copying Bytes received except TX Integrity Check Bytes
			}
	
			integrityCheckRX = RC4(secretKey,paddedByteSeq);
			
			//CHECKING TX AND RX INTEGRITY CHECK BYTES
			
			for(int i=0;i<4;i++)
			{
				if(integrityCheckRX[i]!=integrityCheckTX[i])
				{
					flag = 1;	// Integrity Check Fail
				}
			}
		
			// Converting the sequence number from byte to int 
			
			int firstSeq = receivedData[1] >= 0 ? receivedData[1] : receivedData[1] + 256;
			int secondSeq = receivedData[2] >= 0 ? receivedData[2] : receivedData[2] + 256;
			int thirdSeq = receivedData[3] >= 0 ? receivedData[3] : receivedData[3] + 256;
			int fourthSeq = receivedData[4] >= 0 ? receivedData[4] : receivedData[4] + 256;
			int seqNumber = (firstSeq << 24) + (secondSeq << 16) + (thirdSeq << 8) + fourthSeq;
			
			// Conditions to send ACK
			if((flag == 0) && (seqNumber == ackNumber) && (receivedData[5] <= MAX_PAYLOAD_SIZE)) 
			{
				if((receivedData[0]==(byte)0X55)||(receivedData[0]==(byte)0Xaa))
				{
					ackNumber += receivedData[5]; // Assigning the next byte number as the ACK number
					System.out.println("Recieved Correctly, Seq # : "+seqNumber+", ACK # : "+ackNumber);
					
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
				    DataOutputStream ackByte = new DataOutputStream(bos);
					ackByte.writeInt(ackNumber);
					
					sentAck = new Acknowledgment((byte)0xff, bos.toByteArray(),integrityCheckRX); // Creating ACK packet
					sendAck(sentAck, transmitterAddress, senderSocket);	// Sending ACK 
					
					// Storing the data in an array for further use
					if(receivedData[0] == (byte)0X55)
					{
					for (int i = 0; i < (receivePacket.getLength() - 10); i++) 
					{
						dataBuffer[k + i] = receivedData[i + 6]; 		
					}
					}
					
					// Last packet receives 30 bytes only (including header)
					else if(receivedData[0]==(byte)0Xaa)
					{
						for (int i = 0; i < (receivePacket.getLength() - 10); i++) 
						{
							dataBuffer[k + i] = receivedData[i + 6]; 		
						}
						break; 	// breaking while loop
					}
					k += receivedData.length - 10;
				}	// if received packet type does not match, do nothing.
			}//end of if
			else if((flag == 1) || (seqNumber != ackNumber))	//Integrity Check Failed
			{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
			    DataOutputStream ackByte = new DataOutputStream(bos);
				System.out.println("Incorrect Packet! Sending previous ACK : "+ackNumber);
			    ackByte.writeInt(ackNumber);
				
				sentAck = new Acknowledgment((byte)0xff, bos.toByteArray(),integrityCheckRX); // Creating ACK packet with previous seq number
				sendAck(sentAck, transmitterAddress, senderSocket);	// Sending ACK 
			}
			
		}// end of while loop

		System.out.println("Data Received : ");
	
		//Displaying the received byte sequence
		for(int j = 0; j < dataBuffer.length; j++)
		{
			System.out.print(dataBuffer[j]+" ");
		}
		
	} // end of main()
				
} //end of class
