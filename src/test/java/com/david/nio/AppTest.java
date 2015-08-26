package com.david.nio;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.swing.plaf.SliderUI;

import org.junit.Test;

public class AppTest {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final String RW = "rw";

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@Test
	public void testApp() {
		Method[] methods = ByteChannel.class.getMethods();
		for (Method method : methods) {
			System.out.println(method);
		}
	}

	private File getFile(String path) {

		return new File(path);
	}

	@Test
	public void testRandomAccessFile() throws IOException {
		String path = "F:" + File.separator + "静夜思.txt";
		RandomAccessFile raf = new RandomAccessFile(getFile(path), RW);
		FileChannel fileChannel = raf.getChannel();
		ByteBuffer byteBuffer = ByteBuffer.allocate(120);

		int byteRead = fileChannel.read(byteBuffer);

		while (byteRead != -1) {
			System.out.println("Read: " + byteRead);

			byteBuffer.flip(); // 注意数据翻转

			while (byteBuffer.hasRemaining()) {
				System.out.print((char) byteBuffer.get());
			}

			byteBuffer.clear();
			byteRead = fileChannel.read(byteBuffer);
		}

		raf.close();

	}

	@Test
	public void testBuffer() {
		System.out.println("从buffer读取到channel");
		System.out.println("使用get从buffer");
	}

	@Test
	public void testBufferDemo() throws IOException {
		String path = "F:" + File.separator + "静夜思.txt";
		RandomAccessFile raf = new RandomAccessFile(new File(path), RW);
		FileChannel fileChannel = raf.getChannel();
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

		int len = fileChannel.read(byteBuffer);
		StringBuilder sb = new StringBuilder();

		while (len != -1) {
			byteBuffer.flip();
			while (byteBuffer.hasRemaining()) {
				sb.append((char) byteBuffer.get());
			}

			byteBuffer.clear();
			len = fileChannel.read(byteBuffer);
		}

		raf.close();
		System.out.println(sb.toString());
	}

	@Test
	public void testWrapBuffer() {
		String s1 = "你好中国";
		String s2 = "hello, daviddai";
		CharBuffer c1 = CharBuffer.wrap(s1);
		CharBuffer c2 = CharBuffer.wrap(s2);
		System.out.println(c2);
		System.out.println(c1);

	}

	@Test
	public void testTransferFrom() throws IOException {
		String pattern = RW;
		File fromFile = new File("F:" + File.separator + "niofile" + File.separator + "fromFile.txt");
		RandomAccessFile fromAccessFile = new RandomAccessFile(fromFile, pattern);
		FileChannel fromChannel = fromAccessFile.getChannel();

		File toFile = new File("F:" + File.separator + "niofile" + File.separator + "toFile.txt");
		RandomAccessFile toAccessFile = new RandomAccessFile(toFile, pattern);
		FileChannel toChannel = toAccessFile.getChannel();

		long size = fromChannel.size();

		toChannel.transferFrom(fromChannel, 0, size);

		fromAccessFile.close();
		toAccessFile.close();

		fromChannel.close();
		toChannel.close();

		System.err.println("transferFrom操作成功...");
	}

	@Test
	public void testTransferTo() throws IOException {
		String pattern = RW;

		File fromFile = new File("F:" + File.separator + "niofile" + File.separator + "newFromFile.txt");
		RandomAccessFile fromAccessFile = new RandomAccessFile(fromFile, pattern);
		FileChannel fromChannel = fromAccessFile.getChannel();

		File toFile = new File("F:" + File.separator + "niofile" + File.separator + "toFile.txt");
		RandomAccessFile toAccessFile = new RandomAccessFile(toFile, pattern);
		FileChannel toChannel = toAccessFile.getChannel();

		long size = toChannel.size();

		toChannel.transferTo(0, size, fromChannel);

		fromAccessFile.close();
		toAccessFile.close();

		fromChannel.close();
		toChannel.close();

		System.err.println("transferTo操作成功...");
	}

	@Test
	public void testClearContents() {
		File fromFile = new File("F:" + File.separator + "niofile" + File.separator + "newFromFile.txt");
		clearContents(fromFile);
	}

	private void clearContents(File file) {
		try {
			FileWriter fw = new FileWriter(file);
			fw.write("");
			fw.close();
			System.out.println("清理成功!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testFileChannel() throws IOException {
		String path = "F:" + File.separator + "nioFile" + File.separator + "result.txt";
		RandomAccessFile randomAccessFile = new RandomAccessFile(getFile(path), RW);
		FileChannel fileChannel = randomAccessFile.getChannel();

		System.out.println("start position: " + fileChannel.position());

		String newData = "New String to write to file..." + System.currentTimeMillis();
		ByteBuffer byteBuffer = ByteBuffer.allocate(48);

		byteBuffer.clear();
		byteBuffer.put(newData.getBytes());
		byteBuffer.flip();

		long beforeSize = fileChannel.size();
		System.out.println("before write size: " + beforeSize);

		while (byteBuffer.hasRemaining()) {
			fileChannel.write(byteBuffer);
		}

		long afterSize = fileChannel.size();
		System.out.println("after write size: " + afterSize);

		System.out.println("end position: " + fileChannel.position());
		System.out.println("channel size: " + fileChannel.size());

		randomAccessFile.close();
		fileChannel.close();
	}

	@Test
	public void testSelector() throws IOException {
		SocketAddress socketAddress = new InetSocketAddress("172.16.21.200", 9999);
		SocketChannel socketChannel = SocketChannel.open(socketAddress);
		Selector selector = Selector.open();
		socketChannel.configureBlocking(false);
		socketChannel.register(selector, SelectionKey.OP_READ);

		while (true) {
			int readChannel = selector.select();
			if (readChannel == 0)
				continue;

			Set<SelectionKey> keys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = keys.iterator();
			while (iterator.hasNext()) {
				SelectionKey selectionKey = iterator.next();
				if (selectionKey.isAcceptable()) {
					System.out.println("isAcceptable");
				} else if (selectionKey.isConnectable()) {
					System.out.println("isConnectable");
				} else if (selectionKey.isReadable()) {
					System.out.println("isReadable");
				} else if (selectionKey.isWritable()) {
					System.out.println("isWritable");
				}

				iterator.remove();
			}
		}

	}

	@Test
	public void testSocketChannelConnect() throws IOException {
		SocketChannel socketChannel = SocketChannel.open();
		boolean isConnect = socketChannel.connect(new InetSocketAddress("localhost", 9999));
		System.out.println("before close: " + isConnect + ", socketIsConnected: " + socketChannel.isConnected());

		// 怎样确认我的buffer长度是够的呢
		ByteBuffer buff = ByteBuffer.allocate(48);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String data = "hello, world, " + sdf.format(new Date());
		System.out.println("data length: " + data.length());
		buff.clear();
		buff.put(data.getBytes());

		System.out.println("buff.position(): " + buff.position());
		buff.flip();

		while (buff.hasRemaining()) {
			socketChannel.write(buff);
		}

		socketChannel.close();
		System.out.println("after close, socketIsConnected: " + socketChannel.isConnected());
	}

	@Test
	public void testServerSocketChannelConnect() throws IOException, InterruptedException {
		// 打开连接
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		ServerSocket serverSocket = serverSocketChannel.socket();
		System.out.println(serverSocket);
		serverSocket.bind(new InetSocketAddress("localhost", 9999));
		System.err.println(serverSocket);

		ByteBuffer buff = ByteBuffer.allocate(128);
		StringBuilder sb = null;
		while (true) {
			// 一旦读完的buffer需要再被写入之前可以进行clear()或者compact()操作，compact()会保留未读取数据，先写写
			buff.clear();
			System.out.println("The server socket is started...");
			SocketChannel socketChannel = serverSocketChannel.accept();
			System.out.println("accepted a socketChannel information: " + socketChannel);
			int readLength = socketChannel.read(buff);
			System.out.println("readLength: " + readLength + ", " + sdf.format(new Date()));

			buff.flip();
			sb = new StringBuilder();
			while (buff.hasRemaining()) {
				sb.append((char) buff.get());
			}

			System.err.println("received information: " + sb.toString());
		}

	}

	@Test
	public void testDatagramChannelClient() throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		String data = "hello, daviddai, " + sdf.format(new Date());
		ByteBuffer buff = ByteBuffer.allocate(128);
		buff.clear();
		buff.put(data.getBytes());
		buff.flip();

		int byteSent = channel.send(buff, new InetSocketAddress("localhost", 9998));
		System.out.println("byteSent: " + byteSent);

	}

	@Test
	public void testDatagramChannelServer() throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		DatagramSocket datagramSocket = channel.socket();
		datagramSocket.bind(new InetSocketAddress("localhost", 9998));
		System.out.println(datagramSocket);
		System.out.println(datagramSocket.getInetAddress());
		System.out.println(datagramSocket.getLocalAddress());
		System.out.println(datagramSocket.getRemoteSocketAddress());
		System.out.println(datagramSocket.getLocalSocketAddress());
		System.err.println("===============================");

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ByteBuffer buff = ByteBuffer.allocate(128);
		StringBuilder sb = null;

		while (true) {
			buff.clear();
			System.out.println("The server socket is started...");
			SocketAddress socketAddress = channel.receive(buff);
			System.out.println(socketAddress + ", " + sdf.format(new Date()));

			buff.flip();
			sb = new StringBuilder();
			while (buff.hasRemaining()) {
				sb.append((char) buff.get());
			}

			System.err.println(sb.toString());
		}
	}

	@Test
	public void testPipeSinkChannel() throws IOException {
		Pipe pipe = Pipe.open();
		Pipe.SinkChannel sinkChannel = pipe.sink();
		String data = "hello, pipesink, " + sdf.format(new Date());
		ByteBuffer buff = ByteBuffer.allocate(64);
		buff.clear();
		buff.put(data.getBytes());

		buff.flip();
		sinkChannel.write(buff);

		System.out.println("=============source分割线=====================");
		Pipe.SourceChannel sourceChannel = pipe.source();
		ByteBuffer resultBuff = ByteBuffer.allocate(64);
		int length = sourceChannel.read(resultBuff);
		System.out.println("data length: " + length);
	
		StringBuilder sb = new StringBuilder();
		resultBuff.flip();
		while (resultBuff.hasRemaining()) {
			sb.append((char) resultBuff.get());
		}
		
		System.out.println(sb.toString());
	}
}
