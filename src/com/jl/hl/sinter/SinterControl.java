package com.jl.hl.sinter;

import java.util.ArrayList;

import org.apache.logging.log4j.Logger;

import com.jl.hl.sinter.dao.RecordRunningTimeThread;
import com.jl.hl.sinter.model.SinterVO;
import com.jl.hl.sinter.model.ValveVO;
import com.jl.hl.sinter.util.LoggerUtil;

public class SinterControl {

	private final static Logger logger = LoggerUtil.getLogger("Control");

	private SinterService service = new SinterService();

	private long startTime_MODE_1 = 0L;
	private long startTime_MODE_2 = 0L;

	private FIFO<SinterVO> FIFO_SinterQueue;

	private ArrayList<ValveVO> valveList = new ArrayList<ValveVO>();

	private int tempInterval = 5;

	private int gasLimitInterval = 1;

	private int airGasInterval = 2;

	private int airGasTuningCounts = 3;

	private boolean runAirGasStradegy = false;

	private StringBuffer msg;

	/**
	 * 1:自动，3：手动
	 */
	private int mode = 3;

	public SinterControl() {
	}

	public void initStradegyData(FIFO<SinterVO> FIFO_SinterQueue, int newMode) {
		this.FIFO_SinterQueue = FIFO_SinterQueue;
		valveList = new ArrayList<ValveVO>();
		msg = new StringBuffer();
		if ((newMode == 1 || newMode == 2) && mode != newMode) {
			tempInterval = 5;
			gasLimitInterval = 1;
			runAirGasStradegy = false;
		}
		recordRunningTime(newMode);
		mode = newMode;
	}

	public void run() {
		if (mode == 1 || mode == 2) {

			counting();

			if (gasLimitInterval == 0) {
				doGasLimitControl();
				gasLimitInterval = 5;
			}

			if (mode == 1 && tempInterval == 0 && runAirGasStradegy == false) {
				doTempControl();
				runAirGasStradegy = true;
				airGasInterval = 6;
			} else if (mode == 2 && tempInterval == 0 && runAirGasStradegy == false) {
				doGasQuataControl();
				runAirGasStradegy = true;
				airGasInterval = 3;
			} else if (airGasInterval == 0 && runAirGasStradegy) {
				doAirGasTuningControl();
				airGasInterval = 4;
				airGasTuningCounts--;
				if (airGasTuningCounts == 0) {
					airGasTuningCounts = 3;
					tempInterval = 2;
					runAirGasStradegy = false;
				}
			}
			logger.info(msg.toString());
		}
	}

	private void doTempControl() {
		service.synchroStradegy(FIFO_SinterQueue);
		ArrayList<ValveVO> vos = service.getChangedValves();
		msg.append(service.getMsg());
		copyFrom(vos);
	}

	private void doGasLimitControl() {
		service.gasFlowLimitsStradegy(FIFO_SinterQueue);
		ArrayList<ValveVO> vos = service.getChangedValves();
		msg.append(service.getMsg());
		copyFrom(vos);
	}

	private void doAirGasTuningControl() {
		service.airGasRatioTuningStradegy(FIFO_SinterQueue);
		ArrayList<ValveVO> vos = service.getChangedValves();
		msg.append(service.getMsg());
		copyFrom(vos);
	}

	private void doGasQuataControl() {
		service.gasQuotaStradegy(FIFO_SinterQueue);
		ArrayList<ValveVO> vos = service.getChangedValves();
		msg.append(service.getMsg());
		copyFrom(vos);
	}

	public void counting() {
		tempInterval--;
		airGasInterval--;
		gasLimitInterval--;

		if (airGasInterval < 0) {
			airGasInterval = 0;
		}
		if (tempInterval < 0) {
			tempInterval = 0;
		}
		if (gasLimitInterval < 0) {
			gasLimitInterval = 0;
		}
	}

	private void recordRunningTime(int newMode) {
		if ((newMode == 1 && mode != newMode)) {
			startTime_MODE_1 = System.currentTimeMillis();
			logger.info("开始进入温控模式");
		}
		if (mode == 1 && mode != newMode) {
			long now = System.currentTimeMillis();
			long mins = (now - startTime_MODE_1) / 1000 / 60;
			long temp = (now - startTime_MODE_1) % (1000 * 60);
			long seconds = temp / 1000;

			logger.info("本次温控模式运行{}分{}秒", mins, seconds);
			recordDuration(startTime_MODE_1, mins);
		}
		if ((newMode == 2 && mode != newMode)) {
			startTime_MODE_2 = System.currentTimeMillis();
			logger.info("开始进入流量模式");
		}
		if (mode == 2 && mode != newMode) {
			long now = System.currentTimeMillis();
			long mins = (now - startTime_MODE_2) / 1000 / 60;
			long temp = (now - startTime_MODE_2) % (1000 * 60);
			long seconds = temp / 1000;

			logger.info("本次流量模式运行{}分{}秒", mins, seconds);
			recordDuration(startTime_MODE_2, mins);
		}
	}

	private void copyFrom(ArrayList<ValveVO> sourceList) {
		for (ValveVO vo : sourceList) {
			valveList.add(vo);
		}
	}

	private void recordDuration(long startTime, long mins) {
		RecordRunningTimeThread record = new RecordRunningTimeThread();
		record.setData(startTime, mins, mode);
		record.start();
	}

	public ArrayList<ValveVO> getValveList() {
		return valveList;
	}

	public StringBuffer getMsg() {
		return msg;
	}
}
