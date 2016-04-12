package com.jl.hl.sinter.plc.moka7.demo;

import com.jl.hl.sinter.plc.moka7.*;

public class PLCTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String ipAddress = "10.32.132.253";
		int rack = 0; // Default 0 for S7400
		int slot = 2; // Default 2 for S7400
		int DBNum_Read = 9; 
		int DBNum_Write = 8;
		
		IntByRef SizeRead = new IntByRef(0);

		S7Client Client = new S7Client();

		try {

			int Result = 0;
			Result = Client.ConnectTo(ipAddress, rack, slot);
			if (Result == 0) {
				System.out.println("Connected to   : " + ipAddress + " (Rack="
						+ rack + ", Slot=" + slot + ")");
				System.out.println("PDU negotiated : " + Client.PDULength()
						+ " bytes");
				System.out.println("1. 与PLC通信成功");
			}

			// 2 get DB
			byte[] Buffer3 = new byte[100];
			Client.DBGet(DBNum_Read, Buffer3, SizeRead);
			System.out.println("数据长度：" + SizeRead.Value);

			// 3 read DB
			byte[] Buffer = new byte[58];
			// read the first data
			Result = Client.ReadArea(S7.S7AreaDB, DBNum_Read, 0, SizeRead.Value,
					Buffer);
			System.out.println("ReadDB Result:" + Result);
			// read float (signed 32 bit float) from PLC
			System.out.println("read DB: " + S7.GetFloatAt(Buffer,0));
			System.out.println("read DB: " + S7.GetFloatAt(Buffer,4));
			System.out.println("read DB: " + S7.GetFloatAt(Buffer,8));
			

			 // 4 write DB
			 byte[] Buffer2 = new byte[20];
			 S7.SetFloatAt(Buffer2, 0, 2046);
			 S7.SetFloatAt(Buffer2, 4, 99.2f);
			 S7.SetFloatAt(Buffer2, 8, 1984);
			 Client.WriteArea(S7.S7AreaDB, DBNum_Write, 0, 12, Buffer2);

			

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			Client.Disconnect();
			System.out.println("close!");
		}
	}
}
