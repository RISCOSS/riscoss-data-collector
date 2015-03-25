package eu.riscoss.rdc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.AttributesImpl;


public class XmlNode implements CharSequence, Comparable<XmlNode>, Iterable<XmlNode>
{
	static final XmlNode	s_noneNode	= new XmlNode( "", "" );
	
	/**
	 * Creates a whole XmlNode hierarchy from a file of give className
	 * 
	 * @param file
	 *            - file to be loaded
	 * @return an XmlNode hierarchy or NoNode if an error occurs during
	 */
	public static XmlNode load( File file ) {
		Document document = null;
		DocumentBuilder documentBuilder = null;
		DocumentBuilderFactory documentBuilderFactory = null;
		
		try {
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.parse( file );
		}
		catch( Exception e ) {
			return s_noneNode;
		}
		
		return new XmlNode( document.getChildNodes().item( 0 ), true );
	}
	
	public static XmlNode loadString( String string ) {
		return loadString( new ByteArrayInputStream( string.getBytes() ) );
	}
	
	public static XmlNode loadString( InputStream is ) {
		Document document = null;
		DocumentBuilder documentBuilder = null;
		DocumentBuilderFactory documentBuilderFactory = null;
		
		try
		{
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.parse( is );
		}
		catch( Exception e )
		{
			 e.printStackTrace();
			
			return s_noneNode; // new XmlNode();
		}
		
		return new XmlNode( document.getChildNodes().item( 0 ), true );
	}
	
	public static XmlNode load( String filename )
	{
		return load( new File( filename ) );
	}
	
	String							m_tag		= "";
	
	/** ************************************************** */
	
	Map<String, String>		m_attr		= new HashMap<String, String>();
	
	DuplicateMap<String, XmlNode>	children	= new DuplicateMap<String, XmlNode>();
	
	
	/** ************************************************** */
	
	String							m_value		= "";
	
	/**
	 * Construct an ampty XmlNode
	 */
	public XmlNode()
	{
	}
	
	/**
	 * Construct a whole XmlNode hierarchy as a result of parsing of
	 * <i>file</i>.
	 * 
	 * @param file
	 *            - The file to be parsed
	 */
	public XmlNode( File file )
	{
		Document document = createDocument( file );
		
		if( document == null )
		{
			return;
		}
		
		add( document.getChildNodes().item( 0 ) );
	}
	
	/**
	 * Construct an XmlNode starting from a stream instead of a file
	 * 
	 * @param is
	 *            Input stream
	 */
	public XmlNode( InputStream is )
	{
		Document document = createDocument( is );
		
		if( document == null )
		{
			return;
		}
		
		add( document.getChildNodes().item( 0 ) );
	}
	
	/**
	 * Construct a whole XmlNode from a java native xml Node.
	 * 
	 * @param nd
	 *            - Node
	 */
	public XmlNode( Node nd )
	{
		add( nd );
	}
	
	XmlNode( Node nd, boolean force )
	{
		this.m_tag = nd.getNodeName().trim();
		
		NamedNodeMap attr = nd.getAttributes();
		
		for( int i = 0; i < attr.getLength(); i++ )
		{
			Node a = attr.item( i );
			setAttr( a.getNodeName(), a.getNodeValue() );
		}
		
//		if( !hasSubElements( nd ) )
//		{
////			this.m_tag = this.m_tag.trim();
//			this.m_value = getStringContent( nd );
//			// add( m_tag.trim(), "Aaa" ); //getStringContent( nd ) );
//			
//			return;
//		}
		
		NodeList nlist = nd.getChildNodes();
		
		int ncount = 0;
		String value = "";
		
		for( int i = 0; i < nlist.getLength(); i++ )
		{
			Node item = nlist.item( i );
			
			switch( item.getNodeType() )
			{
			case Node.ELEMENT_NODE:
				ncount++;
				add( item.getNodeName(), new XmlNode( item, force ) );
				break;
			case Node.CDATA_SECTION_NODE:
				value += item.getNodeValue();
				break;
			case Node.TEXT_NODE:
				value += item.getNodeValue();
				break;
			default:
				break;
			}
		}
		
		if( ncount < 1 )
			this.m_value = value;
	}
	
	/**
	 * Construct an XmlNode with specified tag and no value
	 * 
	 * @param tag
	 *            - Tag className
	 */
	public XmlNode( String tag )
	{
		this.m_tag = tag;
	}
	
	/**
	 * Construct an XmlNode with specified tag and value
	 * 
	 * @param tag
	 *            - Tag className
	 * @param value
	 *            - Initial value
	 */
	public XmlNode( String tag, String value )
	{
		this.m_tag = tag;
		this.m_value = value;
	}
	
	/**
	 * Construct an XmlNode with specified tag and value.
	 * 
	 * @param tag
	 *            - Tag className
	 * @param value
	 *            - Initial value
	 * @param attr_string
	 *            - Array of strings containing attributes to set up Element i
	 *            of the array is the className of attribute i. Element i + 1 of
	 *            the array is the value of attribute i. The array must have
	 *            lenght i * 2
	 */
	public XmlNode( String tag, String value, String[] attr_string )
	{
		this.m_tag = tag;
		this.m_value = value;
		for( int i = 0; i < attr_string.length; i += 2 )
		{
			setAttr( attr_string[i], attr_string[i + 1] );
		}
	}
	
	/**
	 * Contruct an XmlNode with the same tag, value and attributes as
	 * <i>copy</i>. Child nodes are nod copied.
	 * 
	 * @param copy
	 *            - XmlNode to copy from
	 */
	public XmlNode( XmlNode copy )
	{
		this.m_tag = copy.getTag();
		this.m_value = copy.getValue();
		Set<String> set = copy.listAttributes();
		for( Iterator<String> it = set.iterator(); it.hasNext(); )
		{
			String attr = it.next();
			setAttr( attr, copy.getAttr( attr ) );
		}
	}
	
	/**
	 * Contruct an XmlNode with the same tag, value and attributes as
	 * <i>copy</i>. Allows to recursively create a copy of a whole XmlNode
	 * hierarcy.
	 * 
	 * @param copy
	 *            XmlNode to be copied
	 * @param copy_childs
	 *            if true, childs are also copied
	 */
	public XmlNode( XmlNode copy, boolean copy_childs )
	{
		this.m_tag = copy.getTag();
		this.m_value = copy.getValue();
		Set<String> set = copy.listAttributes();
		for( Iterator<String> it = set.iterator(); it.hasNext(); )
		{
			String attr = it.next();
			setAttr( attr, copy.getAttr( attr ) );
		}
		if( !copy_childs )
		{
			return;
		}
		
		for( XmlNode node : copy.children )
		{
			add( new XmlNode( node, true ) );
		}
	}
	
	public void add( Node nd )
	{
		this.m_tag = nd.getNodeName();
		
		NamedNodeMap attr = nd.getAttributes();
		
		for( int i = 0; i < attr.getLength(); i++ )
		{
			Node a = attr.item( i );
			setAttr( a.getNodeName(), a.getNodeValue() );
		}
		
		NodeList nlist = nd.getChildNodes();
		
		if( nlist.getLength() == 1 )
		{
			this.m_value = nlist.item( 0 ).getNodeValue();
			return;
		}
		
		for( int i = 0; i < nlist.getLength(); i++ )
		{
			Node item = nlist.item( i );
			
			if( item.getNodeType() == Node.ELEMENT_NODE )
			{
				// if( item.getFirstChild() != null )
				{
					add( new XmlNode( item ) );
				}
			}
		}
	}
	
	// //////////////////////////////////////////////////////////////////////////////////
	// CharSequence
	
	public XmlNode add( String key )
	{
		return add( new XmlNode( key ) );
	}
	
	public void add( String key, boolean value )
	{
		add( key, new String( "" + value ) );
	}
	
	public void add( String key, double value )
	{
		add( key, new String( "" + value ) );
	}
	
	public void add( String key, float value )
	{
		add( key, new String( "" + value ) );
	}
	
	// //////////////////////////////////////////////////////////////////////////////////
	
	public void add( String key, int value )
	{
		add( key, new String( "" + value ) );
	}
	
	public void add( String key, String value )
	{
		add( key, new XmlNode( key, value ) );
	}
	
	public void add( String key, XmlNode child )
	{
		this.children.put( key, child );
	}
	
	public XmlNode add( XmlNode child )
	{
		this.children.put( child.getTag(), child );
		
		return child;
	}
	
	// ////////////////////////////////////////////////////////////////////////////
	
	/**
	 * From CharSequence interface: Returns the character at the specified
	 * index.
	 * 
	 * @param index
	 *            Character index
	 * @return Character at given position
	 */
	public char charAt( int index )
	{
		return this.m_value.charAt( index );
	}
	
	public int childsCount()
	{
		return this.children.size();
	}
	
	// ////////////////////////////////////////////////////////////////////////////
	
	public int compareTo( XmlNode arg0 )
	{
		return this.m_tag.compareTo( arg0.toString() );
	}
	
	Document createDocument( File file )
	{
		Document document = null;
		DocumentBuilder documentBuilder = null;
		DocumentBuilderFactory documentBuilderFactory = null;
		
		try
		{
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.parse( file );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return null;
		}
		return document;
	}
	
	Document createDocument( InputStream is )
	{
		Document document = null;
		DocumentBuilder documentBuilder = null;
		DocumentBuilderFactory documentBuilderFactory = null;
		
		try
		{
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			document = documentBuilder.parse( is );
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
			return null;
		}
		return document;
	}
	
	/**
	 * Tests if addressed node exists in xml tree
	 * 
	 * @return Returns true if <code>this != s_noneNode</code>
	 */
	public boolean exists()
	{
		return (this != s_noneNode);
	}
	
	public XmlNode findByAttr( String attr, String value )
	{
		for( XmlNode node : children )
		{
			if( node.getAttr( attr ).compareTo( value ) == 0 )
			{
				return node;
			}
		}
		
		//		ArrayList<XmlNode> childs = getChilds();
		//		int n = childs.size();
		//		for( int i = 0; i < n; i++ )
		//		{
		//			XmlNode node = childs.get( i );
		//			if( node.getAttr( attr ).compareTo( value ) == 0 )
		//			{
		//				return node;
		//			}
		//		}
		
		return null;
	}
	
	public String formatAttrString()
	{
		String txt = "";
		Set<String> attribs = listAttributes();
		
		for( Iterator<String> it = attribs.iterator(); it.hasNext(); )
		{
			String key = (String) it.next();
			
			txt = txt + " " + key + "=\"" + getAttr( key ) + "\"";
		}
		
		return txt;
	}
	
	public String getAttr( int n )
	{
		return this.m_attr.get( n );
	}
	
	public String getAttr( String attr_name )
	{
		String ret = this.m_attr.get( attr_name );
		if( ret == null )
		{
			return "";
		}
		return ret;
	}
	
	public String getAttr( String attr_name, String def )
	{
		String ret = this.m_attr.get( attr_name );
		if( ret == null )
		{
			return def;
		}
		return ret;
	}
	
	public int getAttrCount()
	{
		return this.m_attr.size();
	}
	
	// //////////////////////////////////////////////
	public boolean getBoolean()
	{
		return this.m_value.trim().toLowerCase().compareTo( "true" ) == 0;
		// return Boolean.getBoolean( m_value.trim().toLowerCase() );
	}
	
	// //////////////////////////////////////////
	
	public boolean getBoolean( boolean def )
	{
		if( !exists() )
		{
			return def;
		}
		boolean ret = def;
		try
		{
			ret = (this.m_value.trim().toLowerCase().matches( "true" ) ? true
					: false);
		}
		catch( Exception ex )
		{
		}
		return ret;
	}
	
	/**
	 * Returns the number of child tags
	 * 
	 * @return the number of child tags
	 */
	public int getChildCount()
	{
		return this.children.size();
	}
	
	public int getChildCount( String tagname )
	{
		return this.children.count( tagname );
	}
	
	/**
	 * Returns a copy of childs list.
	 * 
	 * @return XmlNode.List
	 */
	//	public ArrayList<XmlNode> getChilds()
	//	{
	//		ArrayList<XmlNode> ret = new ArrayList<XmlNode>();
	//		
	//		for( XmlNode node : children )
	//		{
	//			ret.add( node );
	//		}
	////		ret.addAll( this.m_childs.valueSet() );
	//
	//		return ret;
	//	}
	
	/**
	 * Returns a list of all child with specified tag className
	 * 
	 * @param tagname
	 *            - Name of the tag
	 * @return An XmlNode.List containing the tags.
	 */
	public ArrayList<XmlNode> getChildren( String tagname )
	{
		ArrayList<XmlNode> ret = new ArrayList<XmlNode>();
		
		int n = getChildCount( tagname );
		
		for( int i = 0; i < n; i++ ) {
			ret.add( item( tagname, i ) );
		}
		
		return ret;
	}
	
	public Iterable<XmlNode> getChildren() {
		return children;
	}
	
	// //////////////////////////////////////////////
	public double getDouble()
	{
		return Double.parseDouble( this.m_value.trim().toLowerCase() );
	}
	
	public double getDouble( double def )
	{
		if( !exists() )
		{
			return def;
		}
		try
		{
			return Double.parseDouble( this.m_value.trim().toLowerCase() );
		}
		catch( Exception ex )
		{
		}
		return def;
	}
	
	// ////////////////////////////////////////////
	public float getFloat()
	{
		return Float.parseFloat( this.m_value );
	}
	
	public float getFloat( float def )
	{
		float ret = def;
		
		try
		{
			ret = getFloat();
		}
		catch( Exception ex )
		{
		}
		
		return ret;
	}
	
	/**
	 * Returns a html-formatted string representing the whole hierarchy of the
	 * XmlNode
	 * 
	 * @return A string containing an html representation of the node
	 */
	public String getHtmlRepresentation()
	{
		return printXmlNode( this, 0 );
	}
	
	/**
	 * Returns an integer representation of tag's content
	 * 
	 * @return The integer representation of tag's content
	 */
	public int getInt()
	{
		return Integer.parseInt( this.m_value );
	}
	
	/**
	 * Returns an integer representation of tag's content, or <code>def</code>
	 * if value is not convertible to integer.
	 * 
	 * @param def
	 *            - DataSet to return in case of error
	 * @return Integer representation of tag's content
	 */
	public int getInt( int def )
	{
		int ret = def;
		
		try
		{
			ret = getInt();
		}
		catch( Exception ex )
		{
		}
		
		return ret;
	}
	
	String getStringContent( Node nd )
	{
		NodeList nlist = nd.getChildNodes();
		String content = "";
		
		for( int i = 0; i < nlist.getLength(); i++ )
		{
			Node item = nlist.item( i );
			
			if( item.getNodeType() == Node.TEXT_NODE )
			{
				content = content + item.getNodeValue();
			}
		}
		
		return content.trim();
	}
	
	// /////////////////////////////////////////////
	public String getTag()
	{
		return this.m_tag;
	}
	
	protected String getTextRepresentation( int ind )
	{
		String txt = "";
		
		txt = indent( ind, "    " ) + "<" + getTag() + "";
		
		{
			Set<String> attribs = listAttributes();
			
			for( Iterator<String> it = attribs.iterator(); it.hasNext(); )
			{
				String key = (String) it.next();
				
				if( key.startsWith( "$" ) )
				{
					continue;
				}
				
				txt = txt + " " + key + "=\"" + getAttr( key ) + "\"";
			}
		}
		
		txt = txt + ">";
		
		{
			if( children.size() > 0 )
			{
				txt = txt + "\r\n";
				
				for( XmlNode child : children )
				{
					txt = txt
							+ child.getTextRepresentation( ind + 1 );
				}
				
				txt = txt + indent( ind, "    " ) + "</" + getTag() + ">\r\n";
			}
			else
			{
				txt = txt + getValue().trim();
				txt = txt + "</" + getTag() + ">\r\n";
			}
		}
		
		return txt;
	}
	
	public String getValue()
	{
		return this.m_value;
	}
	
	public String getValue( String def )
	{
		if( this.m_value.length() == 0 )
		{
			return def;
		}
		return this.m_value;
	}
	
	boolean hasSubElements( Node nd )
	{
		NodeList nlist = nd.getChildNodes();
		
		for( int i = 0; i < nlist.getLength(); i++ )
		{
			Node item = nlist.item( i );
			
			if( item.getNodeType() == Node.ELEMENT_NODE )
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Prints out indentation characters
	 * 
	 * @param ind
	 *            - Indentation level
	 * @return Indentation string
	 */
	protected String indent( int ind )
	{
		String txt = "";
		for( int s = 0; s < ind; s++ )
		{
			txt = txt + "&nbsp;&nbsp;&nbsp;&nbsp;";
		}
		return txt;
	}
	
	/**
	 * Prints out indentation characters
	 * 
	 * @param ind
	 *            - Indentation level
	 * @param t
	 *            - Character to use for indentation
	 * @return ndentation string
	 */
	protected String indent( int ind, String t )
	{
		String txt = "";
		for( int s = 0; s < ind; s++ )
		{
			txt = txt + t;
		}
		return txt;
	}
	
	//	public XmlNode item( int index )
	//	{
	//		return this.m_childs.get( index );
	//	}
	
	public XmlNode item( String key )
	{
		if( this.children.get( key ) == null )
		{
			return XmlNode.s_noneNode;
		}
		
		return (this.children.get( key ));
	}
	
	public XmlNode item( String key, boolean create )
	{
		if( this.children.get( key ) == null )
		{
			if( create )
			{
				add( new XmlNode( key ) );
			}
			else
			{
				return XmlNode.s_noneNode;
			}
		}
		
		return (this.children.get( key ));
	}
	
	public XmlNode item( String key, int index )
	{
		if( this.children.get( key, index ) == null )
		{
			return XmlNode.s_noneNode;
		}
		
		return (this.children.get( key, index ));
	}
	
	public XmlNode item( String key, int index, boolean create )
	{
		if( create )
		{
			while( this.children.get( key, index ) == null )
			{
				add( new XmlNode( key ) );
			}
		}
		
		if( this.children.get( key, index ) == null )
		{
			return XmlNode.s_noneNode;
		}
		
		return (this.children.get( key, index ));
	}
	
	/**
	 * From CharSequence interface: Returns the length of this character
	 * sequence.
	 * 
	 * @return String length
	 */
	public int length()
	{
		return this.m_value.length();
	}
	
	public Set<String> listAttributes()
	{
		return this.m_attr.keySet();
	}
	
	public Set<String> listChildTags()
	{
		return listChildTags( "" );
	}
	
	Set<String> listChildTags( String prepend )
	{
		Set<String> childTags = new HashSet<String>();
		
		for( Iterator<String> it = this.children.keySet().iterator(); it
				.hasNext(); )
		{
			String key = it.next();
			
			int n = this.getChildCount( key );
			
			if( n > 1 )
			{
				for( int i = 0; i < n; i++ )
				{
					childTags.add( prepend + key + "!" + i );
				}
			}
			else
			{
				childTags.add( prepend + key );
			}
		}
		
		return childTags;
	}
	
	public Set<String> listDescendantTags()
	{
		return listDescendantTags( "" );
	}
	
	Set<String> listDescendantTags( String prepend )
	{
		Set<String> tags = listChildTags( prepend );
		//		ArrayList<XmlNode> childs = getChilds();
		
		for( XmlNode child : children )
		{
			tags.addAll( child.listDescendantTags(
					prepend + child.getTag() + "/" ) );
		}
		
		//		for( int i = 0; i < childs.size(); i++ )
		//		{
		//			tags.addAll( childs.get( i ).listDescendantTags(
		//					prepend + childs.get( i ).getTag() + "/" ) );
		//		}
		
		return tags;
	}
	
	public XmlNode path( String path )
	{
		String tokens[] = path.split( "/" );
		
		if( tokens.length == 1 )
		{
			return item( path, true );
		}
		
		XmlNode node = this;
		
		for( int i = 0; i < tokens.length; i++ )
		{
			String[] parts = tokens[i].split( "!" );
			
			if( parts.length == 1 )
			{
				node = node.item( tokens[i], true );
			}
			else if( parts.length == 2 )
			{
				node = node.item( parts[0], Integer.valueOf( parts[1] )
						.intValue(), true );
			}
		}
		
		return node;
	}
	
	protected void print( PrintStream out, int indentation )
	{
		Set<String> keys;
		
		String ind = "";
		for( int i = 0; i < indentation; i++ )
		{
			ind += "  ";
		}
		
		out.print( ind + "<" + this.m_tag );
		
		keys = this.m_attr.keySet();
		for( Iterator<String> it = keys.iterator(); it.hasNext(); )
		{
			String key = (String) it.next();
			
			if( key.startsWith( "$" ) )
			{
				continue;
			}
			
			out.print( " " + key + "=\"" + getAttr( key ) + "\"" );
		}
		
		if( (this.children.size() < 1) & (this.m_value.length() == 0) )
		{
			out.println( " />" );
			return;
		}
		
		out.print( ">" );
		
		//		ArrayList<XmlNode> childs = getChilds();
		if( children.size() > 0 )
		{
			out.println();
			
			for( XmlNode child : children )
			{
				child.print( out, indentation + 1 );
			}
			
			out.println( ind + "</" + this.m_tag + ">" );
		}
		else
		{
			if( getValue().trim().length() != 0 )
			{
				out.print( getValue() );
				out.println( "</" + this.m_tag + ">" );
			}
		}
	}
	
	/**
	 * Returns a html-formatted string representing the whole hierarchy of the
	 * XmlNode
	 * 
	 * @param node
	 *            - XmlNode to print
	 * @param ind
	 *            - Indentation level
	 * @return A html-formatted string
	 */
	protected String printXmlNode( XmlNode node, int ind )
	{
		// String txt = "";
		StringBuffer buf = new StringBuffer();
		
		buf.append( indent( ind ) );
		buf.append( "&lt;<font color=#aa0000>" );
		buf.append( node.getTag() );
		buf.append( "</font>" );
		// txt = indent( ind ) + "&lt;<font color=#aa0000>" + node.getTag() +
		// "</font>";
		
		{
			Set<String> attribs = node.listAttributes();
			
			for( Iterator<String> it = attribs.iterator(); it.hasNext(); )
			{
				String key = (String) it.next();
				
				buf.append( " <font color=#0000bb>" );
				buf.append( key );
				buf.append( "</font>=<font color=#008800>\"" );
				buf.append( node.getAttr( key ) );
				buf.append( "\"</font>" );
				// txt = txt + " <font color=#0000bb>" + key + "</font>=<font
				// color=#008800>\"" + node.getAttr( key ) + "\"</font>";
			}
		}
		
		buf.append( "&gt;" );
		// txt = txt + "&gt;";
		
		{
			//			ArrayList<XmlNode> childs = node.getChilds();
			
			if( children.size() > 0 )
			{
				buf.append( "<br>" );
				
				for( XmlNode child : children )
				{
					buf.append( printXmlNode( child, ind + 1 ) );
				}
				buf.append( indent( ind ) );
				buf.append( "<font color=#999999>&lt;/" );
				buf.append( node.getTag() );
				buf.append( "&gt;</font><br>" );
				// txt = txt + indent( ind ) + "<font color=#999999>&lt;/" +
				// node.getTag() + "&gt;</font><br>";
			}
			else
			{
				buf.append( "<b>" );
				buf.append( node.getValue().trim() );
				buf.append( "</b>" );
				// txt = txt + "<b>" + node.getValue().trim() + "</b>";
				buf.append( "<font color=#999999>&lt;/" );
				buf.append( node.getTag() );
				buf.append( "&gt;</font><br>" );
				// txt = txt + "<font color=#999999>&lt;/" + node.getTag() +
				// "&gt;</font><br>";
			}
		}
		
		return buf.toString(); // txt;
	}
	
	public void setAttr( String attr_name, String attr_value )
	{
		if( this.m_attr.get( attr_name ) != null )
		{
			this.m_attr.remove( attr_name );
		}
		
		if( attr_value == null )
			this.m_attr.remove( attr_name );
		else
			this.m_attr.put( attr_name, attr_value );
	}
	
	public void setTag( String tag )
	{
		this.m_tag = tag;
	}
	
	public void setValue( boolean value )
	{
		this.m_value = new Boolean( value ).toString();
	}
	
	public void setValue( float f )
	{
		this.m_value = new Float( f ).toString();
	}
	
	public void setValue( int n )
	{
		this.m_value = new Integer( n ).toString();
	}
	
	public void setValue( String value )
	{
		this.m_value = value;
	}
	
	/**
	 * From CharSequence interface: Returns a new character sequence that is a
	 * subsequence of this sequence.
	 * 
	 * @param start
	 *            - the start index, inclusive
	 * @param end
	 *            - the end index, exclusive
	 * @return Returns a new character sequence that is a subsequence of this
	 *         sequence.
	 */
	public CharSequence subSequence( int start, int end )
	{
		return this.m_value.subSequence( start, end );
	}
	
	/**
	 * From CharSequence interface: Returns a string containing the characters
	 * in this sequence in the same order as this sequence.
	 * 
	 * @return a string consisting of exactly this sequence of characters
	 */
	@Override
	public String toString()
	{
		return this.m_value.toString();
	}
	
	public void var_dump()
	{
		var_dump( System.out );
	}
	
	public void var_dump( PrintStream out )
	{
		Set<String> keys;
		
		out.println();
		
		out.println( "-----------" );
		
		out.println( ".TAG: " + this.m_tag );
		out.println( ".VALUE: " + getValue() );
		
		out.print( ".ATTRIBUTES: " );
		
		keys = this.m_attr.keySet();
		for( Iterator<String> it = keys.iterator(); it.hasNext(); )
		{
			String key = (String) it.next();
			
			out.print( key + "=" + getAttr( key ) + " " );
		}
		
		out.println();
		
		keys = this.children.keySet();
		for( Iterator<String> it = keys.iterator(); it.hasNext(); )
		{
			String key = (String) it.next();
			item( key ).var_dump();
		}
	}
	
	public void write( File file )
	{
		try {
			PrintStream ps = new PrintStream( new FileOutputStream( file ) );
			
			write( ps );
			
			ps.close();
		}
		catch( FileNotFoundException ex ) {
			ex.printStackTrace();
		}
	}
	
	public void write( OutputStream out )
	{
		PrintWriter pw = new PrintWriter( out );
		
		write( pw );
	}
	
	public void write( PrintStream ps )
	{
		PrintWriter out = new PrintWriter( ps );
		
		write( out );
	}
	
	void write( TransformerHandler hd )
	{
		try
		{
			// Set keys = m_data.keySet();
			AttributesImpl atts = new AttributesImpl();
			
			if( this.m_tag.trim().length() > 0 )
			{
				Set<String> keys = this.m_attr.keySet();
				for( Iterator<String> it = keys.iterator(); it.hasNext(); )
				{
					String key = (String) it.next();
					
					if( key.startsWith( "$" ) )
					{
						continue;
					}
					
					atts.addAttribute( "", "", key, "CDATA", getAttr( key ) );
				}
				hd.startElement( "", "", this.m_tag, atts );
			}
			
			Set<String> keys = this.children.keySet();
			
			if( (this.m_value.trim().length() > 0) && (keys.size() == 0) )
			{
				hd.characters( this.m_value.toCharArray(), 0, this.m_value
						.length() );
			}
			
			for( Iterator<String> it = keys.iterator(); it.hasNext(); )
			{
				String key = (String) it.next();
				XmlNode content = item( key );
				int itn = 0;
				
				while( content.exists() )
				{
					atts.clear();
					// hd.startElement( "", "", key, atts );
					
					if( content instanceof XmlNode )
					{
						(content).write( hd );
					}
					else
					{
						String str = this.children.get( key ).toString();
						hd.characters( str.toCharArray(), 0, str.length() );
					}
					itn++;
					content = item( key, itn );
				}
				// hd.endElement( "", "", key );
			}
			if( this.m_tag.trim().length() > 0 )
			{
				hd.endElement( "", "", this.m_tag );
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
	}
	
	public void write( Writer out )
	{
		try
		{
			StreamResult streamResult = new StreamResult( out );
			SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory
					.newInstance();
			TransformerHandler hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty( OutputKeys.ENCODING, "ISO-8859-1" );
			serializer.setOutputProperty( OutputKeys.INDENT, "yes" );
			serializer.setOutputProperty( OutputKeys.METHOD, "xml" );
			serializer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4" );
			hd.setResult( streamResult );
			//			hd.startDocument();
			
			write( hd );
			
			hd.endDocument();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public Iterator<XmlNode> iterator()
	{
		return this.children.iterator();
	}
	
	public XmlNode item( int i )
	{
		return this.children.getValue( i );
	}
	
	public Map<String,String> getAttributes() {
		return m_attr;
	}
}
