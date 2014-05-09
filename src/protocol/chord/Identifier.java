package protocol.chord;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Identifier implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/* bit length */
	public transient static final int maxLength = 140;
	public String id;
	
	public Identifier ()
	{
		this.id = null;
	}
	
	public Identifier (String id)
	{
		this.id = id;
	}
	
	public static Identifier toIdentifier (Object o)
	{
		String id = null;
		if (o instanceof Node)
			id = ((Node) o).getIdentifier().id;
		else if (o instanceof Identifier)
			id = ((Identifier) o).id;
		else if (o instanceof String)
			id = (String) o;
		else if (o instanceof BigInteger)
			id = ((BigInteger) o).toString();
		else
			throw new IllegalArgumentException("can's convert to " + Identifier.class);

		Identifier ident = new Identifier(id);
		if  (ident.toBigInteger().bitLength() > Identifier.maxLength)
			throw new IllegalArgumentException("Max. permissiable length of identifier is " + Identifier.maxLength + " bits");

		return ident;
	}
	
	public static String fromIdentifier (Identifier ident)
	{
		return ident.id.toString();
	}
	
	public boolean compareTo (Object id, String op)
	{
		return Identifier.compare(this, id, op);
	}

	public static boolean compare (Object ob1, Object ob2, String op)
	{
		BigInteger id1 = null;
		BigInteger id2 = null;
		
		if (ob1 instanceof Node)
			id1 = ((Node) ob1).getIdentifier().toBigInteger();
		else if (ob1 instanceof Identifier)
			id1 = ((Identifier) ob1).toBigInteger();
		else if (ob1 instanceof String)
			id1 = Identifier.toIdentifier((String) ob1).toBigInteger();
		else if (ob1 instanceof BigInteger)
			id1 = (BigInteger) ob1;
		else
			throw new IllegalArgumentException("1st identifier = " + (ob1 == null ? "null" : ob1.toString()));

		if (ob2 instanceof Node)
			id2 = ((Node) ob2).getIdentifier().toBigInteger();
		else if (ob2 instanceof Identifier)
			id2 = ((Identifier) ob2).toBigInteger();
		else if (ob2 instanceof String)
			id2 = Identifier.toIdentifier((String) ob2).toBigInteger();
		else if (ob2 instanceof BigInteger)
			id2 = (BigInteger) ob2;
		else
			throw new IllegalArgumentException("2nd identifier = " + (ob2 == null ? "null" : ob2.toString()));

		if (op.equals(">"))
			return id1.compareTo(id2) > 0;
                else if (op.equals(">="))
			return id1.compareTo(id2) >= 0;
                else if (op.equals("<"))
			return id1.compareTo(id2) < 0;
                else if (op.equals("<="))
			return id1.compareTo(id2) <= 0;
                else if (op.equals("=="))
			return id1.compareTo(id2) == 0;
                else if (op.equals("!="))
			return id1.compareTo(id2) != 0;
		else
			throw new IllegalArgumentException("Operator " + op + " not supported as a comparision type for identifiers.");
	}
	
	public boolean between (Object ob1, Object ob2)
	{
		return this.between (ob1, ob2, false, false);
	}
	
	public boolean between (Object ob1, Object ob2, boolean leftClosed, boolean rightClosed)
	{
		if (ob1 == null || ob2 == null)
			throw new IllegalArgumentException("values being compared cant be null");
		BigInteger id1 = null;
		BigInteger id2 = null;
		
		if (ob1 instanceof Node)
			id1 = ((Node) ob1).getIdentifier().toBigInteger();
		else if (ob1 instanceof Identifier)
			id1 = ((Identifier) ob1).toBigInteger();
		else if (ob1 instanceof String)
			id1 = Identifier.toIdentifier((String) ob1).toBigInteger();
		else if (ob1 instanceof BigInteger)
			id1 = (BigInteger) ob1;
		else
			throw new IllegalArgumentException("1st identifier = " + (ob1 == null ? "null" : ob1.toString()));

		if (ob2 instanceof Node)
			id2 = ((Node) ob2).getIdentifier().toBigInteger();
		else if (ob2 instanceof Identifier)
			id2 = ((Identifier) ob2).toBigInteger();
		else if (ob2 instanceof String)
			id2 = Identifier.toIdentifier((String) ob2).toBigInteger();
		else if (ob2 instanceof BigInteger)
			id2 = (BigInteger) ob2;
		else
			throw new IllegalArgumentException("2nd identifier = " + (ob2 == null ? "null" : ob2.toString()));
		
		//if (id1.compareTo(id2) == 0 && leftClosed ^ rightClosed)
			//throw new IllegalArgumentException((leftClosed ? "[" : "(") + id1.toString() + ", " + id2.toString() + (rightClosed ? "]" : ")") + " not allowed.");

		BigInteger id = new BigInteger(this.id);
		BigInteger maxId = BigInteger.ONE.shiftLeft(Identifier.maxLength);
		id = id.compareTo(id1) < 0 ? id.add(maxId) : id;
		id2 = id2.compareTo(id1) <= 0 ? id2.add(maxId) : id2;
		
		
		if (((leftClosed && id1.compareTo(id) <= 0) ||  (!leftClosed && id1.compareTo(id) < 0))
			&& ((rightClosed && id2.compareTo(id) >= 0) ||  (!rightClosed && id2.compareTo(id) > 0)))
		{
			return true;
		}
		return false;
	}

	//TODO: remove this method
	public BigInteger toBigInteger()
	{
	    return new BigInteger(this.id);//foo.getBytes());
	}
	
	//TODO: change the hash function
	public static String hash (String key)
	{
		return new BigInteger(1, Identifier.sha1(key)).mod(BigInteger.ONE.shiftLeft(Identifier.maxLength)).toString();
	}
	 
//	private static String sha1(String input)
//	{
//		String hash = null;
//		try
//		 {
//		     StringBuffer sb = new StringBuffer();
//		     for (int i = 0; i < result.length; i++) {
//		         sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
//		     }
//		     hash = sb.toString();
//		 } catch (NoSuchAlgorithmException e) {}
//		return hash;
//	}
	
	private static byte[] sha1(String input)
	{
		byte[] result = null;
		try
		 {
			 MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		     result = mDigest.digest(input.getBytes());
		 } catch (NoSuchAlgorithmException e) {}
		return result;
	}
	
	public boolean equals (Object o)
	{
		if (o == null)
			return false;
		try
		{
			boolean b = this.compareTo(o, "==");
//			System.out.println("checking if " + this + " == " + o + " result = " + b);
			return b;
		} catch (Exception e)
		{
			return false;
		}
	}
	
	public int hashCode ()
	{
		return this.id.hashCode();
	}
	
        @Override
	public String toString ()
	{
		return this.id;
	}

}
