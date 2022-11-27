import java.lang.*;
import java.io.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;
public class virtualModem{
	public static void main(String[] param){
		(new virtualModem()).app();
	}
	public void app(){
		Modem modem;
		modem=new Modem();
		modem.setSpeed(80000);
		modem.setTimeout(2000);
		modem.open("ithaki");
		String commandEcho="E8805\r\n";
		String commandImage="M6646\r\n";
		String commandImageError="G8020\r\n";
		String commandGPS="P1386";
		String commandGPSFINAL="";
		String commandACK="Q0219\r\n";
		String commandNACK="R5356\r\n";
		int k;
		for(;;){
			try{
				k=modem.read();
				if(k==-1)break;
				System.out.print((char)k);
			}catch(Exception x){
				break;
			}
		}
		System.out.println("ECHO REQUEST...start");
		call_ECHO(modem,commandEcho);
		System.out.println("IMAGE REQUEST WITHOUT ERRORS...start");
		call_IMAGE(modem,commandImage);
		System.out.println("IMAGE REQUEST WITH ERRORS...start");
		call_IMAGE(modem,commandImageError);
		System.out.println("ACK-NACK REQUEST...start");
		call_ACKNACK(modem,commandACK,commandNACK);
		System.out.println("GPS COORDINATES...start");
		call_GPS(modem,commandGPS);
		System.out.println("Goodbye!");
		//modem.close();
	}
	public void call_ECHO(Modem modem,String command){
		int k;
		float time;
		PrintWriter echoes=null;
		try{
			echoes=new PrintWriter("EchoRequest.txt","UTF-8");
		}catch(Exception x){
			System.out.println("Exception Occured ");
			System.exit(1);
		}
		String echoPacket="";
		long tic,tac,tStart;
		tStart=System.currentTimeMillis();
		while((System.currentTimeMillis()-tStart)<4 * 60000){
			tic=System.currentTimeMillis();
			modem.write(command.getBytes());
			for(;;){
				try{
					k=modem.read();
					if(k==-1)break;
					echoPacket=echoPacket+(char)k;
					// System.out.print((char)k);
					if(echoPacket.endsWith("PSTOP")){
						tac=System.currentTimeMillis();
						time=(float)((float)(tac-tic)/1000);
						echoes.write(System.lineSeparator());
						echoes.write(Float.toString(time));
						// echoes.write(System.lineSeparator());
					}
				}catch(Exception x){
					System.exit(1);
				}
			}
			echoPacket="";
		}
		try{
			echoes.close();
		}catch(Exception x){
			System.exit(1);
		}
	}
	public void call_IMAGE(Modem modem ,String command){
		int k;
		boolean counter=false;
		OutputStream out=null;
		if(command.startsWith("M")){
			try{
				out=new FileOutputStream("IMAGE.jpg");
			}catch(Exception x){
				System.out.println("CANNOT OPEN FILE ");
				System.exit(1);
			}
		}
		if (command.startsWith("G")){
			try{
				out=new FileOutputStream("IMAGEWITHERROR.jpg");
			}catch(Exception x){
				System.out.println("CANNOT OPEN THE FILE ");
				System.exit(1);
			}
		}
		try{
			modem.write(command.getBytes());
		}catch(Exception x){
			System.exit(1);
		}
		for(;;){
			try{
				k=modem.read();
				if(k==-1)break;
				if(k==0xFF){
					for(;;){
						out.write(k);
						k=modem.read();
						if(k==0xFF){
							out.write(k);
							k=modem.read();
							if(k==0XD9){
								out.write(k);
								counter=true;
							}
						}
						if(counter) break;
					}
				}
			}catch(Exception x){
				System.exit(1);
			}
			if(counter) break;
		}
		try{
			out.close();
		}catch(Exception x){
			System.exit(1);
		}
	}
	public void call_GPS(Modem modem,String command){
		int k;
		String message=command+"R=1000099\r\n";
		OutputStream op=null;
		PrintWriter coords=null;
		boolean counter=false;
		OutputStream out=null;
		modem.close();
		Modem modem3=new Modem();
		modem3.setSpeed(80000);
		modem3.setTimeout(2000);
		modem3.open("ithaki");
		for(;;){
			try{
				k=modem3.read();
				if(k==-1)break;
			}catch(Exception x){
				System.exit(1);
			}
		}
		try{
			out=new FileOutputStream("GPSIMAGE.jpg");
		}catch(Exception x){
			System.out.println("Exception Occured");
			System.exit(1);
		}
		try{
			op=modem3.getOutputStream();
		}catch (Exception x){
			System.out.println("Exception Occured ");
			System.exit(1);
		}
		try{
			coords=new PrintWriter("GPSCOORDS.txt","UTF-8");
		}catch(Exception x){
			System.out.println("cannot create the file ");
			System.exit(1);
		}
		try{
			op.write(message.getBytes());
		}catch(Exception x){
			System.out.println("Exception Occured ");
			System.exit(1);
		}
		for(;;){
			try{
				k=modem3.read();
				if(k==-1)break;
				coords.write((char)k);
				System.out.print((char)k);
			}catch(Exception x){
				System.out.println("exception occured ");
				System.exit(1);
			}
		}
		coords.close();
		try{
			op.close();
			modem3.close();
		}catch(Exception x){
			System.out.println("Exception Occured ");
			System.exit(1);
		}
		String line="";
		BufferedReader br1=null;
		PrintWriter onlyCOORDS=null;
		try{
			onlyCOORDS=new PrintWriter("ONLYGPS.txt","UTF-8");
		}catch(Exception x){
			System.out.println("Exception Occured");
			System.exit(1);
		}
		try{
			br1=new BufferedReader(new FileReader("GPSCOORDS.txt"));
		}catch (Exception x){
			System.out.println("Exception Occured");
			System.exit(1);
		}
		try{
			while((line=br1.readLine())!=null){
				if(line.startsWith("$GPGGA")){
					onlyCOORDS.write(line);
					onlyCOORDS.write(System.lineSeparator());
				}
			}
		}catch(Exception x){
			System.exit(1);
		}
		try{
			br1.close();
		}catch(Exception x){
			System.out.println("Exception Occured ");
			System.exit(1);
		}
		try{
			onlyCOORDS.close();
		}catch(Exception x){
			System.out.println("Exception Occured");
			System.exit(1);
		}
		BufferedReader br2=null;
		line="";
		try{
			br2=new BufferedReader(new FileReader("ONLYGPS.txt"));
		}catch(Exception x){
			System.exit(1);
		}
		int Counter=0;
		double[] longtitude=new double[5];
		double[] latitude=new double[5];
		int[] time=new int[5];
		int sec,min,temp,temp2;
		String TEMP;
		String[][] data=new String[99][15];
		try{
			while((line=br2.readLine())!=null){
				if(Counter==99)break;
				data[Counter]=line.split(",");
				Counter=Counter+1;
			}
		}catch(Exception x){
			System.out.println("EXCEPTION OCCURED");
			System.exit(1);
		}
		try{
			br2.close();
		}catch(Exception x){
			System.exit(1);
		}
		Counter=0;
		for(int j=0;j<data.length;j++){
			TEMP=data[j][1].substring(2,6);
			temp=Integer.parseInt(TEMP);
			sec=temp%100;
			min=(temp%10000)-sec;
			min=min/100;
			temp2=(min*60);
			temp2+=sec;//temp2 time in sec
			if(Counter==0){
				latitude[Counter]=Double.parseDouble(data[j][2]);
				longtitude[Counter]=Double.parseDouble(data[j][4]);
				time[Counter]=temp2;
				Counter+=1;
			}
			else if(Counter<5 && Counter>0){
				if(temp2-time[Counter-1]>18){
					latitude[Counter]=Double.parseDouble(data[j][2]);
					longtitude[Counter]=Double.parseDouble(data[j][4]);
					time[Counter]=temp2;
					Counter=Counter+1;
				}
			}
			else break;
		}
		String cmd=command;
		long a,b;
		int aa,bb;
		for(int j=0;j<5;j++){
			a=(long)(longtitude[j]);
			b=(long)(latitude[j]);
			aa=(int)((longtitude[j]-a)*60);
			bb=(int)((latitude[j]-b)*60);
			cmd=cmd+"T="+a+aa+b+bb;
		}
		cmd=cmd+"\r\n";
		System.out.println(cmd);
		Modem modem2=new Modem();
		modem2.setSpeed(80000);
		modem2.setTimeout(2000);
		modem2.open("ithaki");
		for(;;){
			try{
				k=modem2.read();
				if(k==-1)break;
				System.out.print((char)k);
			}catch(Exception x){
				System.out.println("Exception Occured");
				System.exit(1);
			}
		}
		for(;;){
			try{
				modem2.write(cmd.getBytes());
			}catch(Exception x){
				System.out.println("Exception Occured");
				System.exit(1);
			}
			try{
				k=modem2.read();
				if(k==-1)break;
				System.out.print((char)k);
				if(k==0xFF){
					for(;;){
						out.write(k);
						k=modem2.read();
						if(k==0xFF){
							out.write(k);
							k=modem2.read();
							if(k==0xD9){
								out.write(k);
								counter=true;
							}
						}
						if(counter)break;
					}
				}
			}catch(Exception x){
				System.exit(1);
			}
			if(counter)break;
		}
		modem2.close();
		try{
			out.close();
		}catch(Exception x){
			System.exit(1);
		}
	}
	public void call_ACKNACK(Modem modem,String commandACK,String commandNACK){
		int k ;
		PrintWriter ackResponse=null;
		PrintWriter ackRetrans=null;
		try{
			ackResponse=new PrintWriter("ACK.txt","UTF-8");
			ackRetrans=new PrintWriter("ACK_retrans.txt", "UTF-8");
		}catch(Exception x){
			System.out.println("exception occured");
			System.exit(1);
		}
		long tic,toc;
		long totalNumberofPackets=0;
		String currentPacket="";
		String code,result;
		int xorResult;
		char xor;
		float responseTime;
		long correctPackets=0;
		long wrongPackets=0;
		long retransCount=0;
		long tStart;
		tStart=System.currentTimeMillis();
		tic=System.currentTimeMillis();
		try{
			modem.write(commandACK.getBytes());
		}catch(Exception x){
			System.exit(1);
		}
		while((System.currentTimeMillis()-tStart)<4*60000){
			for(;;){
				try{
					k=modem.read();
					if(k==-1)break;
					currentPacket+=(char)k;
					if(currentPacket.endsWith("PSTOP")){
						code=currentPacket.substring(31,47);
						result=currentPacket.substring(49,52);
						xorResult=Integer.parseInt(result);
						xor=code.charAt(0);
						for(int i=1;i<16;i++){
							xor=(char)(xor^(code.charAt(i)));
						}
						if((int)xor==xorResult){
							ackRetrans.write(Long.toString(retransCount));
							ackRetrans.write(System.lineSeparator());
							if(retransCount > 0) {
								retransCount = 0;
							}
							toc=System.currentTimeMillis();
							responseTime=(float)((float)(toc-tic)/1000);
							ackResponse.write(Float.toString(responseTime));
							ackResponse.write(System.lineSeparator());
       						correctPackets++;
       						tic=System.currentTimeMillis();
       						try{
       							modem.write(commandACK.getBytes());
       						}catch(Exception x){
       							System.exit(1);
       						}
						}else{
							wrongPackets++;
							retransCount++;
							try{
								modem.write(commandNACK.getBytes());
							}catch(Exception x){
								System.exit(1);
							}
						}
						break;
					}
				} catch(Exception x){
					System.exit(1);
				}
			}
			totalNumberofPackets++;
			currentPacket="";
		}
		System.out.println("Total Packets: " + totalNumberofPackets);
		System.out.println("Corrent Packets: " + correctPackets);
		System.out.println("Wrong Packets: " + wrongPackets);
		ackRetrans.close();
		ackResponse.close();
	}
}
