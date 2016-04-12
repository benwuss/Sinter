package com.jl.hl.sinter;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;

import com.jl.hl.sinter.model.SinterVO;
import com.jl.hl.sinter.model.ValveVO;
import com.jl.hl.sinter.util.LoggerUtil;

/**
 * 将被控制类作为单例使用。
 * 
 * @author benwu
 * @since 2016.3.21
 * 
 */
public class SinterService {

	private final static Logger logger = LoggerUtil.getLogger("Control");

	private final static Logger WARNLogger = LoggerUtil.getLogger("WARN");

	public static final float AIR_VALVE_MAX = 55;

	private SinterVO svo;

	private FIFO<SinterVO> FIFO_SinterQueue;

	private ArrayList<ValveVO> valveList = new ArrayList<ValveVO>();

	private StringBuffer msg = new StringBuffer();

	private boolean isTemp950Lock = false;

	private boolean isTemp970Lock = false;

	private boolean isTemp1000Lock = false;

	private boolean isTemp1020Lock = false;

	private boolean isTemp1040Lock = false;

	private boolean isTemp1060Lock = false;

	private int gasFlowMax;

	private int gasFlowMin;

	public SinterService() {

	}

	public ArrayList<ValveVO> getValveList() {
		return valveList;
	}

	public StringBuffer getMsg() {
		return msg;
	}

	private void initGlobalParams(FIFO<SinterVO> FIFO_SinterQueue) {
		this.FIFO_SinterQueue = FIFO_SinterQueue;
		msg = new StringBuffer();
		valveList = new ArrayList<ValveVO>();
		svo = FIFO_SinterQueue.getMostRecentOne();
		gasFlowMax = svo.getGasFlowGivenMax().intValue();
		gasFlowMin = svo.getGasFlowGivenMin().intValue();

		if (svo.getAirGasRatioGiven().floatValue() > 0.5f || svo.getAirGasRatioGiven().floatValue() < 0.4f) {
			svo.setAirGasRatioGiven(new BigDecimal(0.44));
		}

		if (svo.getTempGiven().intValue() < 1000) {
			svo.setTempGiven(new BigDecimal(1000));
		}

		tempAutoAdjust();

		// 以下是温度达标后的煤气流量节省模式，最高温的优先级高，所以排最后
		if (isTemp1000Lock) {
			gasFlowMax = 9400;
			gasFlowMin = 8800;
		}

		if (isTemp1020Lock) {
			gasFlowMax = 9200;
			gasFlowMin = 8500;
		}

		if (isTemp1040Lock) {
			gasFlowMax = 8500;
			gasFlowMin = 7800;
		}

		if (isTemp1060Lock) {
			gasFlowMax = 8300;
			gasFlowMin = 7600;
		}

		// 以下是温度过低的煤气临时加量模式，低温的最优先，所以排最后。
		if (isTemp970Lock) {
			gasFlowMax = 9700;
			gasFlowMin = 9300;
		}

		if (isTemp950Lock) {
			gasFlowMax = 10000;
			gasFlowMin = 9400;
		}

		logger.info(
				"isTemp1060Lock:{},isTemp1040Lock:{},isTemp1020Lock:{}, isTemp1000Lock:{}, isTemp970Lock:{}, isTemp950Lock:{}",
				isTemp1060Lock, isTemp1040Lock, isTemp1020Lock, isTemp1000Lock, isTemp970Lock, isTemp950Lock);
		logger.info("gasFlowMax:{}, gasFlowMin:{}", gasFlowMax, gasFlowMin);
		msg.append("本次煤气流量允许波动范围==>上限为：" + gasFlowMax + ", 下限为：" + gasFlowMin + "。\n");
	}

	private void tempAutoAdjust() {
		int temp = svo.getTempNorth().intValue();
		int avg = getAvgTemp(20);

		if (temp <= 950 && isTemp950Lock == false) {
			if (avg != 0 && avg < 950) {
				isTemp950Lock = true;
				msg.append("过去20秒温度平均低于950度，煤气流量上限临时提到10000！\n");
			}
		} else if (isTemp950Lock && temp > 975) {
			if (avg > 975) {
				isTemp950Lock = false;
				msg.append("过去20秒温度已提升到975度以上，煤气流量上限以设定值为准。\n");
			}
		}
		if (temp <= 970 && temp > 950 && isTemp970Lock == false) {
			if (avg != 0 && avg < 970) {
				isTemp970Lock = true;
				msg.append("过去20秒温度平均在950-970度之间，煤气流量上限临时提到9700！\n");
			}
		} else if (isTemp970Lock && temp > 990) {
			if (avg > 990) {
				isTemp970Lock = false;
				msg.append("过去20秒温度已提升到990度以上，煤气流量上限以设定值为准。\n");
			}
		}
		if (temp >= 1005 && temp < 1020 && isTemp1000Lock == false) {
			if (avg >= 1005) {
				isTemp1000Lock = true;
				msg.append("过去20秒温度已达1005度以上，煤气流量上限启动节省模式。\n");
			}
		} else if (temp <= 1000 && isTemp1000Lock) {
			if (avg != 0 && avg <= 1000) {
				isTemp1000Lock = false;
				msg.append("过去20秒温度已掉到1000以下，解除煤气流量上限节省模式。\n");
			}
		}
		if (temp >= 1020 && temp < 1040 && isTemp1020Lock == false) {
			if (avg >= 1020) {
				isTemp1020Lock = true;
				msg.append("过去20秒温度已提升到1020度以上，煤气流量上限启动节省模式。\n");
			}
		} else if (temp <= 1005 && isTemp1020Lock) {
			if (avg != 0 && avg <= 1005) {
				isTemp1020Lock = false;
				msg.append("过去20秒温度已掉到1005以下，解除煤气流量上限节省模式。\n");
			}
		}
		if (temp >= 1040 && temp < 1060 && isTemp1040Lock == false) {
			if (avg >= 1040) {
				isTemp1040Lock = true;
				msg.append("过去20秒温度已提升到1040度以上，煤气流量上限启动节省模式。\n");
			}
		} else if (temp <= 1020 && isTemp1040Lock) {
			if (avg != 0 && avg <= 1020) {
				isTemp1040Lock = false;
				msg.append("过去20秒温度已掉到1020以下，解除煤气流量上限节省模式。\n");
			}
		}
		if (temp >= 1060 && isTemp1060Lock == false) {
			if (avg >= 1060) {
				isTemp1060Lock = true;
				msg.append("过去20秒温度已提升到1060度以上，煤气流量上限启动节省模式。\n");
			}
		} else if (temp <= 1040 && isTemp1060Lock) {
			if (avg != 0 && avg <= 1040) {
				isTemp1060Lock = false;
				msg.append("过去20秒温度已掉到1040以下，解除煤气流量上限节省模式。\n");
			}
		}
	}

	private int getAvgTemp(int rows) {
		int avg = 0;
		int[] tempArray = getRecentTempArray(FIFO_SinterQueue.getFIFOByDSC(), rows);
		if (tempArray.length == rows) {
			int t = 0;
			for (int i = 0; i < tempArray.length; i++) {
				t = t + tempArray[i];
			}
			avg = t / rows;
		}
		return avg;
	}

	public ArrayList<ValveVO> getChangedValves() {
		return valveList;
	}

	/**
	 * 根据上下限控制流量。热值够，9500可以烧到1010左右，10500可以到1040，11000可以烧到1050以上。
	 * 热值不够时，这个策略很重要。如果以后装热值仪，这个策略可以退役。根据热值决定流量才能谈温度命中率。
	 * 
	 * @param FIFO_SinterQueue
	 */
	public void gasFlowLimitsStradegy(FIFO<SinterVO> FIFO_SinterQueue) {
		initGlobalParams(FIFO_SinterQueue);
		msg.append("进入煤气流量上下限调整策略.\n");
		gasFlowLimitsTuningRule();
	}

	/**
	 * 如果热值够，流量到11000左右，温度可以烧到1040以上。如果热值不够，这么大的流量反而会让温度下降。
	 * 
	 * @param FIFO_SinterQueue
	 */
	public void synchroStradegy(FIFO<SinterVO> FIFO_SinterQueue) {
		initGlobalParams(FIFO_SinterQueue);
		int gasFlow = svo.getGasFlow().intValue();

		// 先检查煤气流量是否在上下限之内，如果不在就先修正煤气流量，跳过本次温度控制。
		if (gasFlow > gasFlowMax || gasFlow < gasFlowMin) {
			msg.append("因流量在上下限之外，本次温控策略跳过。\n");
			logger.info("煤气流量：{}，不在煤气上限：{}， 煤气下限：{}范围内，跳过温度控制模式！", gasFlow, gasFlowMax, gasFlowMin);
			return;
		}

		msg.append("进入温度控制模式.\n");
		// 现在开始进入温控模式
		int tempNow = svo.getTempNorth().intValue();
		int tempTarget = svo.getTempGiven().intValue();
		int tempGap = tempTarget - tempNow;
		int[] tempArray = getRecentTempArray(FIFO_SinterQueue.getFIFOByDSC(), 6);
		int[] gasFlowArray = getRecentFlowArray(FIFO_SinterQueue.getFIFOByDSC(), "G", 6);

		if (tempArray.length > 5) {
			int t1 = tempArray[0];
			int t6 = tempArray[5];
			int diff = t1 - t6;
			logger.info("t1-t6:{},{},{},{},{},{}", tempArray[0], tempArray[1], tempArray[2], tempArray[3], tempArray[4],
					tempArray[5]);
			if (diff > 1 && (tempGap > 0 && tempGap < 25)) {
				msg.append("升温模式，但温度处在上升趋势，本次不执行同步策略。\n");
				return;
			} else if (diff < -1 && tempGap < 0) {
				msg.append("降温模式，但温度处在下降趋势，本次不执行同步策略。\n");
				return;
			}
		}

		if (gasFlowArray.length > 5) {
			int t1 = gasFlowArray[0];
			int t6 = gasFlowArray[5];
			int diff = t1 - t6;
			logger.info("t1-t6:{},{},{},{},{},{}", gasFlowArray[0], gasFlowArray[1], gasFlowArray[2], gasFlowArray[3],
					gasFlowArray[4], gasFlowArray[5]);
			// 流量变化相对于温度是先行指标，如果流量动，温度变化会滞后，就不需要根据温度差及时开阀。
			if (diff >= 500 && tempGap > 0) {
				msg.append("升温模式，且流量处于增加趋势，本次不执行同步策略，能不能升温看热值了。\n");
				return;
			} else if (diff <= -500 && tempGap < 0) {
				msg.append("降温模式，但流量处于减少趋势，本次不执行同步策略。\n");
				return;
			}
		}

		float span = getFlowValveSpan(tempGap);
		if (span != 0) {
			setFlowValves(ValveVO.T_STRADEGY, span);
		}
	}

	private void gasFlowLimitsTuningRule() {
		int gasFlow = svo.getGasFlow().intValue();
		int gap = 0;
		if (gasFlow > gasFlowMax) {
			gap = gasFlowMax - svo.getGasFlow().intValue();
		} else if (gasFlow < gasFlowMin) {
			gap = gasFlowMin - svo.getGasFlow().intValue();
		}
		float span = getGasSpan(gap, svo.getGasPressure().floatValue());
		if (span != 0) {
			setFlowValves(ValveVO.G_STRADEGY, span);
		}
	}

	public void gasQuotaStradegy(FIFO<SinterVO> FIFO_SinterQueue) {
		initGlobalParams(FIFO_SinterQueue);
		msg.append("进入煤气定额策略。现在北面温度是：" + svo.getTempNorth().intValue() + "。南面温度是：" + svo.getTempSouth() + "\n");

		int gap = gasFlowMax - svo.getGasFlow().intValue();

		if (gap > 0 && svo.getGasFlowValveActual().intValue() > 95) {
			msg.append("煤气阀已经开到顶了，再等等看。\n");
			return;
		}

		if (gap > 0 && svo.getGasFlow().intValue() > gasFlowMax) {
			msg.append("煤气流量已达上限。不再调整阀位。\n");
			return;
		}

		if (gap < 0 && svo.getGasFlow().intValue() < gasFlowMin) {
			msg.append("煤气流量已达下限。不再调整阀位。\n");
			return;
		}

		if (gap < 0 && svo.getGasFlowValveActual().intValue() < 30) {
			msg.append("煤气阀已经太低了，再等等看。\n");
			return;
		}

		float span = getGasSpan(gap, svo.getGasPressure().floatValue());
		if (span != 0) {
			setFlowValves(ValveVO.G_STRADEGY, span);
		} else {
			msg.append("本次流量策略限幅为零。\n");
		}
	}

	private float getGasSpan(int gap, float gasMain) {
		float span = 0;
		if (gap > 0) {
			// 升流量限幅分五档位
			if (gap > 3500) {
				span = 12;
			} else if (gap <= 3500 && gap >= 2500) {
				span = 10;
			} else if (gap < 2500 && gap >= 1000) {
				span = 8;
			} else if (gap < 1000 && gap >= 50) {
				span = 6;
			} else if (gap < 50) {
				span = 3;
			}
		} else if (gap < 0) {
			// 降流量限幅分6档位
			gap = gap * -1;
			if (gap > 3000) {
				span = -12;
			} else if (gap <= 3000 && gap >= 2000) {
				span = -8;
			} else if (gap < 2000 && gap >= 1500) {
				span = -6;
			} else if (gap < 1500 && gap >= 500) {
				span = -4;
			} else if (gap < 500 && gap >= 100) {
				span = -3;
			} else if (gap < 100) {
				span = -2;
			}
		}
		return span;
	}

	public void airGasRatioTuningStradegy(FIFO<SinterVO> FIFO_SinterQueue) {
		initGlobalParams(FIFO_SinterQueue);
		msg.append("进入空燃比调整策略。\n");
		int tempNow = svo.getTempNorth().intValue();
		int tempTarget = svo.getTempGiven().intValue();
		int tempGap = tempTarget - tempNow;
		int mode = svo.getMode();

		float actual = svo.getAirGasRatioActual().floatValue();
		float given = svo.getAirGasRatioGiven().floatValue();

		int span = getAirGasRatioSpan(actual, given);

		if (span != 0 && mode == 1) {
			// 升温模式
			if (tempGap > 0) {
				if (svo.getGasFlowValveActual().compareTo(new BigDecimal(90)) < 0) {
					if (svo.getGasFlow().intValue() > gasFlowMax || svo.getGasFlow().intValue() < gasFlowMin) {
						adjustAirByGas(span);
					} else {
						adjustGasByAir(span);
					}
				} else {
					adjustAirByGas(span);
				}
			}
			// 降温过程的空气阀位调整策略
			if (tempGap < 0) {
				msg.append("进入降温过程空气阀位调整策略。\n");
				if (svo.getGasFlow().intValue() > gasFlowMax || svo.getGasFlow().intValue() < gasFlowMin) {
					adjustAirByGas(span);
				} else {
					adjustGasByAir(span);
				}
			}
		} else if (span != 0 && mode == 2) {
			adjustAirByGas(span);
		} else {
			msg.append("实际空燃比在给定值范围，不做调整").append(".\n");
		}
	}

	private int getAirGasRatioSpan(float actual, float given) {
		float gap = actual - given;
		float airGasRatioDiff = 0.02f;
		int span = 0;

		if (gap > airGasRatioDiff) {
			if (gap > 0.2) {
				span = 6;
			} else if (gap <= 0.2 && gap > 0.1) {
				span = 5;
			} else if (gap <= 0.1 && gap > 0.06) {
				span = 4;
			} else if (gap <= 0.06 && gap >= 0.02) {
				span = 3;
			}
		} else if (gap < -airGasRatioDiff) {
			if (gap < -0.2) {
				span = -6;
			} else if (gap < -0.1 && gap >= -0.2) {
				span = -5;
			} else if (gap < -0.05 && gap >= -0.1) {
				span = -4;
			} else if (gap <= -0.02 && gap >= -0.05) {
				span = -3;
			}
		} else {
			span = 0;
		}
		return span;
	}

	private void adjustGasByAir(int span) {
		float newValve = svo.getGasFlowValveActual().floatValue() + span;
		ValveVO vG = new ValveVO(svo.getTimeID(), ValveVO.A_STRADEGY, "G");
		vG.setNewValue(new BigDecimal(newValve));
		vG.setValueChange(new BigDecimal(span));
		vG.setOldValue(svo.getGasFlowValveActual());
		valveList.add(vG);
		msg.append("根据空燃比调整煤气阀位,限幅为").append(span).append("， 煤气阀位给定：").append(newValve).append(".\n");
	}

	private void adjustAirByGas(int span) {
		// 第一道保护 add on 2016-03-31
		if ((span < 0 && isAirReachMax()) || (span > 0 && isAirReachMin())) {
			return;
		}

		float newValve = svo.getAirFlowValveActual().floatValue() - span;

		// 第二到保护
		if (newValve >= AIR_VALVE_MAX) {
			newValve = AIR_VALVE_MAX;
			WARNLogger.warn("空气阀位已到最高值。\n");
		}

		ValveVO vA = new ValveVO(svo.getTimeID(), ValveVO.A_STRADEGY, "A");
		vA.setNewValue(new BigDecimal(newValve));
		vA.setValueChange(new BigDecimal(-span));
		vA.setOldValue(svo.getAirFlowValveActual());
		valveList.add(vA);
		msg.append("根据空燃比调整空气阀位,限幅为").append(-span).append("，空气阀位给定：").append(newValve).append(".\n");
	}

	private boolean isAirReachMax() {
		boolean isMax = false;
		int airMax = (svo.getAirGasRatioGiven().multiply(svo.getGasFlowGivenMax())).intValue();
		int airNow = svo.getAirFlow().intValue();
		logger.info("当期空气流量：{}，给定最大量：{}", airNow, airMax);
		if (airNow >= airMax) {
			msg.append("空气流量已达最大，不再增加阀位开度！\n");
			isMax = true;
		}
		return isMax;
	}

	private boolean isAirReachMin() {
		boolean isMin = false;
		int airMin = (svo.getAirGasRatioGiven().multiply(svo.getGasFlowGivenMin())).intValue();
		int airNow = svo.getAirFlow().intValue();
		logger.info("当期空气流量：{}，给定最小量：{}", airNow, airMin);
		if (airNow <= airMin) {
			msg.append("空气流量已达最小，不再减少阀位开度！\n");
			isMin = true;
		}
		return isMin;
	}

	private void setFlowValves(String stradegy, float span) {

		// modified on 2016-3-29 删除空气随动规则。
		float newGasValve = svo.getGasFlowValveActual().floatValue() + span;

		if (newGasValve < 0)
			newGasValve = 0;
		if (newGasValve > 100)
			newGasValve = 100;

		float gasSpan = newGasValve - svo.getGasFlowValveActual().floatValue();
		ValveVO vG = new ValveVO(svo.getTimeID(), stradegy, "G");
		vG.setNewValue(new BigDecimal(newGasValve));
		vG.setValueChange(new BigDecimal(gasSpan));
		vG.setOldValue(svo.getGasFlowValveActual());
		valveList.add(vG);
		msg.append("策略：" + stradegy + "：煤气阀位执行限幅为：" + span).append("。新值为：").append(newGasValve).append("。\n");
		doWarnlog(newGasValve);

	}

	private float getFlowValveSpan(int tempGap) {
		float span = 0;
		if (tempGap > 0) {
			// 升温限幅分四档位
			if (tempGap >= 25) {
				span = 8;
			} else if (tempGap < 25 && tempGap >= 15) {
				span = 6;
			} else if (tempGap < 15 && tempGap > 8) {
				span = 4;
			} else if (tempGap <= 8) {
				span = 0;
			}
		} else if (tempGap < 0) {
			// 降温限幅先和升温一样，未来会变再改
			tempGap = tempGap * -1;
			if (tempGap >= 120) {
				span = -7;
			} else if (tempGap < 120 && tempGap >= 110) {
				span = -5;
			} else if (tempGap < 110 && tempGap > 100) {
				span = -3;
			} else if (tempGap <= 100) {
				span = 0;
			}
		}
		return span;
	}

	private void doWarnlog(float newValve) {
		if (newValve < 38 || newValve > 58) {
			WARNLogger.info("警告！煤气阀新值为:{}，目标在38-58之间。", newValve);
		}
	}

	/**
	 * 取得最近几笔的温度值，用来判断升温趋势
	 * 
	 * @param alist
	 * @param rows
	 * @return
	 */
	public int[] getRecentTempArray(ArrayList<SinterVO> alist, int rows) {
		if (alist.size() < rows) {
			rows = alist.size();
		}
		int[] tempArray = new int[rows];
		for (int i = 0; i < rows; i++) {
			tempArray[i] = alist.get(i).getTempNorth().intValue();
		}
		return tempArray;
	}

	public int[] getRecentFlowArray(ArrayList<SinterVO> alist, String type, int rows) {
		if (alist.size() < rows) {
			rows = alist.size();
		}
		int[] tempArray = new int[rows];
		for (int i = 0; i < rows; i++) {
			if (type.equals("G")) {
				tempArray[i] = alist.get(i).getGasFlow().intValue();
			} else if (type.equals("A")) {
				tempArray[i] = alist.get(i).getAirFlow().intValue();
			}
		}
		return tempArray;
	}

}
