import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.Charset;

//import ru.spbstu.telematics.java.MyThread;

//import ru.spbstu.telematics.java.MyThread;

class MyThread extends Thread {

	private List<Integer> fileNames = new ArrayList<>();
	int nBig = 0;
	int numberS = 0;

	MyThread(int start, int end){
		for(int i = start; i <= end; i++) {
			fileNames.add(i);
			nBig++;
		}
		numberS = start;
	}

	public void run() {
		try{
			InetSocketAddress Addr = new InetSocketAddress("localhost", 1122);
			SocketChannel Client = SocketChannel.open(Addr);

			log("Connecting to Server...");	
			String str = "";

			for (int i = 0; i < nBig; i++) {
				str += fileNames.get(i) + " ";
			}

			byte[] message = new String(str).getBytes();
			ByteBuffer buffer = ByteBuffer.wrap(message);
			Client.write(buffer);

			ByteBuffer bufferRead = ByteBuffer.allocate(2020);

			Client.read(bufferRead);
			String strRead = new String(bufferRead.array());
			System.out.println(strRead);
			buffer.clear();
			bufferRead.clear();

			Thread.sleep(0);

			Client.close();
		}catch (IOException | InterruptedException e) {
			log("Something happened");
			e.printStackTrace();
		}
	}

	private static void log(String str) {
		System.out.println(str);
	}
}

public class Client {

	public static void main(String[] args) throws IOException, InterruptedException {
		int N = 10;
		ArrayList<MyThread> list = new ArrayList<MyThread>();

		for (int i = 1; i <= N; i++){
			MyThread t = new MyThread((i-1)*100/N+1, i * 100 / N );
			t.start();
			list.add(t);
		}

		for (int i = 0; i < list.size(); i++){
			list.get(i).join();
		}
	}
}