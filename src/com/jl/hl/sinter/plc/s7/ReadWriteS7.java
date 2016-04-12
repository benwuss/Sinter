package com.jl.hl.sinter.plc.s7;

import org.apache.logging.log4j.Logger;

import com.jl.hl.sinter.plc.moka7.S7;
import com.jl.hl.sinter.plc.moka7.S7Client;
import com.jl.hl.sinter.util.LoggerUtil;

public class ReadWriteS7 {

	private final static Logger logger = LoggerUtil.getLogger(ReadWriteS7.class
			.getSimpleName());

	private String ipAddress = "10.32.132.253";
	private int rack = 0; // for S7400
	private int slot = 2; // for S7400
	private int readDB = 9;
	private int writeDB = 8;
	
	public final static int READ_BYTE_SIZE = 58;
	
	public final static int WRITE_BYTE_SIZE = 9;

	public ReadWriteS7() {

	}

	public byte[] readS7400() {
		byte[] buffer = new byte[READ_BYTE_SIZE];
		S7Client client = new S7Client();
		client.ConnectTo(ipAddress, rack, slot);
		if (client.Connect() == 0) {
			client.ReadArea(S7.S7AreaDB, readDB, 0, buffer.length, buffer);
		}
		client.Disconnect();
		return buffer;
	}

	public void writeS7400(byte[] buffer) {
		S7Client client = new S7Client();
		client.ConnectTo(ipAddress, rack, slot);
		if (client.Connect() == 0) {
			int r = client.WriteArea(S7.S7AreaDB, writeDB, 0, buffer.length, buffer);
			if(r != 0){
				logger.warn("写入S7失败！错误码为{}，请拨打电话18600049580，并将最近一份日志发给Ben", r);
			}
		}
		client.Disconnect();
	}

	public void writeHeartBeats(boolean heartBeats) {		
		byte[] buffer = new byte[1];
		S7.SetBitAt(buffer, 0, 0, heartBeats);		
		S7Client client = new S7Client();
		client.ConnectTo(ipAddress, rack, slot);
		if (client.Connect() == 0) {
			client.WriteArea(S7.S7AreaDB, writeDB, 8, 1, buffer);
		}		
		client.Disconnect();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		
	}

}
