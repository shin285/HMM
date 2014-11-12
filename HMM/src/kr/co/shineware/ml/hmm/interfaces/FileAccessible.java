package kr.co.shineware.ml.hmm.interfaces;

/**
 * 
 * @author Junsoo Shin <jsshin@shineware.co.kr>
 * @version 2.1
 * @since 2.1
 *
 */
public interface FileAccessible {
	/**
	 * 현재 사용되고 있는 데이터를 filename에 저장
	 * @param filename
	 */
	public void save(String filename);
	/**
	 * 저장된 filename으로부터 데이터 로드
	 * @param filename
	 */
	public void load(String filename);
}