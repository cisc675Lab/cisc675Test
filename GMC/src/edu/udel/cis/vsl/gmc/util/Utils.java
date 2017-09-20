package edu.udel.cis.vsl.gmc.util;

import java.util.Collection;
import java.util.LinkedList;

public class Utils {
	/**
	 * 
	 * @param minuend
	 * @param subtrahend
	 * @return A collection subtraction: <code>{minuend} - {subtrahend}</code>
	 */
	public static Collection<? extends Object> subtract(
			Collection<? extends Object> minuend,
			Collection<? extends Object> subtrahend) {
		Collection<Object> result = new LinkedList<>();

		for (Object minuendEle : minuend) {
			boolean contains = false;

			for (Object subtrahendEle : subtrahend)
				if (minuendEle.equals(subtrahendEle)) {
					contains = true;
					break;
				}
			if (!contains)
				result.add(minuendEle);
		}
		return result;
	}
}
