package com.jl.hl.sinter.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.logging.log4j.Logger;

import com.jl.hl.sinter.FIFO;
import com.jl.hl.sinter.SinterControl;
import com.jl.hl.sinter.dao.SaveSinterVOAndValveVOThread;
import com.jl.hl.sinter.dao.TimeUtil;
import com.jl.hl.sinter.model.SinterVO;
import com.jl.hl.sinter.model.ValveVO;
import com.jl.hl.sinter.plc.moka7.S7;
import com.jl.hl.sinter.plc.s7.S7Service;
import com.jl.hl.sinter.util.LoggerUtil;

public class SinterAutoHMI {

	private final static Logger logger = LoggerUtil.getLogger("Control");

	private final static Logger valveLogger = LoggerUtil.getLogger("PLC");

	private JFrame mainFrame;

	private Color BK_COLOR = new Color(12, 12, 12);
	private Color FG_COLOR = new Color(255, 215, 0);

	private Color FG_ALARM_COLOR = new Color(255, 111, 255);

	private Font font = new Font("黑体", Font.PLAIN, 20);
	private Font font2 = new Font("黑体", Font.PLAIN, 16);
	private Font textAreaFont = new Font("黑体", Font.PLAIN, 17);

	GridBagLayout bgl = new GridBagLayout();
	GridBagConstraints bgc = new GridBagConstraints();

	ExtJButton btnM = new ExtJButton(ExtJButton.ROUND_RECT);

	JTextArea textArea = new JTextArea();

	private Tick tickTGasPressure = new Tick();
	private Tick tickTAirPressure = new Tick();

	private Tick tickTemp = new Tick();

	private Tick tickAGR = new Tick();

	private static final String PLASTIC3D = "com.jgoodies.looks.plastic.Plastic3DLookAndFeel";

	private AutoControlThread autoThread = null;

	private boolean isS7300Connected = true;

	S7Service s7 = new S7Service();

	// 1:自动模式，2:煤气模式, 3:手动模式
	private int mode = 2;

	private String statusText = "春天到了，钓鱼季节到了！";

	private FIFO<SinterVO> FIFO_Sinter = new FIFO<SinterVO>(30);

	private FIFO<ValveVO> FIFO_Command = new FIFO<ValveVO>(20);

	private JPanel pPanel = null;

	private JPanel tPanel = null;

	private JPanel vPanel = null;

	private JPanel fPanel = null;

	private JPanel pressurePanel = null;

	private JPanel valvePanel = null;

	private JPanel tempPanel = null;

	private JPanel flowPanel = null;

	private long timeLasted_MODE_1 = 0L;

	private long timeLasted_MODE_2 = 0L;

	private long timeLasted_MODE_3 = 0L;

	public SinterAutoHMI() {

	}

	public static void main(String[] args) {

		setLAF();

		SinterAutoHMI hmi = new SinterAutoHMI();
		hmi.initGUI();
	}

	public static void setLAF() {
		try {
			UIManager.setLookAndFeel(PLASTIC3D);
			// UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
			// UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticLookAndFeel");
			// UIManager.setLookAndFeel("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
			// UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initGUI() {
		mainFrame = new JFrame("烧结点火器自动控制系统");
		mainFrame.getContentPane().setBackground(BK_COLOR);
		ImageIcon ic = new ImageIcon("resources/images/go.jpg");
		mainFrame.setIconImage(ic.getImage());

		// 设置全屏
		mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		int width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int height = Toolkit.getDefaultToolkit().getScreenSize().height;
		mainFrame.setSize(width, height);
		mainFrame.setLocation(0, 0);
		mainFrame.setResizable(true);
		mainFrame.setLayout(bgl);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// 按照以下顺序生成画面
		buttonsPanel();

		pPanel = createPressureTickPanel();
		bgc = new GridBagConstraints(0, 1, 1, 1, 100, 20,
				GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(1,
						5, 1, 5), 100, 0);
		mainFrame.add(pPanel, bgc);

		fPanel = createFlowTickPanel();
		bgc = new GridBagConstraints(1, 1, 1, 1, 100, 20,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(1,
						5, 1, 5), 100, 0);
		mainFrame.add(fPanel, bgc);

		tPanel = createTempTicksPanel();
		bgc = new GridBagConstraints(0, 2, 1, 1, 100, 20,
				GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(1,
						5, 1, 5), 100, 0);
		mainFrame.add(tPanel, bgc);

		vPanel = createValveTickPanel();
		bgc = new GridBagConstraints(1, 2, 1, 1, 100, 20,
				GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(1,
						5, 1, 5), 100, 0);
		mainFrame.add(vPanel, bgc);

		JPanel buttomPanel = createBottomPanel();
		bgc = new GridBagConstraints(0, 3, 2, 1, 100, 30,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						1, 5, 1, 5), 100, 0);
		mainFrame.add(buttomPanel, bgc);
		mainFrame.setVisible(true);

		// 定义了1个线程。
		// 每1秒执行更新画面的值温度与煤气流量，后面可以再增加，让画面动态显示
		// 同时保留最近六笔数据到FIFO对象里。
		autoThread = new AutoControlThread();
		autoThread.start();

	}

	class AutoControlThread extends Thread {

		SinterControl autoControl = new SinterControl();
		SaveSinterVOAndValveVOThread saveDBThread = new SaveSinterVOAndValveVOThread();

		long waitingSeconds = 10;
		boolean isARead = true;
		boolean isGRead = true;
		boolean isMRead = true;
		int failedCounts = 0;
		boolean heartBeats = true;
		SinterVO svo = null;

		public void run() {
			while (isS7300Connected) {
				try {
					Thread.sleep(waitingSeconds);
				} catch (InterruptedException e) {
					waitingSeconds = 1000;
				}

				long start = System.currentTimeMillis();
				svo = new SinterVO();
				byte[] buffer = s7.readS7400();
				svo.loadFromPLC(buffer);
				logger.info("GET SINTERVO FROM PLC FOR {} mills",
						(System.currentTimeMillis() - start));
				if (svo.getMode() == 0) {
					waitingSeconds = 1000;
					failedCounts++;
					logger.info("failedCounts:" + failedCounts);
					if (failedCounts > 3) {
						logger.info("读取信号数据失败{}次。不再尝试与PLC通信，请查明原因后再重启本系统。",
								failedCounts);
						writeTextArea("读取信号数据失败3次。不再尝试与PLC通信，请查明原因后再重启本系统。");
						isS7300Connected = false;
					}
				} else {
					start = System.currentTimeMillis();
					// 主要逻辑在这里！！！
					runningAutoControl(svo, buffer);
					logger.info("RUNNING TIME FOR {} mills",
							(System.currentTimeMillis() - start));
				}
			}
		}

		private void runningAutoControl(SinterVO svo, byte[] buffer) {
			mode = svo.getMode();
			FIFO_Sinter.addLast(svo);
			switch (mode) {
			case 1:
				if (isARead) {
					btnM.setText("温度控制模式");
					writeTextArea("收到您下达的自动温控模式指令！");
					timeLasted_MODE_1 = System.currentTimeMillis();
					isARead = false;
					isMRead = true;
					isGRead = true;
				} else {
					btnM.setText("自动温控模式已"
							+ TimeUtil.getRuningMins(timeLasted_MODE_1) + "分钟");
				}
				break;
			case 2:
				if (isGRead) {
					btnM.setText("煤气流量模式");
					timeLasted_MODE_2 = System.currentTimeMillis();
					writeTextArea("收到您下达的流量模式指令！！");
					isARead = true;
					isGRead = false;
					isMRead = true;
				} else {
					btnM.setText("非自动模式已"
							+ TimeUtil.getRuningMins(timeLasted_MODE_2) + "分钟");
				}
				break;
			case 3:
				if (isMRead) {
					btnM.setText("非自动模式");
					timeLasted_MODE_3 = System.currentTimeMillis();
					writeTextArea("您好，我是HAL 9001型智能燃烧模糊控制系统，请叫我Daenerys，请让我的孩子喷火吧！");
					isARead = true;
					isMRead = false;
					isGRead = true;
				} else {
					btnM.setText("非自动模式已"
							+ TimeUtil.getRuningMins(timeLasted_MODE_3) + "分钟");
				}
				break;
			default:
				isARead = true;
				isMRead = true;
				isGRead = true;
			}
			waitingSeconds = 1000;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateUI();
				}
			});
			autoControl.initStradegyData(FIFO_Sinter, mode);
			autoControl.run();
			ArrayList<ValveVO> valveList = autoControl.getValveList();
			saveDBThread.setDataToSave(svo, valveList);
			saveDBThread.run();
			sendValveCommandToS7(valveList, svo);
			writeTextArea(autoControl.getMsg().toString());
		}

		private void sendValveCommandToS7(ArrayList<ValveVO> valveList,
				SinterVO svo) {

			byte[] buffer = new byte[9];
			S7.SetFloatAt(buffer, 0, svo.getGasFlowValveGiven().floatValue());
			S7.SetFloatAt(buffer, 4, svo.getAirFlowValveGiven().floatValue());
			valveLogger.info(svo);
			for (ValveVO vo : valveList) {
				FIFO_Command.addLast(vo);
				valveLogger.info(vo);
				if (vo.getType().equals("G")) {
					S7.SetFloatAt(buffer, 0, vo.getNewValue().floatValue());
				} else if (vo.getType().equals("A")) {
					S7.SetFloatAt(buffer, 4, vo.getNewValue().floatValue());
				}
			}
			long start = System.currentTimeMillis();
			s7.writeS7400(buffer);
			logger.info("Write PLC FOR {} mills",
					(System.currentTimeMillis() - start));
		}

		private void updateUI() {
			repaintPressureTicks(svo);
			repaintTempTicksPanel(svo);
			repaintValveTickPanel(svo);
			repaintFlowTickPanel(svo);
		}
	}

	private void buttonsPanel() {
		JPanel bPanel = new JPanel();
		bPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 35, 1));
		bPanel.setBackground(BK_COLOR);
		btnM = createExtJButton("待命中", "M");
		bPanel.add(btnM);

		bgc = new GridBagConstraints(0, 0, 3, 1, 100, 0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 100, 0);

		mainFrame.add(bPanel, bgc);

		mainFrame.setVisible(true);
	}

	private ExtJButton createExtJButton(String text, String actionCommand) {
		ExtJButton btn = new ExtJButton(ExtJButton.ROUND_RECT);
		btn.setText(text);
		btn.setFont(font);
		btn.setForeground(FG_COLOR);
		btn.setHorizontalTextPosition(SwingConstants.CENTER);
		btn.setBorder(BorderFactory.createRaisedBevelBorder());
		btn.setActionCommand(actionCommand);
		btn.setPreferredSize(new Dimension(300, 40));
		return btn;
	}

	private JPanel createPressureTickPanel() {
		pressurePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 2));
		pressurePanel.setBackground(BK_COLOR);
		JLabel[] marks = { new JLabel("5"), new JLabel("5") };
		Tick[] ticks = { tickTGasPressure, tickTAirPressure };
		JLabel[] titles = { new JLabel("煤气压力"), new JLabel("空气压力") };
		for (int i = 0; i < ticks.length; i++) {
			JPanel jsPanel = new JPanel(new BorderLayout());
			jsPanel.setBackground(BK_COLOR);
			ticks[i] = createPressureTick(null, 0);
			marks[i].setHorizontalAlignment(SwingConstants.CENTER);
			marks[i].setForeground(FG_COLOR);
			marks[i].setFont(font2);
			titles[i].setHorizontalAlignment(SwingConstants.CENTER);
			titles[i].setForeground(FG_COLOR);
			titles[i].setFont(font2);
			jsPanel.add(titles[i], BorderLayout.SOUTH);
			jsPanel.add(ticks[i], BorderLayout.CENTER);
			jsPanel.add(marks[i], BorderLayout.NORTH);
			pressurePanel.add(jsPanel);
		}
		return pressurePanel;
	}

	private JPanel createFlowTickPanel() {
		flowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 2));
		flowPanel.setBackground(BK_COLOR);
		JLabel[] marks = { new JLabel("7000"), new JLabel("4000") };
		JLabel[] titles = { new JLabel("煤气流量"), new JLabel("空气流量") };
		for (int i = 0; i < 2; i++) {
			JPanel jsPanel = new JPanel(new BorderLayout());
			jsPanel.setBackground(BK_COLOR);
			Tick tick = createFlowTick(null, 0, "");
			marks[i].setHorizontalAlignment(SwingConstants.CENTER);
			marks[i].setForeground(FG_COLOR);
			marks[i].setFont(font2);
			titles[i].setHorizontalAlignment(SwingConstants.CENTER);
			titles[i].setForeground(FG_COLOR);
			titles[i].setFont(font2);
			jsPanel.add(titles[i], BorderLayout.SOUTH);
			jsPanel.add(tick, BorderLayout.CENTER);
			jsPanel.add(marks[i], BorderLayout.NORTH);
			flowPanel.add(jsPanel);
		}
		return flowPanel;
	}

	private void repaintFlowTickPanel(SinterVO vo) {
		flowPanel.removeAll();
		flowPanel.setBackground(BK_COLOR);
		JLabel[] marks = {
				new JLabel(String.valueOf(vo.getGasFlow().intValue())),
				new JLabel(String.valueOf(vo.getAirFlow().intValue())) };
		JLabel[] titles = { new JLabel("煤气流量"), new JLabel("空气流量") };
		int[] values = { vo.getGasFlow().intValue(), vo.getAirFlow().intValue() };
		String[] types = { "G", "A" };
		for (int i = 0; i < 2; i++) {
			JPanel jsPanel = new JPanel(new BorderLayout());
			jsPanel.setBackground(BK_COLOR);
			Tick tick = createFlowTick(vo, values[i], types[i]);
			marks[i].setHorizontalAlignment(SwingConstants.CENTER);
			marks[i].setForeground(FG_COLOR);
			marks[i].setFont(font2);
			titles[i].setHorizontalAlignment(SwingConstants.CENTER);
			titles[i].setForeground(FG_COLOR);
			titles[i].setFont(font2);
			jsPanel.add(titles[i], BorderLayout.SOUTH);
			jsPanel.add(tick, BorderLayout.CENTER);
			jsPanel.add(marks[i], BorderLayout.NORTH);
			flowPanel.add(jsPanel);
		}
		flowPanel.updateUI();
	}

	private JPanel createValveTickPanel() {
		valvePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 2));
		valvePanel.setBackground(BK_COLOR);
		JLabel[] marks = { new JLabel("90"), new JLabel("45") };
		JLabel[] titles = { new JLabel("煤气流量阀"), new JLabel("空气流量阀") };
		for (int i = 0; i < 2; i++) {
			JPanel jsPanel = new JPanel(new BorderLayout());
			jsPanel.setBackground(BK_COLOR);
			Tick tick = createValveTick(null, 0);
			marks[i].setHorizontalAlignment(SwingConstants.CENTER);
			marks[i].setForeground(FG_COLOR);
			marks[i].setFont(font2);
			titles[i].setHorizontalAlignment(SwingConstants.CENTER);
			titles[i].setForeground(FG_COLOR);
			titles[i].setFont(font2);
			jsPanel.add(titles[i], BorderLayout.SOUTH);
			jsPanel.add(tick, BorderLayout.CENTER);
			jsPanel.add(marks[i], BorderLayout.NORTH);
			valvePanel.add(jsPanel);
		}
		return valvePanel;
	}

	private void repaintValveTickPanel(SinterVO svo) {
		valvePanel.removeAll();
		valvePanel.setBackground(BK_COLOR);
		float[] values = { svo.getGasFlowValveActual().floatValue(),
				svo.getAirFlowValveActual().floatValue() };
		JLabel[] marks = {
				new JLabel(String.valueOf(svo.getGasFlowValveActual()
						.floatValue())),
				new JLabel(String.valueOf(svo.getAirFlowValveActual()
						.floatValue())) };
		JLabel[] titles = { new JLabel("煤气流量阀"), new JLabel("空气流量阀") };
		for (int i = 0; i < 2; i++) {
			JPanel jsPanel = new JPanel(new BorderLayout());
			jsPanel.setBackground(BK_COLOR);
			Tick tick = createValveTick(svo, values[i]);
			marks[i].setHorizontalAlignment(SwingConstants.CENTER);
			marks[i].setForeground(FG_COLOR);
			marks[i].setFont(font2);
			titles[i].setHorizontalAlignment(SwingConstants.CENTER);
			titles[i].setForeground(FG_COLOR);
			titles[i].setFont(font2);
			jsPanel.add(titles[i], BorderLayout.SOUTH);
			jsPanel.add(tick, BorderLayout.CENTER);
			jsPanel.add(marks[i], BorderLayout.NORTH);
			valvePanel.add(jsPanel);
		}
		valvePanel.updateUI();
	}

	private JPanel createTempTicksPanel() {
		tempPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 35, 2));
		tempPanel.setBackground(BK_COLOR);
		JLabel tempLabel = new JLabel("1000");
		JLabel argLabel = new JLabel("0.7");
		JLabel tempName = new JLabel("实际温度");
		JLabel argName = new JLabel("实际空燃比");

		JPanel jsPanel1 = new JPanel(new BorderLayout());
		jsPanel1.setBackground(BK_COLOR);
		tickTemp = createTempTick(null, 0);
		tempLabel.setHorizontalAlignment(SwingConstants.CENTER);
		tempLabel.setForeground(FG_COLOR);
		tempLabel.setFont(font2);
		tempName.setHorizontalAlignment(SwingConstants.CENTER);
		tempName.setForeground(FG_COLOR);
		tempName.setFont(font2);
		jsPanel1.add(tempName, BorderLayout.SOUTH);
		jsPanel1.add(tickTemp, BorderLayout.CENTER);
		jsPanel1.add(tempLabel, BorderLayout.NORTH);
		tempPanel.add(jsPanel1);

		JPanel jsPanel2 = new JPanel(new BorderLayout());
		jsPanel2.setBackground(BK_COLOR);
		tickAGR = createAGRTick(null, 0);
		argLabel.setHorizontalAlignment(SwingConstants.CENTER);
		argLabel.setForeground(FG_COLOR);
		argLabel.setFont(font2);
		argName.setHorizontalAlignment(SwingConstants.CENTER);
		argName.setForeground(FG_COLOR);
		argName.setFont(font2);
		jsPanel2.add(argName, BorderLayout.SOUTH);
		jsPanel2.add(tickAGR, BorderLayout.CENTER);
		jsPanel2.add(argLabel, BorderLayout.NORTH);
		tempPanel.add(jsPanel2);

		return tempPanel;
	}

	private void repaintTempTicksPanel(SinterVO svo) {
		tempPanel.removeAll();
		tempPanel.setBackground(BK_COLOR);
		JLabel tempLabel = new JLabel(String.valueOf(svo.getTempGiven()
				.intValue()));
		JLabel argLabel = new JLabel(String.valueOf(svo.getAirGasRatioGiven()
				.floatValue()));
		JLabel tempName = new JLabel("实际温度");
		JLabel argName = new JLabel("实际空燃比");

		JPanel jsPanel1 = new JPanel(new BorderLayout());
		jsPanel1.setBackground(BK_COLOR);
		tickTemp = createTempTick(svo, svo.getTempNorth().floatValue());
		tempLabel.setHorizontalAlignment(SwingConstants.CENTER);
		tempLabel.setForeground(FG_COLOR);
		tempLabel.setFont(font2);
		tempName.setHorizontalAlignment(SwingConstants.CENTER);
		tempName.setForeground(FG_COLOR);
		tempName.setFont(font2);
		jsPanel1.add(tempName, BorderLayout.SOUTH);
		jsPanel1.add(tickTemp, BorderLayout.CENTER);
		jsPanel1.add(tempLabel, BorderLayout.NORTH);
		tempPanel.add(jsPanel1);

		JPanel jsPanel2 = new JPanel(new BorderLayout());
		jsPanel2.setBackground(BK_COLOR);
		tickAGR = createAGRTick(svo, svo.getAirGasRatioActual().floatValue());
		argLabel.setHorizontalAlignment(SwingConstants.CENTER);
		argLabel.setForeground(FG_COLOR);
		argLabel.setFont(font2);
		argName.setHorizontalAlignment(SwingConstants.CENTER);
		argName.setForeground(FG_COLOR);
		argName.setFont(font2);
		jsPanel2.add(argName, BorderLayout.SOUTH);
		jsPanel2.add(tickAGR, BorderLayout.CENTER);
		jsPanel2.add(argLabel, BorderLayout.NORTH);
		tempPanel.add(jsPanel2);

		tempPanel.updateUI();
	}

	private void repaintPressureTicks(SinterVO svo) {
		pressurePanel.removeAll();
		pressurePanel.setBackground(BK_COLOR);
		JLabel[] titles = { new JLabel("煤气压力"), new JLabel("空气压力") };
		JLabel[] marks = {
				new JLabel(String.valueOf(svo.getGasPressure().floatValue())),
				new JLabel(String.valueOf(svo.getAirPressure().floatValue())) };
		Tick[] ticks = { tickTGasPressure, tickTAirPressure };
		float[] values = { svo.getGasPressure().floatValue(),
				svo.getAirPressure().floatValue() };
		for (int i = 0; i < ticks.length; i++) {
			JPanel jsPanel = new JPanel(new BorderLayout());
			jsPanel.setBackground(BK_COLOR);
			ticks[i] = createPressureTick(svo, values[i]);
			marks[i].setHorizontalAlignment(SwingConstants.CENTER);
			marks[i].setForeground(FG_COLOR);
			marks[i].setFont(font2);
			titles[i].setHorizontalAlignment(SwingConstants.CENTER);
			titles[i].setForeground(FG_COLOR);
			titles[i].setFont(font2);
			jsPanel.add(titles[i], BorderLayout.SOUTH);
			jsPanel.add(ticks[i], BorderLayout.CENTER);
			jsPanel.add(marks[i], BorderLayout.NORTH);
			pressurePanel.add(jsPanel);
		}
		pressurePanel.updateUI();
	}

	private Tick createPressureTick(SinterVO svo, float inValue) {
		String value = "5";
		Color color = FG_COLOR;
		Tick tick = new Tick();
		tick.setType(Tick.RING_240);
		tick.setFrom(0);
		tick.setTo(20);
		tick.setTickFontSize(11);
		tick.setMajor(5);
		tick.setMinor(1);
		tick.setUnit("Kpa");
		tick.setBackground(BK_COLOR);
		if (svo != null) {
			value = String.valueOf(inValue);
			if (inValue < 3) {
				color = FG_ALARM_COLOR;
			}
		}
		tick.setValue(value);
		tick.setForeground(color);
		return tick;
	}

	private Tick createValveTick(SinterVO svo, float inValue) {
		String value = "90";
		Color color = FG_COLOR;
		Tick tick = new Tick();
		tick.setType(Tick.RING_240);
		tick.setFrom(0);
		tick.setTo(100);
		tick.setTickFontSize(11);
		tick.setMajor(10);
		tick.setMinor(5);
		tick.setUnit("%");
		tick.setBackground(BK_COLOR);
		if (svo != null) {
			value = String.valueOf(inValue);
			if (inValue > 90) {
				color = FG_ALARM_COLOR;
			}
		}
		tick.setValue(value);
		tick.setForeground(color);
		return tick;
	}

	private Tick createTempTick(SinterVO svo, float inValue) {
		String value = "1000";
		Color color = FG_COLOR;
		Tick tick = new Tick();
		tick.setType(Tick.RING_240);
		tick.setFrom(700);
		tick.setTo(1100);
		tick.setTickFontSize(11);
		tick.setMajor(100);
		tick.setMinor(10);
		tick.setUnit("°");
		tick.setBackground(BK_COLOR);
		if (svo != null) {
			value = String.valueOf(inValue);
			if (inValue < (svo.getTempGiven().floatValue() - 10)
					|| inValue > (svo.getTempGiven().floatValue() + 10)) {
				color = FG_ALARM_COLOR;
			}
		}
		tick.setValue(value);
		tick.setForeground(color);
		return tick;
	}

	private Tick createAGRTick(SinterVO svo, float inValue) {
		String value = "0.7";
		Color color = FG_COLOR;
		Tick tick = new Tick();
		tick.setType(Tick.RING_240);
		tick.setFrom(0.0);
		tick.setTo(1.0);
		tick.setTickFontSize(11);
		tick.setMajor(0.5);
		tick.setMinor(0.1);
		tick.setUnit("°");
		tick.setBackground(BK_COLOR);
		if (svo != null) {
			value = String.valueOf(inValue);
			if (inValue < (svo.getAirGasRatioGiven().floatValue() - 0.1)
					|| inValue > (svo.getAirGasRatioGiven().floatValue() + 0.1)) {
				color = FG_ALARM_COLOR;
			}
		}
		tick.setValue(value);
		tick.setForeground(color);
		return tick;
	}

	private Tick createFlowTick(SinterVO svo, float inValue, String type) {
		String value = "7000";
		Color color = FG_COLOR;
		Tick tick = new Tick();
		tick.setType(Tick.RING_240);
		tick.setFrom(0);
		tick.setTo(15000);
		tick.setMajor(5000);
		tick.setMinor(1000);
		tick.setUnit("㎥");
		tick.setBackground(BK_COLOR);
		if (svo != null) {
			value = String.valueOf(inValue);
			if (type.equals("A") && inValue > 6000 || inValue < 3000) {
				color = FG_ALARM_COLOR;
			} else if (type.equals("G") && inValue < 6000 || inValue > 10000) {
				color = FG_ALARM_COLOR;
			}
		}
		tick.setValue(value);
		tick.setForeground(color);
		return tick;
	}

	private JPanel createBottomPanel() {
		JPanel outPanel = new JPanel(new BorderLayout());
		outPanel.add(createTextArea(), BorderLayout.CENTER);
		return outPanel;
	}

	private JScrollPane createTextArea() {
		JPanel outPanel = new JPanel(new BorderLayout());
		outPanel.setPreferredSize(new Dimension(900, 200));
		outPanel.setBackground(BK_COLOR);
		textArea = new JTextArea("", 200, 100);
		writeTextArea(statusText);
		textArea.setPreferredSize(new Dimension(900, 200));
		textArea.setEditable(false);
		textArea.setFont(textAreaFont);
		textArea.setBackground(BK_COLOR);
		textArea.setForeground(FG_COLOR);
		textArea.setSelectedTextColor(Color.WHITE);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane sPane = new JScrollPane(textArea);
		sPane.setBackground(BK_COLOR);
		sPane.setForeground(FG_COLOR);
		sPane.setPreferredSize(new Dimension(900, 200));
		sPane.setWheelScrollingEnabled(true);

		return sPane;
	}

	private void writeTextArea(String msg) {
		if (msg.trim().length() > 0) {
			Date currentTime = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
			String time = formatter.format(currentTime);
			StringBuffer sb = new StringBuffer();
			sb.append(time).append(" ").append(msg).append("\n");
			textArea.insert(sb.toString(), 0);
		}
	}
}
