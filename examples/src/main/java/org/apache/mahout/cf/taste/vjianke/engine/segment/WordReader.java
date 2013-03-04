package org.apache.mahout.cf.taste.vjianke.engine.segment;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

public class WordReader extends Reader
{
	private Reader in;
	private char sp;
	private boolean notempty;

	public WordReader(String src)
	{
		this(new StringReader(src));
	}

	public WordReader(Reader in)
	{
		this(in, ' ');
	}

	public WordReader(String src, char sp)
	{
		this(new StringReader(src), sp);
	}

	public WordReader(Reader in, char sp)
	{
		this.in = in;
		this.sp = sp;
		notempty = true;
	}

	public boolean hasNext()
	{
		return notempty;
	}

	public String readWord() throws IOException
	{
		if (!notempty)
			return null;
		StringBuilder sb = new StringBuilder();
		int c = read();
		do
		{
			sb.append((char) c);
		} while ((c = read()) != sp && c != -1);
		if (c == -1)
		{
			notempty = false;
		}
		return sb.toString();
	}

	@Override
	public void close() throws IOException
	{
		in.close();
	}

	public void mark(int readAheadLimit) throws IOException
	{
		in.mark(readAheadLimit);
	}

	public boolean markSupported()
	{
		return in.markSupported();
	}

	public int read(char[] cbuf) throws IOException
	{
		return in.read(cbuf);
	}

	public boolean ready() throws IOException
	{
		return in.ready();
	}

	public void reset() throws IOException
	{
		in.reset();
	}

	@Override
	public long skip(long n) throws IOException
	{
		return in.skip(n);
	}

	@Override
	public int read(CharBuffer target) throws IOException
	{
		return in.read(target);
	}

	@Override
	public int read() throws IOException
	{
		return in.read();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		return in.read(cbuf, off, len);
	}

}
