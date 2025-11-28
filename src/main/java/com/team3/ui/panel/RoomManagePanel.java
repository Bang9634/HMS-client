package com.team3.ui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.team3.client.api.RoomApi;
import com.team3.dto.request.RoomIdRequest;
import com.team3.dto.response.ApiResponse;
import com.team3.ui.dialog.AddRoomDialog;
import com.team3.ui.dialog.PriceChangeLogDialog;

/**
 * 객실 관리 패널
 * <p>
 * 객실 목록 조회, 추가/수정/삭제/금액 변경 기능 제공
 * </p>
 *
 * @author bang9634
 * @since 2025-11-27
 */
public class RoomManagePanel extends JPanel {

	private static final Logger logger = LoggerFactory.getLogger(RoomManagePanel.class);

	private final RoomApi roomApi;
	private JButton refreshButton;
	private JButton addRoomButton;
	private JTable roomTable;
	private DefaultTableModel tableModel;
	private JLabel statusLabel;
	private JProgressBar progressBar;

	private static final String[] COLUMN_NAMES = {
		"객실번호", "기본금액", "점유상태", "최대인원", "금액변경로그", "수정", "삭제"
	};

	public RoomManagePanel(String serverHost, int serverPort) {
		this.roomApi = new RoomApi(serverHost, serverPort);
		initComponents();
		setupLayout();
		loadRoomList();
		logger.info("RoomManagePanel 초기화 완료");
	}

	private void initComponents() {
		setBackground(Color.WHITE);

		refreshButton = new JButton("새로고침");
		refreshButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
		refreshButton.setBackground(new Color(52, 152, 219));
		refreshButton.setForeground(Color.BLACK);
		refreshButton.setFocusPainted(false);
		refreshButton.setPreferredSize(new Dimension(120, 36));
		refreshButton.addActionListener(e -> loadRoomList());

		addRoomButton = new JButton("객실 추가");
		addRoomButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
		addRoomButton.setBackground(new Color(39, 174, 96));
		addRoomButton.setForeground(Color.BLACK);
		addRoomButton.setFocusPainted(false);
		addRoomButton.setPreferredSize(new Dimension(120, 36));
		addRoomButton.addActionListener(e -> handleAddRoom());

		tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				// 수정/삭제 버튼만 편집 가능
				return column == 4 || column == 5 || column == 6;
			}
		};
		roomTable = new JTable(tableModel);
		roomTable.setRowHeight(32);
		roomTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
		roomTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 13));
		roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// 수정/삭제 버튼 렌더러 및 에디터
		roomTable.getColumn("금액변경로그").setCellRenderer(new ButtonRenderer("금액변경로그", new Color(52, 152, 219)));
    	roomTable.getColumn("금액변경로그").setCellEditor(new ButtonEditor("금액변경로그", this::handlePriceChangeLog));
		roomTable.getColumn("수정").setCellRenderer(new ButtonRenderer("수정", new Color(241, 196, 15)));
		roomTable.getColumn("수정").setCellEditor(new ButtonEditor("수정", this::handleEditRoom));
		roomTable.getColumn("삭제").setCellRenderer(new ButtonRenderer("삭제", new Color(231, 76, 60)));
		roomTable.getColumn("삭제").setCellEditor(new ButtonEditor("삭제", this::handleDeleteRoom));

		statusLabel = new JLabel("대기 중...");
		statusLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(false);
		progressBar.setVisible(false);
	}

	private void setupLayout() {
		setLayout(new BorderLayout(10, 10));
		setBorder(new EmptyBorder(20, 20, 20, 20));

		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
		topPanel.setBackground(Color.WHITE);
		topPanel.add(refreshButton);
		topPanel.add(addRoomButton);
		add(topPanel, BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane(roomTable);
		scrollPane.setBorder(BorderFactory.createTitledBorder(
			BorderFactory.createLineBorder(Color.LIGHT_GRAY),
			"객실 목록",
			0,
			0,
			new Font("맑은 고딕", Font.BOLD, 12)
		));
		add(scrollPane, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
		bottomPanel.setBackground(Color.WHITE);
		bottomPanel.add(statusLabel, BorderLayout.WEST);
		bottomPanel.add(progressBar, BorderLayout.CENTER);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	/**
	 * 객실 목록 조회 및 테이블 갱신
	 */
	private void loadRoomList() {
		statusLabel.setText("객실 목록 조회 중...");
		progressBar.setVisible(true);
		progressBar.setIndeterminate(true);
		tableModel.setRowCount(0);

		SwingWorker<ApiResponse, Void> worker = new SwingWorker<>() {
			@Override
			protected ApiResponse doInBackground() {
				return roomApi.getRoomList();
			}

			@Override
			protected void done() {
				try {
					ApiResponse response = get();
					if (response.isSuccess()) {
						List<RoomRow> rooms = parseRoomList(response.getBody());
						updateTable(rooms);
						statusLabel.setText("✓ 객실 목록 조회 성공 (" + rooms.size() + "개)");
						statusLabel.setForeground(new Color(39, 174, 96));
					} else {
						statusLabel.setText("✗ 객실 목록 조회 실패");
						statusLabel.setForeground(new Color(231, 76, 60));
					}
				} catch (InterruptedException | ExecutionException e) {
					logger.error("객실 목록 조회 실패", e);
					statusLabel.setText("✗ 오류 발생: " + e.getMessage());
					statusLabel.setForeground(new Color(231, 76, 60));
				} finally {
					progressBar.setVisible(false);
					progressBar.setIndeterminate(false);
				}
			}
		};
		worker.execute();
	}

	/**
	 * 객실 목록 JSON 파싱
	 */
	private List<RoomRow> parseRoomList(String json) {
		Gson gson = new Gson();
		try {
			com.google.gson.JsonObject obj = gson.fromJson(json, com.google.gson.JsonObject.class);
			if (obj.has("rooms")) {
				com.google.gson.JsonElement roomsElem = obj.get("rooms");
				Type listType = new TypeToken<List<RoomRow>>(){}.getType();
				return gson.fromJson(roomsElem, listType);
			}
		} catch (JsonSyntaxException e) {
			logger.error("객실 목록 파싱 실패", e);
		}
		return java.util.Collections.emptyList();
	}

	/**
	 * 테이블에 객실 목록 표시
	 */
	private void updateTable(List<RoomRow> rooms) {
		tableModel.setRowCount(0);
		for (RoomRow room : rooms) {
			Vector<Object> row = new Vector<>();
			row.add(room.roomId);
			row.add(room.basePrice);
			row.add(room.isAvailable ? "비어있음" : "점유중");
			row.add(room.maxOccupancy);
			row.add("금액변경로그");
			row.add("수정");
			row.add("삭제");
			tableModel.addRow(row);
		}
	}

	/**
	 * 객실 추가 버튼 클릭 처리
	 */
	private void handleAddRoom() {
		AddRoomDialog dialog = new AddRoomDialog(SwingUtilities.getWindowAncestor(this), roomApi);
		dialog.setVisible(true);
		loadRoomList();
	}

	/**
	 * 금액변경로그 버튼 클릭 처리
	 */
	private void handlePriceChangeLog(int row) {
		int roomId = (int) tableModel.getValueAt(row, 0);
		PriceChangeLogDialog dialog = new PriceChangeLogDialog(
			SwingUtilities.getWindowAncestor(this),
			roomApi,
			roomId
		);
		dialog.setVisible(true);
	}

	/**
	 * 객실 수정 버튼 클릭 처리
	 */
	private void handleEditRoom(int row) {
		// int roomId = (int) tableModel.getValueAt(row, 0);
		// int basePrice = (int) tableModel.getValueAt(row, 1);
		// boolean isAvailable = "비어있음".equals(tableModel.getValueAt(row, 2));
		// int maxOccupancy = (int) tableModel.getValueAt(row, 3);
		// String priceChangeReason = (String) tableModel.getValueAt(row, 4);

		// EditRoomDialog dialog = new EditRoomDialog(
		// 	SwingUtilities.getWindowAncestor(this),
		// 	roomApi,
		// 	roomId,
		// 	basePrice,
		// 	isAvailable,
		// 	maxOccupancy,
		// 	priceChangeReason
		// );
		// dialog.setVisible(true);
		// loadRoomList();
	}

	/**
	 * 객실 삭제 버튼 클릭 처리
	 */
	private void handleDeleteRoom(int row) {
		int roomId = (int) tableModel.getValueAt(row, 0);
		int confirm = JOptionPane.showConfirmDialog(this, "정말로 객실 [" + roomId + "]를 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);
		if (confirm == JOptionPane.YES_OPTION) {
			ApiResponse response = roomApi.deleteRoom(new RoomIdRequest(roomId));
			if (response.getStatusCode() != 200) {
				JOptionPane.showConfirmDialog(this, response.getBody(), "삭제 실패", JOptionPane.CLOSED_OPTION);
			}
		}
		loadRoomList();
	}

	/**
	 * 객실 정보 DTO (테이블 표시용)
	 */
	private static class RoomRow {
		int roomId;
		int basePrice;
		boolean isAvailable;
		int maxOccupancy;
		String priceChangeReason;
	}

	/**
	 * 테이블 버튼 셀 렌더러
	 */
	private static class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
		public ButtonRenderer(String text, Color bg) {
			setText(text);
			setFont(new Font("맑은 고딕", Font.BOLD, 12));
			setBackground(bg);
			setForeground(Color.BLACK);
			setFocusPainted(false);
		}
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			return this;
		}
	}

	/**
	 * 테이블 버튼 셀 에디터
	 */
	private static class ButtonEditor extends DefaultCellEditor {
		private final JButton button;
		private final java.util.function.IntConsumer action;
		private int row;

		public ButtonEditor(String text, java.util.function.IntConsumer action) {
			super(new JTextField());
			this.action = action;
			button = new JButton(text);
			button.setFont(new Font("맑은 고딕", Font.BOLD, 12));
			button.setBackground(text.equals("삭제") ? new Color(231, 76, 60) : new Color(241, 196, 15));
			button.setForeground(Color.BLACK);
			button.setFocusPainted(false);
			button.addActionListener(e -> action.accept(row));
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			this.row = row;
			return button;
		}

		@Override
		public Object getCellEditorValue() {
			return button.getText();
		}
	}
}
