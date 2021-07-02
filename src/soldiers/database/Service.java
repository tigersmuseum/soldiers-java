package soldiers.database;

import java.sql.Date;
import java.text.SimpleDateFormat;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class Service {

	private String rank, number, regiment, unit, rankqualifier;
	private Date before, after;
	private long tigerId;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
	

	public String getRank() {
		return rank;
	}


	public void setRank(String rank) {
		this.rank = rank;
	}


	public String getNumber() {
		return number;
	}


	public void setNumber(String number) {
		this.number = number;
	}


	public String getRegiment() {
		return regiment;
	}


	public void setRegiment(String regiment) {
		this.regiment = regiment;
	}


	public String getUnit() {
		return unit;
	}


	public void setUnit(String unit) {
		this.unit = unit;
	}


	public Date getBefore() {
		return before;
	}


	public void setBefore(Date before) {
		this.before = before;
	}


	public Date getAfter() {
		return after;
	}


	public void setAfter(Date after) {
		this.after = after;
	}


	public long getTigerId() {
		return tigerId;
	}


	public void setTigerId(long tigerId) {
		this.tigerId = tigerId;
	}

	public String getRankqualifier() {
		return rankqualifier;
	}


	public void setRankqualifier(String rankqualifier) {
		this.rankqualifier = rankqualifier;
	}


	public void serializeService(ContentHandler ch) throws SAXException {
		
		ch.startPrefixMapping("", SoldiersModel.XML_NAMESPACE);

		AttributesImpl attr = new AttributesImpl();
		if ( number != null )  attr.addAttribute("", "number",  "number", "String", number);
		if ( rank != null )  attr.addAttribute("", "rank",  "rank", "String", rank);
		if ( regiment != null )  attr.addAttribute("", "regiment",  "regiment", "String", regiment);
		if ( unit != null )  attr.addAttribute("", "unit",  "unit", "String", unit);
		if ( after != null )  attr.addAttribute("", "after",  "after", "String", formatter.format(after));
		if ( before != null )  attr.addAttribute("", "before",  "before", "String", formatter.format(before));

		ch.startElement(SoldiersModel.XML_NAMESPACE, "service", "service", attr);
		ch.endElement(SoldiersModel.XML_NAMESPACE, "service", "service");	
	}

	@Override
	public String toString() {

		String beforeDate = before == null ? "unknown" : formatter.format(before);
		String afterDate  = after  == null ? "unknown" : formatter.format(after);
		String service = String.format("%d: number=%s, rank=%s, regiment=%s, unit=%s, after=%s, before=%s", tigerId, number, rank, regiment, unit, afterDate, beforeDate);
		return service;
	}
	
}
