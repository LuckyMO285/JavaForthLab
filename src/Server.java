import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.Charset;


class MyThreadE extends Thread {
	SocketChannel client;
	String str;
	private List<String> fileNames = new ArrayList<>();


	MyThreadE(SocketChannel Client, String Str ){
		client = Client;
		str = Str;
		char[] mas = Str.toCharArray();
		String text = "";
		for (int i = 0; i < mas.length; i++){
			if (mas[i] != ' '){
				text += mas[i];
			}
			else{
				fileNames.add(text);
				text = "";
			}
		}
	}



	public void run() {
		String strForClient = "";
		try {

			for (int i = 0; i < fileNames.size(); i++) {

				strForClient += fileNames.get(i) + '\n' + '\n';

				final Pattern pattern = Pattern.compile(
						"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

				BufferedReader in = new BufferedReader(new FileReader("/home/luck/ForFifthLab/" + fileNames.get(i)));
				String str = in.readLine();
				int j = 0;
				String word = "";

				while (str != null) {
					word = "";
					while (str.equals(""))
						str = in.readLine();

					while ((j <= str.length()) && str.charAt(j) != '>' && str.charAt(j) != '<'
							&& str.charAt(j) != ' ') {
						word += str.charAt(j);
						j++;
						if (j == str.length())
							break;
					}

					if (word.length() == 0)
						j++;
					Matcher matcher = pattern.matcher(word);
					if (matcher.find()) {
						strForClient += word +"\n";
					}
					if (j == str.length()) {
						str = in.readLine();
						j = 0;
					}
				}
				in.close();

			}
			ByteBuffer buffer = ByteBuffer.allocate(strForClient.length());
			buffer = ByteBuffer.wrap(strForClient.getBytes());
			client.write(buffer);
			buffer.clear();
			

			Thread.sleep(0);

			client.close();
		} catch (IOException | InterruptedException e) {
			//log("Something happened");
			e.printStackTrace();
		}
	}



}


public class Server {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {

		Selector selector = Selector.open();

		// выбираем канал (порт) для общение с клиентом по нему
		ServerSocketChannel Socket = ServerSocketChannel.open();
		InetSocketAddress Addr = new InetSocketAddress("localhost", 1122);

		// связываем сокет с адресом для его прослушивания
		Socket.bind(Addr);

		// Настраиваем блокировку данного канала
		Socket.configureBlocking(false);

		int ops = Socket.validOps();
		SelectionKey selectKey = Socket.register(selector, ops, null);

		// запуск сервера
		while (true) {

			//log("I'm a server and i'm waiting for new connection and buffer select...");
			// Выбор набора ключей, чьи каналаы готовы для операции ввода/вывода
			selector.select();

			Set<SelectionKey> Keys = selector.selectedKeys();
			Iterator<SelectionKey> Iterator = Keys.iterator();

			while (Iterator.hasNext()) {
				SelectionKey myKey = Iterator.next();

				// проверяет, готов ли канал принять новое соединение
				if (myKey.isAcceptable()) {
					SocketChannel Client = Socket.accept();

					// Отключение блокирующего режима
					Client.configureBlocking(false);

					// Операция набора бит для выполнения операции
					Client.register(selector, SelectionKey.OP_READ);
					log("Connection Accepted: " + Client.getLocalAddress() + "\n");

					// проверяет готовность канала быть прочитанным
				} else if (myKey.isReadable()) {

					SocketChannel Client = (SocketChannel) myKey.channel();
					ByteBuffer Buffer = ByteBuffer.allocate(256);
					Client.read(Buffer);
					String result = new String(Buffer.array()).trim();
					if (result.isEmpty())
						break;

					MyThreadE t = new MyThreadE(Client, result);
					t.start();

				}
				Iterator.remove();
			}
		}
	}

	private static void log(String str) {
		System.out.println(str);
	}
}