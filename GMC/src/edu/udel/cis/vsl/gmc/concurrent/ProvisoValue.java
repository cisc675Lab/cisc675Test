package edu.udel.cis.vsl.gmc.concurrent;

/**
 * This enumeration stores the result of checking "stack proviso" of a STATE in
 * the STATE-TRANSITION system. 
 * <ul>
 * <li>UNKNOWN means that "stack proviso" has not been checked yet.</li>
 * <li>TRUE means that the STATE satisfies "stack proviso".</li>
 * <li>FALSE means that the STATE does not satisfy "stack proviso".</li>
 * </ul>
 * @author yanyihao
 *
 */
public enum ProvisoValue {
	TRUE, UNKNOWN, FALSE
}
