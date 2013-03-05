package org.apache.mahout.cf.taste.vjianke.engine.segment;

import kevin.zhang.NLPIR;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;


public class ICTCLASDelegate
{
	public enum ECodeType
	{
		CODE_TYPE_UNKNOWN, // type unknown
		CODE_TYPE_ASCII, // ASCII
		CODE_TYPE_GB, // GB2312,GBK,GB10380
		CODE_TYPE_UTF8, // UTF-8
		CODE_TYPE_BIG5;// BIG5
		public static int getECode(Charset charset)
		{
			String name = charset.name();
			if (name.equalsIgnoreCase("ascii"))
				return 1;
			if (name.equalsIgnoreCase("gb2312"))
				return 2;
			if (name.equalsIgnoreCase("gbk"))
				return 2;
			if (name.equalsIgnoreCase("utf8"))
				return 3;
			if (name.equalsIgnoreCase("utf-8"))
				return 3;
			if (name.equalsIgnoreCase("big5"))
				return 4;
			return 0;
		}
	}

	private static final String libLocation = "ICTCLAS/";
	private static final String userDict = "userDict.txt";
	// private static final String userDict = "testDict.txt";
	private final static Charset defaultCharset = Charset.forName("gbk");
	private static final boolean is64 = false;
	private static String ictclasPath;

	private static String getRootPath()
	{
		if (ictclasPath == null)
		{
			ictclasPath = ICTCLASDelegate.class.getResource("/").getFile().substring(
				1)
					+ libLocation;
		}
		return ictclasPath;
	}

	public static final String toDBC(String in)
	{
		byte[] b;
		try
		{
			b = in.getBytes("unicode");
			for (int i = 2; i < b.length; i += 2)
			{
				if (b[i] == -1)
				{
					b[i + 1] = (byte) (b[i + 1] + 32);
					b[i] = 0;
				}
			}
			return new String(b, "unicode");
		} catch (UnsupportedEncodingException e1)
		{
		}
		return null;
	}

	public static String toOriginal(String text)
	{
		StringBuffer res = new StringBuffer();
		boolean type = false;
		for (int i = 1; i < text.length(); i++)
		{
			if (text.charAt(i) == ' ')
				break;
			if (text.charAt(i) == '/')
			{
				type = true;
				break;
			}
		}
		int state = 0;
		if (!type)
		{
			for (char c : text.toCharArray())
			{

				switch (state)
				{
				case 0:
					state = 1;
					res.append(c);
					break;
				case 1:
					if (c == ' ')
						state = 0;
					else
						res.append(c);
					break;
				}

			}
		} else
		{
			for (char c : text.toCharArray())
			{
				switch (state)
				{
				case 0:
					state = 1;
					res.append(c);
					break;
				case 1:
					if (c == '/')
						state = 2;
					else
						res.append(c);
					break;
				case 2:
					if (c == ' ')
						state = 0;
					break;
				}
			}
		}
		return res.toString();
	}

	private static ICTCLASDelegate instance;

	public static ICTCLASDelegate getDelegate()
	{
		if (instance == null)
		{
			synchronized (ICTCLASDelegate.class)
			{
				if (instance == null)
				{
					instance = new ICTCLASDelegate();
					instance.init();
					instance.importUserDictFile(getRootPath() + userDict);
				}
			}
		}
		return instance;
	}

	private NLPIR ictclas;

	private ICTCLASDelegate()
	{
	}

	public boolean init()
	{
		ictclas = new NLPIR();
		return ictclas.NLPIR_Init(getRootPath().getBytes(defaultCharset), 0);
	}

	public boolean exit()
	{
		return ictclas.NLPIR_Exit();
	}

	public int importUserDictFile(String path)
	{
		return ictclas.NLPIR_AddUserWord(
                path.getBytes(defaultCharset));
	}

	//
	// public int saveTheUsrDic()
	// {
	// return ictclas.ICTCLAS_SaveTheUsrDic();
	// }

	public String process(String source, int tagged)
	{
		byte[] buff = ictclas.NLPIR_ParagraphProcess(
                source.getBytes(defaultCharset), tagged);
		return new String(buff, 0, buff.length - 1, defaultCharset);
	}

	public void writeNewWords(String words)
	{
		synchronized (userDict)
		{
			String[] word = words.split(" ");
			try
			{
				PrintWriter out = new PrintWriter(new OutputStreamWriter(
						new FileOutputStream(getRootPath() + userDict, true),
						defaultCharset));
				boolean changed = false;
				for (String s : word)
				{

					if (s.length() > 1)
						if (process(s).contains(" "))
						{
							changed = true;
							out.println(s + "@@n");
						}
				}
				out.close();
				if (changed)
					importUserDictFile(getRootPath() + userDict);
			} catch (IOException e)
			{
			}
		}
	}

	public String process(String string)
	{
		WordReader in = new WordReader(process(string, 1));
		StringBuilder sb = new StringBuilder();
		String t;
		String nr1 = null;
		try
		{
			while ((t = in.readWord()) != null)
			{
				if (t.endsWith("nr1"))
				{
					if (nr1 != null)
						sb.append(nr1).append(' ');
					nr1 = t;
				} else if (nr1 != null && t.endsWith("nr2"))
				{
					sb.append(nr1.substring(0, nr1.length() - 4)).append(
						t.substring(0, t.length() - 1));
					sb.append(' ');
					nr1 = null;
				} else
				{
					if (nr1 != null)
					{
						sb.append(nr1).append(' ');
						nr1 = null;
					}
					if (t.length() > 1)
						if (t.charAt(0) == '-' && t.endsWith("nrf"))
						{
							sb.append("-/w ");
							t = t.substring(1);
						}
					sb.append(t).append(' ');
				}
			}
			if (nr1 != null)
				sb.append(nr1).append(' ');
		} catch (IOException e)
		{
		}
		return sb.substring(0, sb.length() - 1);
	}

	/**
	 * @param
	 */
	public static void loadSogouDic()
	{
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("N", "n");
		map.put("V", "v");
		map.put("ADJ", "a");
		map.put("ADV", "d");
		map.put("CLAS", "q");
		map.put("ECHO", "o");
		map.put("STRU", "u");
		map.put("AUX", "u");
		map.put("COOR", "c");
		map.put("CONJ", "c");
		map.put("SUFFIX", "h");
		map.put("PREFIX", "k");
		map.put("PREP", "p");
		map.put("PRON", "r");
		map.put("QUES", "c");
		map.put("NUM", "m");
		map.put("IDIOM", "idiom");
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream("SogouLabDic.dic"), defaultCharset));
			PrintWriter out = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(getRootPath() + userDict),
					defaultCharset));
			ICTCLASDelegate d = ICTCLASDelegate.getDelegate();
			int i = 0;
			while (in.ready())
			{
				String[] l = in.readLine().split("\t");
				if (l[0].length() > 1)
					if (d.process(l[0]).contains(" "))
					{
						String t = "";
						if (l.length > 2)
							t = "@@"
									+ map.get(l[2].subSequence(0,
										l[2].indexOf(',')));
						out.println(l[0] + t);
						System.out.print(i++);
						System.out.print(":");
						System.out.println(l[0]);
					}
			}
			out.close();
			in.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
