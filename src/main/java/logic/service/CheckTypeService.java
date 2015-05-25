package logic.service;

import java.util.ArrayList;
import java.util.HashMap;

public class CheckTypeService {
	/**  */
	private ArrayList<String> keys;
	private HashMap<String, String> ruleMaps;
	
	/** コンストラクタ */
	public CheckTypeService(ArrayList<String> keys, HashMap<String, String> ruleMaps) {
		this.keys = keys;
		this.ruleMaps = ruleMaps;
	}
	
	public boolean checkForTransfer() {
		// keysのサイズが0のときfalseを返す
		if (keys.size() < 1) 
			return false;
		
		// ルールごとにtypeをチェックし、transferならtrueを入れる
		boolean[] keysTransferFlg = this.setFlgs(keys, ruleMaps, "transfer");
		
		// 全てのflgがtrueのとき、trueを返す
		return checkFlgs(keysTransferFlg);
	}
	
	public boolean checkForAnswer() {
		// keysのサイズが0のときfalseを返す
		if (keys.size() < 1) 
			return false;
		
		// ルールごとにtypeをチェックし、transferならtrueを入れる
		boolean[] keysAnswerFlg = this.setFlgs(keys, ruleMaps, "answer");
		
		// 全てのflgがtrueのとき、trueを返す
		return checkFlgs(keysAnswerFlg);
	}
	
	/**
	 * 
	 * */
	private boolean[] setFlgs(ArrayList<String> keys, HashMap<String, String> ruleMaps, String type) {
		boolean[] keysFlg = new boolean[keys.size()];
		
		for(int i=0;i<keys.size();i++) {
			String key = keys.get(i);
			if(ruleMaps.get(key).equalsIgnoreCase(type))
				keysFlg[i] = true;
			else
				keysFlg[i] = false;
		}
		
		return keysFlg;
	}
	
	/**
	 * 
	 * */
	private boolean checkFlgs(boolean[] keyFlags) {
		for(int i=0;i<keyFlags.length;i++) {
			if(!keyFlags[i])
				return false;
		}
		return true;
	}
}
