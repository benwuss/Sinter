package com.jl.hl.sinter.model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.jl.hl.sinter.plc.moka7.S7;

public class SinterVO {

	// yyyyMMddHHmm.SSS
	private String timeID;

	private BigDecimal gasPressure;

	private BigDecimal airPressure;

	private BigDecimal gasFlow;

	private BigDecimal airFlow;

	private BigDecimal gasFlowValveActual;

	private BigDecimal airFlowValveActual;

	private BigDecimal gasFlowValveGiven;

	private BigDecimal airFlowValveGiven;

	private BigDecimal tempNorth;

	private BigDecimal tempSouth;

	private BigDecimal chamberPressure;

	private BigDecimal airGasRatioActual;

	private BigDecimal airGasRatioGiven;

	private BigDecimal tempGiven = new BigDecimal(0);
	
	private BigDecimal gasFlowGivenMax = new BigDecimal(0);
	
	private BigDecimal gasFlowGivenMin = new BigDecimal(0);
	
	private int mode = 0;

	public void loadFromPLC(byte[] buffer) {

		setGasPressure(new BigDecimal(S7.GetFloatAt(buffer, 0)).setScale(1, BigDecimal.ROUND_HALF_UP));
		setAirPressure(new BigDecimal(S7.GetFloatAt(buffer, 4)).setScale(1, BigDecimal.ROUND_HALF_UP));
		setGasFlow(new BigDecimal(S7.GetFloatAt(buffer, 8)).setScale(1, BigDecimal.ROUND_HALF_UP));
		setAirFlow(new BigDecimal(S7.GetFloatAt(buffer, 12)).setScale(1, BigDecimal.ROUND_HALF_UP));
		setGasFlowValveActual(new BigDecimal(S7.GetFloatAt(buffer, 16)).setScale(1, BigDecimal.ROUND_HALF_UP));
		setAirFlowValveActual(new BigDecimal(S7.GetFloatAt(buffer, 20)).setScale(1, BigDecimal.ROUND_HALF_UP));
		setGasFlowValveGiven(new BigDecimal(S7.GetFloatAt(buffer, 24)).setScale(1, BigDecimal.ROUND_HALF_UP));
		setAirFlowValveGiven(new BigDecimal(S7.GetFloatAt(buffer, 28)).setScale(1, BigDecimal.ROUND_HALF_UP));
		setTempNorth(new BigDecimal(S7.GetFloatAt(buffer, 32)).setScale(1, BigDecimal.ROUND_HALF_UP));
		setTempSouth(new BigDecimal(S7.GetFloatAt(buffer, 36)).setScale(1, BigDecimal.ROUND_HALF_UP));
		setChamberPressure(new BigDecimal(S7.GetFloatAt(buffer, 40)).setScale(1, BigDecimal.ROUND_HALF_UP));
		try {
			float f = S7.GetFloatAt(buffer, 44);
			if (f > 100000) {
				f = -1;
			} else if (f < 0.0001) {
				f = 0;
			}
			setAirGasRatioActual(new BigDecimal(f).setScale(2, BigDecimal.ROUND_HALF_UP));
		} catch (Exception e) {
			setAirGasRatioActual(new BigDecimal(0));
		}
		setAirGasRatioGiven(new BigDecimal(S7.GetFloatAt(buffer, 48)).setScale(2, BigDecimal.ROUND_HALF_UP));
		setTempGiven(new BigDecimal(S7.GetFloatAt(buffer, 52)).setScale(1, BigDecimal.ROUND_HALF_UP));
		setMode(S7.GetShortAt(buffer, 56));
		setGasFlowGivenMax(new BigDecimal(S7.GetFloatAt(buffer, 58)).setScale(1, BigDecimal.ROUND_HALF_UP));
		setGasFlowGivenMin(new BigDecimal(S7.GetFloatAt(buffer, 62)).setScale(1, BigDecimal.ROUND_HALF_UP));
	}

	public SinterVO() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
		this.timeID = formatter.format(currentTime);
	}

	public String getTimeID() {
		return timeID;
	}

	public void setTimeID(String timeID) {
		this.timeID = timeID;
	}

	public BigDecimal getGasPressure() {
		return gasPressure;
	}

	public void setGasPressure(BigDecimal gasPressure) {
		this.gasPressure = gasPressure.setScale(1, BigDecimal.ROUND_HALF_DOWN);
	}

	public BigDecimal getAirPressure() {
		return airPressure;
	}

	public void setAirPressure(BigDecimal airPressure) {
		this.airPressure = airPressure.setScale(1, BigDecimal.ROUND_HALF_DOWN);;
	}

	public BigDecimal getGasFlow() {
		return gasFlow;
	}

	public void setGasFlow(BigDecimal gasFlow) {
		this.gasFlow = gasFlow;
	}

	public BigDecimal getAirFlow() {
		return airFlow;
	}

	public void setAirFlow(BigDecimal airFlow) {
		this.airFlow = airFlow;
	}

	public BigDecimal getGasFlowValveActual() {
		return gasFlowValveActual;
	}

	public void setGasFlowValveActual(BigDecimal gasFlowValveActual) {
		this.gasFlowValveActual = gasFlowValveActual.setScale(1, BigDecimal.ROUND_HALF_DOWN);;
	}

	public BigDecimal getAirFlowValveActual() {
		return airFlowValveActual;
	}

	public void setAirFlowValveActual(BigDecimal airFlowValveActual) {
		this.airFlowValveActual = airFlowValveActual.setScale(1, BigDecimal.ROUND_HALF_DOWN);;
	}

	public BigDecimal getGasFlowValveGiven() {
		return gasFlowValveGiven;
	}

	public void setGasFlowValveGiven(BigDecimal gasFlowValveGiven) {
		this.gasFlowValveGiven = gasFlowValveGiven;
	}

	public BigDecimal getAirFlowValveGiven() {
		return airFlowValveGiven;
	}

	public void setAirFlowValveGiven(BigDecimal airFlowValveGiven) {
		this.airFlowValveGiven = airFlowValveGiven;
	}

	public BigDecimal getTempNorth() {
		return tempNorth;
	}

	public void setTempNorth(BigDecimal tempNorth) {
		this.tempNorth = tempNorth;
	}

	public BigDecimal getTempSouth() {
		return tempSouth;
	}

	public void setTempSouth(BigDecimal tempSouth) {
		this.tempSouth = tempSouth;
	}

	public BigDecimal getChamberPressure() {
		return chamberPressure;
	}

	public void setChamberPressure(BigDecimal chamberPressure) {
		this.chamberPressure = chamberPressure.setScale(1, BigDecimal.ROUND_HALF_DOWN);;
	}

	public BigDecimal getAirGasRatioActual() {
		return airGasRatioActual;
	}

	public void setAirGasRatioActual(BigDecimal airGasRatioActual) {
		this.airGasRatioActual = airGasRatioActual.setScale(2, BigDecimal.ROUND_HALF_DOWN);;
	}

	public BigDecimal getAirGasRatioGiven() {
		return airGasRatioGiven;
	}

	public void setAirGasRatioGiven(BigDecimal airGasRatioGiven) {
		this.airGasRatioGiven = airGasRatioGiven.setScale(2, BigDecimal.ROUND_HALF_DOWN);
	}

	public BigDecimal getTempGiven() {
		return tempGiven;
	}

	public void setTempGiven(BigDecimal tempGiven) {
		this.tempGiven = tempGiven;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}
	
	public BigDecimal getGasFlowGivenMax() {
		return gasFlowGivenMax;
	}

	public void setGasFlowGivenMax(BigDecimal gasFlowGivenMax) {
		this.gasFlowGivenMax = gasFlowGivenMax;
	}

	public BigDecimal getGasFlowGivenMin() {
		return gasFlowGivenMin;
	}

	public void setGasFlowGivenMin(BigDecimal gasFlowGivenMin) {
		this.gasFlowGivenMin = gasFlowGivenMin;
	}
	
	@Override
	public String toString() {
		return "SinterVO [timeID=" + timeID + ", gasPressure=" + gasPressure + ", airPressure=" + airPressure
				+ ", gasFlow=" + gasFlow + ", airFlow=" + airFlow + ", gasFlowValveActual=" + gasFlowValveActual
				+ ", airFlowValveActual=" + airFlowValveActual + ", gasFlowValveGiven=" + gasFlowValveGiven
				+ ", airFlowValveGiven=" + airFlowValveGiven + ", tempNorth=" + tempNorth + ", tempSouth=" + tempSouth
				+ ", chamberPressure=" + chamberPressure + ", airGasRatioActual=" + airGasRatioActual
				+ ", airGasRatioGiven=" + airGasRatioGiven + ", tempGiven=" + tempGiven + ", gasFlowGivenMax="
				+ gasFlowGivenMax + ", gasFlowGivenMin=" + gasFlowGivenMin + ", mode=" + mode + "]";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
